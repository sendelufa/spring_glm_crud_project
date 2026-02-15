# Deployment Guide - Ubuntu VPS

Complete guide to deploy AlcoholShop Spring Boot application on a clean Ubuntu VPS.

## Prerequisites

- Ubuntu 20.04+ or 22.04+ (clean server)
- Root or sudo access
- At least 1GB RAM (2GB recommended)
- Open ports: 80, 443, 22

---

## Quick Start (One-Command Deploy)

```bash
curl -fsSL https://raw.githubusercontent.com/YOUR_USERNAME/alcohol-shop/main/java/deploy.sh | bash
```

---

## Manual Deployment Steps

### Step 1: Initial Server Setup

```bash
# Update system
sudo apt update && sudo apt upgrade -y

# Set timezone (optional)
sudo timedatectl set-timezone Europe/Moscow

# Create application user (non-root)
sudo useradd -m -s /bin/bash app
sudo usermod -aG sudo app
```

### Step 2: Install Docker and Docker Compose

```bash
# Install prerequisites
sudo apt install -y curl git ca-certificates gnupg

# Install Docker
curl -fsSL https://download.docker.com/linux/ubuntu/gpg | sudo gpg --dearmor -o /usr/share/keyrings/docker-archive-keyring.gpg

echo \
  "deb [arch=$(dpkg --print-architecture) signed-by=/usr/share/keyrings/docker-archive-keyring.gpg] https://download.docker.com/linux/ubuntu \
  $(lsb_release -cs) stable" | sudo tee /etc/apt/sources.list.d/docker.list > /dev/null

sudo apt update
sudo apt install -y docker-ce docker-ce-cli containerd.io docker-compose-plugin

# Enable Docker on boot
sudo systemctl enable docker
sudo systemctl start docker

# Add user to docker group (logout and login after this)
sudo usermod -aG docker $USER
```

**Logout and login again** for docker group changes to take effect.

### Step 3: Clone Repository

```bash
# Switch to app user
sudo su - app

# Clone your repository (replace with your repo URL)
git clone https://github.com/YOUR_USERNAME/alcohol-shop.git ~/app
cd ~/app/java  # or adjust path based on your repo structure
```

### Step 4: Configure Environment Variables

```bash
# Create .env file
cat > .env << 'EOF'
# Database Password (CHANGE THIS!)
DB_PASSWORD=your_secure_password_here

# Application Port
APP_PORT=8080

# Spring Profile
SPRING_PROFILES_ACTIVE=prod

# JWT Secret (MUST BE 256+ bits, CHANGE THIS!)
JWT_SECRET=$(openssl rand -base64 32)

# JVM Options
JAVA_OPTS=-Xms256m -Xmx1g -XX:+UseG1GC
EOF

# Secure the file
chmod 600 .env
```

### Step 5: Start Application with Docker Compose

```bash
# Build and start services
docker compose up -d --build

# Check logs
docker compose logs -f

# Check service status
docker compose ps
```

### Step 6: Configure Firewall

```bash
# Allow SSH, HTTP, HTTPS
sudo ufw allow 22/tcp
sudo ufw allow 80/tcp
sudo ufw allow 443/tcp
sudo ufw --force enable

# Check status
sudo ufw status
```

### Step 7: Set up Nginx Reverse Proxy (Optional but Recommended)

```bash
# Install Nginx
sudo apt install -y nginx

# Create Nginx config
sudo tee /etc/nginx/sites-available/alcoholshop << 'EOF'
server {
    listen 80;
    server_name your-domain.com;

    client_max_body_size 10M;

    location / {
        proxy_pass http://localhost:8080;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
    }
}
EOF

# Enable site
sudo ln -s /etc/nginx/sites-available/alcoholshop /etc/nginx/sites-enabled/
sudo rm -f /etc/nginx/sites-enabled/default
sudo nginx -t
sudo systemctl restart nginx
```

### Step 8: Configure SSL with Let's Encrypt (Recommended)

```bash
# Install Certbot
sudo apt install -y certbot python3-certbot-nginx

# Obtain certificate
sudo certbot --nginx -d your-domain.com

# Auto-renewal is configured automatically
sudo certbot renew --dry-run
```

---

## Monitoring & Management

### Check Application Health

```bash
# Health check
curl http://localhost:8080/actuator/health

# View logs
docker compose logs -f app

# View database logs
docker compose logs -f postgres
```

### Common Commands

```bash
# Stop services
docker compose stop

# Start services
docker compose start

# Restart services
docker compose restart

# Rebuild after code changes
docker compose up -d --build

# Remove everything (WARNING: deletes data!)
docker compose down -v

# View resource usage
docker stats
```

### Database Backup

```bash
# Backup database
docker compose exec postgres pg_dump -U postgres alcoholshop > backup_$(date +%Y%m%d).sql

# Restore database
docker compose exec -T postgres psql -U postgres alcoholshop < backup.sql
```

---

## Security Checklist

- [ ] Changed `DB_PASSWORD` in `.env`
- [ ] Changed `JWT_SECRET` to a strong 256+ bit key
- [ ] Firewall enabled (UFW)
- [ ] SSH key-based authentication only (disable password auth)
- [ ] Regular security updates: `sudo apt update && sudo apt upgrade -y`
- [ ] Docker and system logs monitored
- [ ] SSL/HTTPS configured
- [ ] PostgreSQL not exposed to public (only internal network)

---

## Troubleshooting

### Application won't start

```bash
# Check logs
docker compose logs app

# Check container status
docker compose ps

# Inspect container
docker inspect alcoholshop-app
```

### Database connection issues

```bash
# Check PostgreSQL is ready
docker compose exec postgres pg_isready -U postgres

# Test connection
docker compose exec postgres psql -U postgres -d alcoholshop -c "SELECT 1;"
```

### Out of memory

```bash
# Check current memory usage
free -h

# Reduce JVM memory in .env:
JAVA_OPTS=-Xms128m -Xmx512m

# Then restart
docker compose restart app
```

---

## Performance Tuning

### For Production (High Traffic)

```yaml
# In docker-compose.yml, adjust:
deploy:
  resources:
    limits:
      cpus: '4'
      memory: 2G
    reservations:
      cpus: '2'
      memory: 1G

# In .env:
JAVA_OPTS=-Xms512m -Xmx1536m -XX:+UseG1GC -XX:MaxGCPauseMillis=200
```

### For Low Memory VPS (512MB-1GB)

```yaml
# In docker-compose.yml:
deploy:
  resources:
    limits:
      cpus: '1'
      memory: 512M
    reservations:
      cpus: '0.25'
      memory: 256M

# In .env:
JAVA_OPTS=-Xms64m -Xmx256m -XX:+UseSerialGC
```

---

## Update Procedure

```bash
# Pull latest code
git pull

# Rebuild and restart
docker compose up -d --build

# Monitor startup
docker compose logs -f
```

---

## Support & Logs

```bash
# Application logs
docker compose logs -f app > app.log

# System logs
sudo journalctl -u docker -f

# Nginx logs
sudo tail -f /var/log/nginx/access.log
sudo tail -f /var/log/nginx/error.log
```

---

## Quick Reference

| Port | Service |
|------|---------|
| 8080 | Spring Boot App |
| 5432 | PostgreSQL (internal only) |
| 80 | HTTP (Nginx) |
| 443 | HTTPS (Nginx) |

| URL | Description |
|-----|-------------|
| `/actuator/health` | Health check |
| `/swagger-ui.html` | API documentation |
| `/api-docs/openapi.json` | OpenAPI spec |

#!/bin/bash
# One-command deployment script for AlcoholShop Spring Boot application
# Usage: curl -fsSL https://raw.githubusercontent.com/YOUR_REPO/main/deploy.sh | bash

set -e

COLOR_RED='\033[0;31m'
COLOR_GREEN='\033[0;32m'
COLOR_YELLOW='\033[1;33m'
COLOR_NC='\033[0m' # No Color

log_info() {
    echo -e "${COLOR_GREEN}[INFO]${COLOR_NC} $1"
}

log_warn() {
    echo -e "${COLOR_YELLOW}[WARN]${COLOR_NC} $1"
}

log_error() {
    echo -e "${COLOR_RED}[ERROR]${COLOR_NC} $1"
}

# Check if running as root
if [ "$EUID" -ne 0 ]; then
    log_error "This script must be run as root (use sudo)"
    exit 1
fi

log_info "Starting deployment of AlcoholShop application..."

# Step 1: Update system
log_info "Updating system packages..."
apt update && apt upgrade -y

# Step 2: Set timezone
log_info "Setting timezone to Europe/Moscow..."
timedatectl set-timezone Europe/Moscow 2>/dev/null || true

# Step 3: Create application user
log_info "Creating application user 'app'..."
if ! id "app" &>/dev/null; then
    useradd -m -s /bin/bash app
    usermod -aG sudo app
    log_info "User 'app' created"
else
    log_info "User 'app' already exists"
fi

# Step 4: Install Docker
log_info "Installing Docker..."
if ! command -v docker &> /dev/null; then
    apt install -y curl git ca-certificates gnupg

    curl -fsSL https://download.docker.com/linux/ubuntu/gpg | gpg --dearmor -o /usr/share/keyrings/docker-archive-keyring.gpg

    echo \
      "deb [arch=$(dpkg --print-architecture) signed-by=/usr/share/keyrings/docker-archive-keyring.gpg] https://download.docker.com/linux/ubuntu \
      $(lsb_release -cs) stable" | tee /etc/apt/sources.list.d/docker.list > /dev/null

    apt update
    apt install -y docker-ce docker-ce-cli containerd.io docker-compose-plugin

    systemctl enable docker
    systemctl start docker

    log_info "Docker installed successfully"
else
    log_info "Docker already installed"
fi

# Step 5: Add app user to docker group
log_info "Adding 'app' user to docker group..."
usermod -aG docker app

# Step 6: Configure firewall
log_info "Configuring firewall..."
if command -v ufw &> /dev/null; then
    ufw allow 22/tcp
    ufw allow 80/tcp
    ufw allow 443/tcp
    ufw --force enable
    log_info "Firewall configured"
else
    log_warn "UFW not installed, skipping firewall configuration"
fi

# Step 7: Clone repository (if not already cloned)
log_info "Setting up application..."
APP_DIR="/home/sendel/myaiproject"
JAVA_DIR="$APP_DIR/java"

if [ ! -d "$JAVA_DIR" ]; then
    log_error "Repository not found at $JAVA_DIR"
    log_error "Please clone your repository first or update the path in this script"
    exit 1
fi

# Step 8: Setup .env file
log_info "Configuring environment variables..."
ENV_FILE="$JAVA_DIR/.env"

if [ ! -f "$ENV_FILE" ]; then
    if [ -f "$JAVA_DIR/.env.example" ]; then
        cp "$JAVA_DIR/.env.example" "$ENV_FILE"

        # Generate secure JWT secret
        JWT_SECRET=$(openssl rand -base64 32)
        sed -i "s/your-super-secret-jwt-key-for-testing-change-this-in-production-at-least-256-bits/$JWT_SECRET/" "$ENV_FILE"

        # Generate secure DB password
        DB_PASSWORD=$(openssl rand -base64 16 | tr -d "=+/" | cut -c1-16)
        sed -i "s/your_secure_password_here/$DB_PASSWORD/" "$ENV_FILE"

        chown app:app "$ENV_FILE"
        chmod 600 "$ENV_FILE"

        log_info "Environment file created"
        log_warn "IMPORTANT: Save these credentials securely!"
        log_warn "DB Password: $DB_PASSWORD"
        log_warn "JWT Secret: $JWT_SECRET"
    else
        log_warn ".env.example not found, skipping environment configuration"
    fi
else
    log_info ".env file already exists"
fi

# Step 9: Start application with Docker Compose
log_info "Building and starting application..."
cd "$JAVA_DIR"

# Build and start services
sudo -u app docker compose up -d --build

# Wait for services to be healthy
log_info "Waiting for services to start..."
sleep 10

# Check service status
MAX_ATTEMPTS=30
ATTEMPT=0

while [ $ATTEMPT -lt $MAX_ATTEMPTS ]; do
    if sudo -u app docker compose ps | grep -q "healthy"; then
        log_info "Services started successfully!"
        break
    fi

    ATTEMPT=$((ATTEMPT + 1))
    if [ $ATTEMPT -eq $MAX_ATTEMPTS ]; then
        log_error "Services failed to start within expected time"
        log_info "Check logs with: docker compose logs"
        exit 1
    fi

    sleep 2
done

# Step 10: Display deployment info
log_info "=========================================="
log_info "Deployment completed successfully!"
log_info "=========================================="
echo ""
log_info "Application Status:"
sudo -u app docker compose ps
echo ""
log_info "Useful Commands:"
echo "  View logs:        docker compose logs -f"
echo "  Check health:      curl http://localhost:8080/actuator/health"
echo "  Stop services:     docker compose stop"
echo "  Start services:    docker compose start"
echo "  Restart services:  docker compose restart"
echo ""
log_info "Application URLs:"
echo "  Health Check:      http://localhost:8080/actuator/health"
echo "  Swagger UI:        http://localhost:8080/swagger-ui.html"
echo ""
log_warn "Next Steps:"
log_warn "1. Configure Nginx reverse proxy (see DEPLOY.md)"
log_warn "2. Setup SSL with Let's Encrypt (see DEPLOY.md)"
log_warn "3. Change your domain DNS to point to this server"
log_warn "4. Review security checklist in DEPLOY.md"
echo ""
log_info "For more information, see: DEPLOY.md"

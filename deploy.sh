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

log_info "Starting deployment of AlcoholShop application..."
log_info "Running as user: $(whoami)"
log_info "Working directory: $(pwd)"

# Check if Docker is available
if ! command -v docker &> /dev/null; then
    log_error "Docker is not installed. Please install Docker first:"
    log_error "  https://docs.docker.com/get-docker/"
    exit 1
fi

# Check if user can run docker
if ! docker ps &> /dev/null; then
    log_error "Cannot run Docker commands. Make sure your user is in the docker group:"
    log_error "  sudo usermod -aG docker \$(whoami)"
    log_error "Then log out and back in for changes to take effect."
    exit 1
fi

log_info "Docker is available and ready"

# Setup application directory
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
log_info "Application directory: $SCRIPT_DIR"

# Setup .env file
log_info "Configuring environment variables..."
ENV_FILE="$SCRIPT_DIR/.env"

if [ ! -f "$ENV_FILE" ]; then
    if [ -f "$SCRIPT_DIR/.env.example" ]; then
        cp "$SCRIPT_DIR/.env.example" "$ENV_FILE"

        # Generate secure JWT secret
        JWT_SECRET=$(openssl rand -base64 32)
        sed -i "s/your-super-secret-jwt-key-for-testing-change-this-in-production-at-least-256-bits/$JWT_SECRET/" "$ENV_FILE"

        # Generate secure DB password
        DB_PASSWORD=$(openssl rand -base64 16 | tr -d "=+/" | cut -c1-16)
        sed -i "s/your_secure_password_here/$DB_PASSWORD/" "$ENV_FILE"

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

# Start application with Docker Compose
log_info "Building and starting application..."
cd "$SCRIPT_DIR"

# Build and start services
docker compose up -d --build

# Wait for services to be healthy
log_info "Waiting for services to start..."
sleep 10

# Check service status
MAX_ATTEMPTS=30
ATTEMPT=0

while [ $ATTEMPT -lt $MAX_ATTEMPTS ]; do
    if docker compose ps | grep -q "healthy"; then
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
docker compose ps
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

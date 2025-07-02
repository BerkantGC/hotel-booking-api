#!/bin/bash

# Environment validation script for Hotel Booking API

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

print_status() {
    echo -e "${GREEN}[✓]${NC} $1"
}

print_warning() {
    echo -e "${YELLOW}[!]${NC} $1"
}

print_error() {
    echo -e "${RED}[✗]${NC} $1"
}

print_info() {
    echo -e "${BLUE}[i]${NC} $1"
}

echo "🔍 Hotel Booking API - Environment Validation"
echo "============================================="

# Check if .env file exists
if [ ! -f .env ]; then
    print_error ".env file not found!"
    echo "Run: cp .env.example .env"
    exit 1
fi

print_status ".env file exists"

# Load environment variables
source .env

echo ""
echo "📊 Environment Configuration Summary:"
echo "======================================"

# Database Configuration
echo ""
print_info "Database Configuration:"
echo "  PostgreSQL: ${POSTGRES_USER}@postgresql:${POSTGRES_PORT:-5432}/${POSTGRES_DB}"
echo "  MongoDB: ${MONGO_INITDB_ROOT_USERNAME}@mongodb:${MONGODB_PORT:-27017}"
echo "  Redis: redis:${REDIS_PORT:-6379}"

# Service Ports
echo ""
print_info "Service Ports:"
echo "  Gateway: ${GATEWAY_SERVICE_PORT:-8080}"
echo "  Auth: ${AUTH_SERVICE_PORT:-8087}"
echo "  Hotel: ${HOTEL_SERVICE_PORT:-8084}"
echo "  Booking: ${BOOKING_SERVICE_PORT:-8085}"
echo "  Comment: ${COMMENT_SERVICE_PORT:-8081}"
echo "  Admin: ${HOTEL_ADMIN_SERVICE_PORT:-8083}"
echo "  Notification: ${NOTIFICATION_SERVICE_PORT:-8082}"
echo "  AI Agent: ${AI_AGENT_SERVICE_PORT:-3001}"
echo "  Socket: ${SOCKET_SERVICE_PORT:-3000}"

# Security Check
echo ""
print_info "Security Configuration:"
if [ ${#JWT_SECRET} -lt 32 ]; then
    print_warning "JWT_SECRET is shorter than 32 characters (not recommended for production)"
else
    print_status "JWT_SECRET length is adequate"
fi

if [ ${#INTERNAL_SECRET_KEY} -lt 16 ]; then
    print_warning "INTERNAL_SECRET_KEY is shorter than 16 characters"
else
    print_status "INTERNAL_SECRET_KEY length is adequate"
fi

# AWS Configuration
echo ""
print_info "AWS Configuration:"
if [ "$AWS_ACCESS_KEY_ID" = "YOUR_AWS_ACCESS_KEY_ID" ]; then
    print_warning "AWS_ACCESS_KEY_ID is set to example value"
else
    print_status "AWS_ACCESS_KEY_ID is configured"
fi

if [ "$AWS_SECRET_ACCESS_KEY" = "YOUR_AWS_SECRET_ACCESS_KEY" ]; then
    print_warning "AWS_SECRET_ACCESS_KEY is set to example value"
else
    print_status "AWS_SECRET_ACCESS_KEY is configured"
fi

if [ "$AWS_S3_BUCKET" = "your-s3-bucket-name" ]; then
    print_warning "AWS_S3_BUCKET is set to example value"
else
    print_status "AWS_S3_BUCKET: ${AWS_S3_BUCKET}"
fi

# Port conflicts check
echo ""
print_info "Checking for potential port conflicts:"
ports=(${GATEWAY_SERVICE_PORT:-8080} ${AUTH_SERVICE_PORT:-8087} ${HOTEL_SERVICE_PORT:-8084} ${BOOKING_SERVICE_PORT:-8085} ${COMMENT_SERVICE_PORT:-8081} ${HOTEL_ADMIN_SERVICE_PORT:-8083} ${NOTIFICATION_SERVICE_PORT:-8082} ${AI_AGENT_SERVICE_PORT:-3001} ${SOCKET_SERVICE_PORT:-3000} ${POSTGRES_PORT:-5432} ${MONGODB_PORT:-27017} ${RABBITMQ_AMQP_PORT:-5672} ${RABBITMQ_MANAGEMENT_PORT:-15672} ${REDIS_PORT:-6379})

for port in "${ports[@]}"; do
    if lsof -i :$port >/dev/null 2>&1; then
        print_warning "Port $port is already in use"
    else
        print_status "Port $port is available"
    fi
done

echo ""
echo "🎯 Next Steps:"
echo "=============="
if [ "$AWS_ACCESS_KEY_ID" = "YOUR_AWS_ACCESS_KEY_ID" ] || [ "$AWS_SECRET_ACCESS_KEY" = "YOUR_AWS_SECRET_ACCESS_KEY" ]; then
    print_warning "Update AWS credentials in .env file for S3 functionality"
fi
echo "1. Run: ./build.sh"
echo "2. Access application at: http://localhost:${GATEWAY_SERVICE_PORT:-8080}"
echo "3. Monitor with: docker-compose logs -f"

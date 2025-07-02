#!/bin/bash

# Setup script for Hotel Booking API microservices
# This script helps initialize the environment and set up the project

set -e

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Function to print colored output
print_status() {
    echo -e "${GREEN}[INFO]${NC} $1"
}

print_warning() {
    echo -e "${YELLOW}[WARNING]${NC} $1"
}

print_error() {
    echo -e "${RED}[ERROR]${NC} $1"
}

print_header() {
    echo -e "${BLUE}[SETUP]${NC} $1"
}

echo "🚀 Hotel Booking API - Project Setup"
echo "====================================="

# Check if Docker is installed
print_header "Checking prerequisites..."
if ! command -v docker &> /dev/null; then
    print_error "Docker is not installed. Please install Docker first."
    exit 1
fi

if ! command -v docker-compose &> /dev/null; then
    print_error "Docker Compose is not installed. Please install Docker Compose first."
    exit 1
fi

print_status "Docker and Docker Compose are available ✓"

# Check if .env file exists
print_header "Setting up environment configuration..."
if [ ! -f .env ]; then
    if [ -f .env.example ]; then
        cp .env.example .env
        print_status ".env file created from .env.example ✓"
        print_warning "IMPORTANT: Please review and update the .env file before proceeding!"
        echo ""
        echo "Key variables to update:"
        echo "- AWS_ACCESS_KEY_ID and AWS_SECRET_ACCESS_KEY (for S3 integration)"
        echo "- Database passwords (if you want to change defaults)"
        echo "- JWT_SECRET and INTERNAL_SECRET_KEY (for production)"
        echo ""
        read -p "Have you updated the .env file with your values? (y/N): " -n 1 -r
        echo
        if [[ ! $REPLY =~ ^[Yy]$ ]]; then
            print_warning "Please update the .env file and run this script again."
            exit 1
        fi
    else
        print_error ".env.example file not found!"
        exit 1
    fi
else
    print_status ".env file already exists ✓"
fi

# Validate critical environment variables
print_header "Validating environment configuration..."
source .env

# Check if critical variables are set to example values
if [ "$AWS_ACCESS_KEY_ID" = "YOUR_AWS_ACCESS_KEY_ID" ]; then
    print_warning "AWS_ACCESS_KEY_ID is still set to the example value"
    print_warning "S3 functionality will not work until you provide real AWS credentials"
fi

if [ "$AWS_SECRET_ACCESS_KEY" = "YOUR_AWS_SECRET_ACCESS_KEY" ]; then
    print_warning "AWS_SECRET_ACCESS_KEY is still set to the example value"
fi

if [ "$AWS_S3_BUCKET" = "your-s3-bucket-name" ]; then
    print_warning "AWS_S3_BUCKET is still set to the example value"
fi

print_status "Environment validation complete ✓"

# Create necessary directories if they don't exist
print_header "Creating project directories..."
mkdir -p logs
mkdir -p data/postgres
mkdir -p data/mongodb
mkdir -p data/redis
print_status "Project directories created ✓"

# Clean up any existing containers
print_header "Cleaning up existing containers..."
docker-compose down --remove-orphans 2>/dev/null || true
print_status "Cleanup complete ✓"

# Build common-model first
print_header "Building common-model dependency..."
cd common-model
mvn clean install -DskipTests
cd ..
print_status "Common-model built successfully ✓"

print_header "Setup complete!"
echo ""
echo "Next steps:"
echo "1. Run './build.sh' to build and start all services"
echo "2. Access the application at http://localhost:${GATEWAY_SERVICE_PORT:-8080}"
echo "3. Monitor services with 'docker-compose logs -f [service-name]'"
echo ""
echo "Available services will be:"
echo "- Gateway: http://localhost:${GATEWAY_SERVICE_PORT:-8080}"
echo "- Auth Service: http://localhost:${AUTH_SERVICE_PORT:-8087}"
echo "- Hotel Service: http://localhost:${HOTEL_SERVICE_PORT:-8084}"
echo "- Booking Service: http://localhost:${BOOKING_SERVICE_PORT:-8085}"
echo "- Comment Service: http://localhost:${COMMENT_SERVICE_PORT:-8081}"
echo "- Hotel Admin: http://localhost:${HOTEL_ADMIN_SERVICE_PORT:-8083}"
echo "- Notification: http://localhost:${NOTIFICATION_SERVICE_PORT:-8082}"
echo "- AI Agent: http://localhost:${AI_AGENT_SERVICE_PORT:-3001}"
echo "- Socket Service: http://localhost:${SOCKET_SERVICE_PORT:-3000}"
echo "- RabbitMQ Management: http://localhost:${RABBITMQ_MANAGEMENT_PORT:-15672}"
echo ""
print_status "Ready to build! Run './build.sh' to continue."

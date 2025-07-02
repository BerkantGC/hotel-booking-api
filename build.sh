#!/bin/bash

# Build script for Hotel Booking API microservices

set -e

echo "🏗️  Building Hotel Booking API microservices..."

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
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

# Check if .env file exists
if [ ! -f .env ]; then
    print_warning ".env file not found. Creating from .env.example..."
    if [ -f .env.example ]; then
        cp .env.example .env
        print_status ".env file created from .env.example"
        print_warning "Please review and update the .env file with your actual values before proceeding!"
        print_warning "Pay special attention to AWS credentials and database passwords."
        exit 1
    else
        print_error ".env.example file not found. Cannot create .env file."
        exit 1
    fi
fi

# Load environment variables
print_status "Loading environment variables from .env file..."
export $(grep -v '^#' .env | xargs)

# Build common-model first (required by other services)
print_status "Building common-model..."
cd common-model
mvn clean install -DskipTests
cd ..

# Build and start infrastructure services
print_status "Starting infrastructure services (PostgreSQL, MongoDB, RabbitMQ)..."
docker-compose up -d postgresql mongodb rabbitmq

# Wait for databases to be ready
print_status "Waiting for databases to be ready..."
sleep 10

# Build all services
print_status "Building all microservices..."
docker-compose build

# Start all services
print_status "Starting all services..."
docker-compose up -d

# Show running containers
print_status "Checking service status..."
docker-compose ps

print_status "✅ Build complete! Services are starting up..."
print_status "Gateway URL: http://localhost:${GATEWAY_SERVICE_PORT:-8080}"
print_status "RabbitMQ Management: http://localhost:${RABBITMQ_MANAGEMENT_PORT:-15672} (${RABBITMQ_DEFAULT_USER:-guest}/${RABBITMQ_DEFAULT_PASS:-guest})"
print_status ""
print_status "Service URLs:"
print_status "- Auth Service: http://localhost:${AUTH_SERVICE_PORT:-8087}"
print_status "- Hotel Service: http://localhost:${HOTEL_SERVICE_PORT:-8084}"
print_status "- Booking Service: http://localhost:${BOOKING_SERVICE_PORT:-8085}"
print_status "- Comment Service: http://localhost:${COMMENT_SERVICE_PORT:-8081}"
print_status "- Hotel Admin Service: http://localhost:${HOTEL_ADMIN_SERVICE_PORT:-8083}"
print_status "- Notification Service: http://localhost:${NOTIFICATION_SERVICE_PORT:-8082}"
print_status "- AI Agent Service: http://localhost:${AI_AGENT_SERVICE_PORT:-3001}"
print_status "- Socket Service: http://localhost:${SOCKET_SERVICE_PORT:-3000}"
print_status ""
print_status "To check logs: docker-compose logs -f [service-name]"
print_status "To stop all services: docker-compose down"

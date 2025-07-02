# Hotel Booking API - Docker Deployment

This document explains how to run the Hotel Booking API microservices using Docker and Docker Compose.

## Prerequisites

- Docker Engine 20.x or later
- Docker Compose 3.9 or later
- At least 4GB of available RAM
- At least 10GB of free disk space

## Architecture

The application consists of the following services:

### Infrastructure Services
- **PostgreSQL**: Main database for business data
- **MongoDB**: Document database for specific services
- **RabbitMQ**: Message broker for async communication

### Microservices
- **Gateway Service** (Port 8080): API Gateway and routing
- **Auth Service** (Port 8087): Authentication and authorization
- **Hotel Service** (Port 8084): Hotel management
- **Booking Service** (Port 8085): Booking management
- **Comment Service** (Port 8081): Review and comment management
- **Hotel Admin Service** (Port 8083): Administrative operations
- **Notification Service** (Port 8082): Notification handling
- **AI Agent Service** (Port 3001): AI-powered features
- **Socket Service** (Port 3000): Real-time communication

## Quick Start

### Option 1: Using the build script (Recommended)
```bash
./build.sh
```

### Option 2: Manual deployment
```bash
# 1. Build the common-model first
cd common-model
mvn clean install -DskipTests
cd ..

# 2. Start infrastructure services
docker-compose up -d postgresql mongodb rabbitmq

# 3. Wait for databases to initialize (about 30 seconds)
sleep 30

# 4. Build and start all services
docker-compose up -d
```

## Service URLs

- **API Gateway**: http://localhost:8080
- **RabbitMQ Management**: http://localhost:15672 (guest/guest)
- **PostgreSQL**: localhost:5432 (berkantgc/postgres123)
- **MongoDB**: localhost:27017 (root/123456)

## Environment Configuration

The following environment variables are configured in docker-compose.yaml:

### Database Configuration
- `SPRING_DATASOURCE_URL`: PostgreSQL connection string
- `SPRING_DATASOURCE_USERNAME`: Database username
- `SPRING_DATASOURCE_PASSWORD`: Database password

### Message Queue Configuration
- `SPRING_RABBITMQ_HOST`: RabbitMQ hostname
- `HOTEL_SERVICE_URL`: Internal service communication URL

## Useful Commands

### View logs
```bash
# All services
docker-compose logs -f

# Specific service
docker-compose logs -f gateway-service
docker-compose logs -f auth-service
```

### Check service status
```bash
docker-compose ps
```

### Restart a service
```bash
docker-compose restart gateway-service
```

### Stop all services
```bash
docker-compose down
```

### Remove all containers and volumes (clean start)
```bash
docker-compose down -v
docker system prune -f
```

### Scale a service
```bash
docker-compose up -d --scale hotel-service=3
```

## Development vs Production

### Development Mode
The current configuration is optimized for development with:
- Exposed database ports for direct access
- Volume mounts for Maven dependencies caching
- Health checks for service dependencies

### Production Considerations
For production deployment, consider:
- Using external managed databases
- Implementing proper secrets management
- Adding SSL/TLS termination
- Setting up monitoring and logging
- Configuring resource limits
- Using container orchestration (Kubernetes)

## Troubleshooting

### Common Issues

1. **Port conflicts**: If ports are already in use, modify the port mappings in docker-compose.yaml
2. **Memory issues**: Ensure Docker has enough memory allocated (4GB minimum)
3. **Database connection errors**: Wait for infrastructure services to fully start before deploying application services

### Service Dependencies
Services start in the following order:
1. Infrastructure (PostgreSQL, MongoDB, RabbitMQ)
2. Common Model (builds shared library)
3. Core services (Auth, Hotel)
4. Dependent services (Booking, Comment, etc.)
5. Gateway service (last, depends on all others)

### Debugging
```bash
# Enter a running container
docker-compose exec gateway-service /bin/bash

# View container resource usage
docker stats

# Inspect container configuration
docker-compose config
```

## Security Notes

- Default passwords are used for development
- Change all default credentials before production deployment
- Consider using Docker secrets for sensitive data
- Implement proper network segmentation
- Use HTTPS in production

## Contributing

When adding new services:
1. Create a Dockerfile in the service directory
2. Add the service to docker-compose.yaml
3. Update this README with the new service information
4. Test the complete deployment

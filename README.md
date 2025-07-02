# Hotel Booking API

A comprehensive microservices-based hotel booking system built with Spring Boot and Node.js, featuring a complete hotel management, booking, and notification system.

## 🏗️ Architecture Overview

This project follows a microservices architecture with the following services:

### Core Services
- **Gateway Service** (Port 8080) - API Gateway and routing
- **Auth Service** (Port 8087) - Authentication and user management
- **Hotel Service** (Port 8084) - Hotel management and inventory
- **Booking Service** (Port 8085) - Booking management and processing
- **Comment Service** (Port 8081) - Reviews and comments system
- **Hotel Admin Service** (Port 8083) - Administrative operations
- **Notification Service** (Port 8082) - Messaging and notifications

### Supporting Services
- **AI Agent Service** (Port 3001) - AI-powered assistance (Node.js)
- **Socket Service** (Port 3000) - Real-time communication (Node.js)
- **Common Model** - Shared data models and utilities

### Infrastructure
- **PostgreSQL** - Primary database for most services
- **MongoDB** - Document database for comments and reviews
- **Redis** - Caching and session management
- **RabbitMQ** - Message queue for async communication

## 🚀 Quick Start

### Prerequisites
- Docker and Docker Compose
- Java 11 or higher (for local development)
- Node.js 16+ (for Node.js services)
- Maven 3.6+ (for local development)

### Using Docker (Recommended)

1. **Clone the repository:**
   ```bash
   git clone <repository-url>
   cd HotelBookingAPI
   ```

2. **Set up environment variables:**
   ```bash
   cp .env.example .env
   # Edit .env file with your configuration
   ```

3. **Build and start all services:**
   ```bash
   # Make scripts executable
   chmod +x build.sh setup.sh validate-env.sh

   # Validate environment setup
   ./validate-env.sh

   # Build and start services
   ./build.sh
   ```

4. **Access the services:**
   - API Gateway: http://localhost:8080
   - RabbitMQ Management: http://localhost:15672 (guest/guest)

### Manual Docker Commands

```bash
# Build all services
docker-compose build

# Start infrastructure services first
docker-compose up -d postgresql mongodb rabbitmq redis

# Start all services
docker-compose up -d

# View logs
docker-compose logs -f [service-name]

# Stop all services
docker-compose down
```

## 📋 Environment Configuration

### Required Environment Variables

Create a `.env` file in the root directory with the following variables:

```bash
# Database Configuration
POSTGRES_DB=hoteldb
POSTGRES_USER=your_username
POSTGRES_PASSWORD=your_password
MONGO_INITDB_ROOT_USERNAME=root
MONGO_INITDB_ROOT_PASSWORD=your_mongo_password

# Security
JWT_SECRET=your_jwt_secret_key
INTERNAL_SECRET_KEY=your_internal_secret

# AWS Configuration (for file uploads)
AWS_ACCESS_KEY_ID=your_aws_access_key
AWS_SECRET_ACCESS_KEY=your_aws_secret_key
AWS_REGION=your_aws_region
AWS_S3_BUCKET=your_s3_bucket

# Service Ports (optional - defaults provided)
GATEWAY_SERVICE_PORT=8080
AUTH_SERVICE_PORT=8087
HOTEL_SERVICE_PORT=8084
BOOKING_SERVICE_PORT=8085
COMMENT_SERVICE_PORT=8081
```

See `ENV_README.md` for complete environment variable documentation.

## 🛠️ Development

### Local Development Setup

1. **Start infrastructure services:**
   ```bash
   docker-compose up -d postgresql mongodb rabbitmq redis
   ```

2. **Build common model:**
   ```bash
   cd common-model
   mvn clean install
   cd ..
   ```

3. **Start individual services:**
   ```bash
   # Example: Start auth service
   cd auth-service
   mvn spring-boot:run
   ```

### Building Services

```bash
# Build all Java services
./build.sh

# Build individual service
cd [service-name]
mvn clean package
```

### Running Tests

```bash
# Run tests for all services
mvn test

# Run tests for specific service
cd [service-name]
mvn test
```

![bookings](https://github.com/user-attachments/assets/891b5825-c554-4eb8-b8e7-ffbfe6afb00c)


## 📖 API Documentation
### Main API Endpoints (via Gateway - Port 8080)

```bash
# Authentication
POST /api/auth/register
POST /api/auth/login
POST /api/auth/refresh

# Hotels
GET /api/hotels
GET /api/hotels/{id}
POST /api/hotels
PUT /api/hotels/{id}

# Bookings
GET /api/bookings
POST /api/bookings
GET /api/bookings/{id}
PUT /api/bookings/{id}

# Comments
GET /api/comments/hotel/{hotelId}
POST /api/comments
PUT /api/comments/{id}
DELETE /api/comments/{id}
```

## 🏢 Service Details

### Auth Service
- User registration and authentication
- JWT token management
- User profile management
- Role-based access control

### Hotel Service
- Hotel CRUD operations
- Room management
- Availability checking
- Hotel search and filtering

### Booking Service
- Booking creation and management
- Payment processing integration
- Booking status tracking
- Integration with hotel availability

### Comment Service
- Hotel reviews and ratings
- Comment moderation
- User feedback management

### Notification Service
- Email notifications
- SMS notifications (if configured)
- Real-time notifications via WebSocket
- Notification templates

### AI Agent Service
- AI-powered customer support
- Booking assistance
- Recommendation engine

## 🔧 Configuration

### Service Communication
Services communicate via:
- **Synchronous**: REST APIs with internal authentication
- **Asynchronous**: RabbitMQ message queues
- **Real-time**: WebSocket connections

### Security
- JWT-based authentication
- Internal service authentication with shared secrets
- HTTPS support (configure in production)
- Rate limiting (configure as needed)

### Monitoring and Health Checks
- Spring Boot Actuator endpoints
- Docker health checks
- Service dependency management

## 🗄️ Database Schema

### PostgreSQL (Primary Database)
Used by: Auth, Hotel, Booking, Admin, Notification services

### MongoDB (Document Database)
Used by: Comment service for reviews and ratings

### Redis (Cache)
Used by: Hotel service for caching frequently accessed data

## 📦 Deployment

### Production Deployment

1. **Configure production environment:**
   ```bash
   cp .env.example .env.production
   # Update with production values
   ```

2. **Build production images:**
   ```bash
   docker-compose -f docker-compose.yml -f docker-compose.prod.yml build
   ```

3. **Deploy with production configuration:**
   ```bash
   docker-compose -f docker-compose.yml -f docker-compose.prod.yml up -d
   ```

### Scaling Services

```bash
# Scale specific services
docker-compose up -d --scale hotel-service=3 --scale booking-service=2
```

## 🧪 Testing

### Integration Tests
```bash
# Run integration tests
mvn verify -P integration-tests
```

### Load Testing
```bash
# Example with Apache Bench
ab -n 1000 -c 10 http://localhost:8080/api/hotels
```

## 📊 Monitoring

### Application Monitoring
- Spring Boot Actuator metrics
- Custom application metrics
- Health check endpoints

### Infrastructure Monitoring
- Docker container metrics
- Database performance monitoring
- Message queue monitoring

## 🔍 Troubleshooting

### Common Issues

1. **Service startup failures:**
   ```bash
   # Check service logs
   docker-compose logs [service-name]
   
   # Verify environment variables
   ./validate-env.sh
   ```

2. **Database connection issues:**
   ```bash
   # Check database containers
   docker-compose ps postgresql mongodb
   
   # Test database connectivity
   docker-compose exec postgresql pg_isready
   ```

3. **Service communication issues:**
   ```bash
   # Check network connectivity
   docker network ls
   docker network inspect hotelbookingapi_default
   ```

### Debug Mode
```bash
# Start services in debug mode
SPRING_PROFILES_ACTIVE=debug docker-compose up
```

## 🤝 Contributing

1. Fork the repository
2. Create a feature branch (`git checkout -b feature/amazing-feature`)
3. Commit your changes (`git commit -m 'Add some amazing feature'`)
4. Push to the branch (`git push origin feature/amazing-feature`)
5. Open a Pull Request

### Code Style
- Follow Java coding conventions
- Use meaningful variable and method names
- Add appropriate comments and documentation
- Write unit tests for new features

## 📄 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## 📞 Support

For support and questions:
- Create an issue in the repository
- Check the documentation in individual service directories
- Review the troubleshooting section above

## 🗂️ Project Structure

```
HotelBookingAPI/
├── auth-service/          # Authentication service
├── hotel-service/         # Hotel management service
├── booking-service/       # Booking management service
├── comment-service/       # Reviews and comments service
├── hotel-admin-service/   # Administrative operations
├── notification-service/  # Notification system
├── gateway-service/       # API Gateway
├── ai-agent-service/      # AI assistance service (Node.js)
├── socket-service/        # Real-time communication (Node.js)
├── common-model/          # Shared models and utilities
├── docker-compose.yaml    # Docker services configuration
├── .env                   # Environment variables
├── build.sh              # Build script
├── setup.sh              # Setup script
├── validate-env.sh       # Environment validation
└── README.md             # This file
```

## 🔄 Version History

- **v1.0.0** - Initial release with core microservices
- **v1.1.0** - Added AI agent and real-time features
- **v1.2.0** - Enhanced security and monitoring

---

**Happy Coding! 🚀**

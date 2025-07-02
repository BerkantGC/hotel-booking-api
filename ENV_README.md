# Hotel Booking API - Environment Configuration

This project now uses environment variables for all configuration, making it easier to manage different environments (development, staging, production) and keeping sensitive information secure.

## Quick Start

1. **Initial Setup**:
   ```bash
   ./setup.sh
   ```

2. **Build and Run**:
   ```bash
   ./build.sh
   ```

## Environment Configuration

### Environment Files

- **`.env`** - Your actual environment configuration (do not commit to git)
- **`.env.example`** - Template with example values (safe to commit)

### Setting Up Environment Variables

1. **Copy the example file**:
   ```bash
   cp .env.example .env
   ```

2. **Edit the .env file** with your actual values:
   ```bash
   nano .env
   # or
   code .env
   ```

### Critical Variables to Update

#### AWS Configuration (Required for S3 functionality)
```env
AWS_ACCESS_KEY_ID=your_actual_access_key
AWS_SECRET_ACCESS_KEY=your_actual_secret_key
AWS_S3_BUCKET=your_actual_bucket_name
```

#### Security (Recommended for Production)
```env
JWT_SECRET=your_secure_jwt_secret_minimum_32_characters
INTERNAL_SECRET_KEY=your_secure_internal_secret_key
```

#### Database Passwords (Optional)
```env
POSTGRES_PASSWORD=your_secure_password
MONGO_INITDB_ROOT_PASSWORD=your_secure_password
```

## Service Configuration

### Database Services
- **PostgreSQL**: Port 5432, Database: `hoteldb`
- **MongoDB**: Port 27017, Database: `hotel_comments_db`
- **Redis**: Port 6379 (for caching)

### Application Services
- **Gateway Service**: Port 8080 (Main entry point)
- **Auth Service**: Port 8087 (Authentication & Users)
- **Hotel Service**: Port 8084 (Hotel management)
- **Booking Service**: Port 8085 (Reservations)
- **Comment Service**: Port 8081 (Reviews & Comments)
- **Hotel Admin Service**: Port 8083 (Admin operations)
- **Notification Service**: Port 8082 (Notifications)
- **AI Agent Service**: Port 3001 (AI chatbot)
- **Socket Service**: Port 3000 (Real-time communication)

### Infrastructure Services
- **RabbitMQ**: Port 5672 (AMQP), Port 15672 (Management UI)

## Environment Variables Reference

### Database Configuration
| Variable | Description | Default |
|----------|-------------|---------|
| `POSTGRES_DB` | PostgreSQL database name | `hoteldb` |
| `POSTGRES_USER` | PostgreSQL username | `berkantgc` |
| `POSTGRES_PASSWORD` | PostgreSQL password | `postgres123` |
| `MONGO_INITDB_ROOT_USERNAME` | MongoDB root username | `root` |
| `MONGO_INITDB_ROOT_PASSWORD` | MongoDB root password | `123456` |

### Service Ports
| Variable | Description | Default |
|----------|-------------|---------|
| `GATEWAY_SERVICE_PORT` | Gateway service port | `8080` |
| `AUTH_SERVICE_PORT` | Auth service port | `8087` |
| `HOTEL_SERVICE_PORT` | Hotel service port | `8084` |
| `BOOKING_SERVICE_PORT` | Booking service port | `8085` |
| `COMMENT_SERVICE_PORT` | Comment service port | `8081` |
| `HOTEL_ADMIN_SERVICE_PORT` | Hotel admin service port | `8083` |
| `NOTIFICATION_SERVICE_PORT` | Notification service port | `8082` |
| `AI_AGENT_SERVICE_PORT` | AI agent service port | `3001` |
| `SOCKET_SERVICE_PORT` | Socket service port | `3000` |

### Security Configuration
| Variable | Description | Required |
|----------|-------------|----------|
| `JWT_SECRET` | JWT signing secret | Yes |
| `INTERNAL_SECRET_KEY` | Internal service communication secret | Yes |
| `AWS_ACCESS_KEY_ID` | AWS access key for S3 | Yes (for S3 features) |
| `AWS_SECRET_ACCESS_KEY` | AWS secret key for S3 | Yes (for S3 features) |

## Development vs Production

### Development
- Use the default values from `.env.example`
- Database passwords can be simple
- AWS credentials can be test/sandbox accounts

### Production
- **Change all default passwords**
- **Use strong, unique JWT and internal secrets**
- **Use production AWS credentials**
- **Consider using Docker secrets or external secret management**

## Docker Compose Integration

The `docker-compose.yaml` file automatically loads variables from the `.env` file. All services are configured to use these environment variables, ensuring consistency across the entire application stack.

## Troubleshooting

### Common Issues

1. **Services fail to start**:
   - Check if `.env` file exists and has correct values
   - Verify ports are not already in use
   - Check Docker logs: `docker-compose logs [service-name]`

2. **Database connection errors**:
   - Ensure database credentials match in `.env`
   - Wait for databases to fully initialize (health checks help)

3. **S3 functionality not working**:
   - Verify AWS credentials are correct in `.env`
   - Check AWS region and bucket name
   - Ensure S3 bucket exists and has proper permissions

### Useful Commands

```bash
# Check service status
docker-compose ps

# View logs for all services
docker-compose logs -f

# View logs for specific service
docker-compose logs -f auth-service

# Restart a specific service
docker-compose restart hotel-service

# Stop all services
docker-compose down

# Rebuild and restart
docker-compose up --build -d
```

## Security Best Practices

1. **Never commit `.env` files** to version control
2. **Use strong passwords** for production databases
3. **Rotate secrets regularly** in production
4. **Use environment-specific configurations**
5. **Consider using Docker secrets** for sensitive data in production
6. **Limit AWS IAM permissions** to minimum required for S3 operations

## Contributing

When adding new environment variables:
1. Add them to `.env.example` with safe default values
2. Update this README with documentation
3. Update `docker-compose.yaml` if needed
4. Test with both default and custom values

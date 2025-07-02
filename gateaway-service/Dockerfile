# Multi-stage build
FROM eclipse-temurin:21-jdk-alpine AS builder

# Install Maven
RUN apk add --no-cache maven

# Set working directory
WORKDIR /app

# Copy parent pom for dependency management
COPY pom.xml ./parent-pom.xml

# Copy pom.xml first for better layer caching
COPY gateaway-service/pom.xml .

# Copy common-model dependency
COPY common-model/target/common-model-1.0.0.jar /tmp/common-model-1.0.0.jar
RUN mvn install:install-file -Dfile=/tmp/common-model-1.0.0.jar -DgroupId=com.hotelbooking -DartifactId=common-model -Dversion=1.0.0 -Dpackaging=jar

# Download dependencies
RUN mvn dependency:go-offline -B

# Copy source code
COPY gateaway-service/src ./src

# Build the application
RUN mvn clean package -DskipTests

# Production stage
FROM eclipse-temurin:21-jre-alpine

# Set working directory
WORKDIR /app

# Copy the built JAR file
COPY --from=builder /app/target/*.jar app.jar

# Expose port
EXPOSE 8080

# Health check
HEALTHCHECK --interval=30s --timeout=3s --start-period=60s --retries=3 \
  CMD wget --no-verbose --tries=1 --spider http://localhost:8080/actuator/health || exit 1

# Run the application
ENTRYPOINT ["java", "-jar", "app.jar"]

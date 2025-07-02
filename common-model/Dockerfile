# Use Eclipse Temurin 21 as base image
FROM eclipse-temurin:21-jdk-alpine AS builder

# Install Maven
RUN apk add --no-cache maven

# Set working directory
WORKDIR /app

# Copy pom.xml first for better layer caching
COPY common-model/pom.xml .

# Download dependencies
RUN mvn dependency:go-offline -B

# Copy source code
COPY common-model/src ./src

# Build the application
RUN mvn clean install -DskipTests

# The common-model is a shared library, so we just need to build it
# Other services will copy the JAR from this stage
FROM alpine:latest
COPY --from=builder /app/target/*.jar /common-model.jar
# Since this is just a build stage, we'll use a simple command that exits successfully
CMD echo "Common model JAR built successfully" && exit 0

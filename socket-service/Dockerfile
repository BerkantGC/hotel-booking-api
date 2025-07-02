# Use Node.js 18 LTS as base image
FROM node:18-alpine

# Set working directory
WORKDIR /app

# Copy package.json and package-lock.json (if available)
COPY socket-service/package*.json ./

# Install dependencies
RUN npm ci --only=production

# Copy source code
COPY socket-service .

# Create a non-root user
RUN addgroup -g 1001 -S nodejs && \
    adduser -S -u 1001 nodejs

# Change ownership of the app directory to nodejs user
RUN chown -R nodejs:nodejs /app

# Switch to nodejs user
USER nodejs

# Expose port
EXPOSE 3000

# Health check
HEALTHCHECK --interval=30s --timeout=3s --start-period=30s --retries=3 \
  CMD wget --no-verbose --tries=1 --spider http://localhost:3000/health || exit 1

# Run the application
CMD ["npm", "start"]

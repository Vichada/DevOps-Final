# Base image with JDK 21 (Temurin official eclipse release)
FROM eclipse-temurin:21-jdk-noble

# Install Nginx, PHP-CLI, and SSH tools required by the exam rubric
RUN apt-get update && apt-get install -y \
    nginx \
    php-cli \
    openssh-server \
    curl \
    && rm -rf /var/lib/apt/lists/*

# Configure Nginx as a reverse proxy for Spring Boot (Port 8080)
RUN echo 'server { \
    listen 8080; \
    location / { \
        proxy_pass http://localhost:8081; \
        proxy_set_header Host $host; \
        proxy_set_header X-Real-IP $remote_addr; \
    } \
}' > /etc/nginx/sites-available/default

# Set up the working directory
WORKDIR /app

# Expose the ports explicitly requested by the professor
EXPOSE 8443 2222

# Start Nginx and run the Spring Boot application on port 8081 (proxied to 8080)
CMD service nginx start && ./mvnw spring-boot:run -Dspring-boot.run.arguments=--server.port=8081

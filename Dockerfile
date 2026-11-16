# syntax=docker/dockerfile:1

# Stage 1: Build the multi-module application
FROM eclipse-temurin:21-jdk-alpine AS builder
WORKDIR /workspace

# Install bash for maven wrapper compatibility
RUN apk add --no-cache bash

# Copy build configuration and wrappers
COPY .mvn/ .mvn/
COPY mvnw pom.xml ./
COPY config/ config/
COPY tetris-core/pom.xml tetris-core/
COPY tetris-app/pom.xml tetris-app/

# Cache dependencies
RUN ./mvnw dependency:go-offline -B || true

# Copy source trees and package the application
COPY tetris-core/src tetris-core/src
COPY tetris-app/src tetris-app/src
RUN ./mvnw clean package -DskipTests -B

# Stage 2: Minimal non-root runtime environment
FROM eclipse-temurin:21-jre-alpine AS runner
WORKDIR /app

# Install curl for container healthcheck probe
RUN apk add --no-cache curl

# Security: Create non-root dedicated user and group
RUN addgroup -S tetris && adduser -S tetris -G tetris

# Copy executable jar from builder stage
COPY --from=builder --chown=tetris:tetris /workspace/tetris-app/target/tetris-app.jar app.jar

# Run with least-privilege user
USER tetris:tetris

# Expose HTTP port
EXPOSE 8080

# Production JVM flags for container environments
ENV JAVA_OPTS="-XX:+UseContainerSupport -XX:MaxRAMPercentage=75.0 -Djava.security.egd=file:/dev/./urandom"

# Health check against Actuator health endpoint
HEALTHCHECK --interval=30s --timeout=3s --start-period=30s --retries=3 \
  CMD curl -f http://localhost:8080/actuator/health || exit 1

ENTRYPOINT ["sh", "-c", "exec java $JAVA_OPTS -jar app.jar"]

# Multi-stage build for blazing fast startup and minimal footprint
# Stage 1: Build stage
FROM maven:3.9.9-eclipse-temurin-21-alpine AS builder

# Update CA certificates for HTTPS access
RUN apk update && \
    apk add --no-cache ca-certificates curl

WORKDIR /build

# Copy pom.xml and download dependencies (cached layer)
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Copy source code and build
COPY src ./src
RUN mvn clean package -DskipTests -B && \
    mv target/alcohol-shop-*.jar app.jar

# Stage 2: Runtime stage - using lightweight JRE Alpine
FROM eclipse-temurin:21-jre-alpine

# Update apk index and install dumb-init and curl
RUN apk update --no-cache && \
    apk add --no-cache ca-certificates curl dumb-init

# Create non-root user for security
RUN addgroup -S spring && adduser -S spring -G spring

WORKDIR /app

# Copy jar from builder
COPY --from=builder /build/app.jar /app/app.jar

# Change ownership
RUN chown -R spring:spring /app

# Switch to non-root user
USER spring

# Health check
HEALTHCHECK --interval=30s --timeout=3s --start-period=40s --retries=3 \
    CMD curl -f http://localhost:8080/actuator/health || exit 1

# Use dumb-init to handle signals properly
ENTRYPOINT ["dumb-init", "--"]

# JVM optimizations for containerized environments
# -Xshare:off - disable CDS (Class Data Sharing) for faster startup
# -XX:+UseContainerSupport - respect container limits
# -XX:MaxRAMPercentage=75.0 - use 75% of container memory for heap
# -XX:+UseG1GC - G1 garbage collector for predictable pauses
ENV JAVA_OPTS="-Xshare:off -XX:+UseContainerSupport -XX:MaxRAMPercentage=75.0 -XX:+UseG1GC"

EXPOSE 8080

CMD ["sh", "-c", "java $JAVA_OPTS -jar /app/app.jar"]

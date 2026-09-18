# Multi-stage Dockerfile for auth-service
# Stage 1: Build with Maven (optimized caching)
FROM maven:3.9.9-eclipse-temurin-17 AS builder

WORKDIR /app

# Copy only pom.xml files first (cacheable layer)
COPY pom.xml .
COPY infrastructure/config-service/pom.xml infrastructure/config-service/
COPY services/auth-service/pom.xml services/auth-service/

# Pre-download dependencies (cache layer, reused if pom.xml unchanged)
RUN mvn -f /app/pom.xml dependency:resolve -pl services/auth-service -am -q || true

# Copy source code AFTER dependencies
COPY services/auth-service/src services/auth-service/src

# Build
RUN mvn -f /app/pom.xml clean package -pl services/auth-service -am -DskipTests -q

# Stage 2: Runtime
FROM eclipse-temurin:17-jre

WORKDIR /app

COPY --from=builder /app/services/auth-service/target/auth-service-*.jar auth-service.jar

EXPOSE 8081

ENV JAVA_OPTS=""

HEALTHCHECK --interval=30s --timeout=10s --start-period=40s --retries=3 \
  CMD wget -qO- http://localhost:8081/actuator/health >/dev/null 2>&1 || exit 1

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar auth-service.jar"]
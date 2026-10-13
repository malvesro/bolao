# Stage 1: Build
FROM eclipse-temurin:25-jdk-noble AS builder

WORKDIR /app

# Copy Maven wrapper and pom first for dependency caching
COPY .mvn/ .mvn/
COPY mvnw pom.xml ./
RUN ./mvnw dependency:go-offline -q

# Copy source and build
COPY src/ src/
RUN ./mvnw package -DskipTests -q

# Stage 2: Runtime
FROM eclipse-temurin:25-jre-noble

WORKDIR /app

COPY --from=builder /app/target/*.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]

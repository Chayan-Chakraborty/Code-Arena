# ==========================================
# Build Stage
# ==========================================
FROM maven:3.9.9-eclipse-temurin-21 AS build

WORKDIR /app

# Copy pom first for dependency caching
COPY pom.xml .

RUN mvn -q -DskipTests dependency:go-offline

# Copy source code
COPY src ./src

# Build application
RUN mvn -q clean package -DskipTests

# ==========================================
# Runtime Stage
# ==========================================
FROM eclipse-temurin:21-jdk

WORKDIR /app

COPY --from=build /app/target/code-arena-backend-1.0.0.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]
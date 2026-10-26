# Stage 1: Build the application using Maven
FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /app

# Copy pom.xml and download dependencies (caches dependencies for faster builds)
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Copy source code and build the JAR package
COPY src ./src
RUN mvn clean package -DskipTests

# Stage 2: Run the application using a lightweight JRE image
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app

# Copy the built JAR from the build stage
COPY --from=build /app/target/*.jar app.jar

# Expose port 8080 (Docker Compose maps this to 8081 and 8082 for multi-instance testing)
EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]
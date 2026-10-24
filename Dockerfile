# ================================
# Stage 1: Build the Spring Boot Application
# ================================

# Use Eclipse Temurin JDK 17 for compilation
FROM eclipse-temurin:17-jdk AS builder

# Set the working directory inside the container
WORKDIR /app

# Copy the Maven wrapper script
COPY mvnw .

# Copy the Maven wrapper configuration directory
COPY .mvn .mvn

# Copy the Maven project configuration file
COPY pom.xml .

# Copy the application source code
COPY src src

# Make the Maven wrapper executable
RUN chmod +x mvnw

# Build the executable Spring Boot JAR
# Skip tests during the Docker build
RUN ./mvnw clean package -DskipTests


# ================================
# Stage 2: Create the Runtime Image
# ================================

# Use Eclipse Temurin JRE 17 to run the application
FROM eclipse-temurin:17-jre

# Set the working directory inside the runtime container
WORKDIR /app

# Copy the generated JAR from the builder stage
# Rename the JAR to app.jar
COPY --from=builder /app/target/*.jar app.jar

# Document the application's default port
EXPOSE 8081

# ================================
# Start the Spring Boot Application
# ================================

# Use Render's PORT environment variable when available
# Default to port 8081 if PORT is not set
ENTRYPOINT ["sh", "-c", "java -Dserver.port=${PORT:-8081} -jar app.jar"]

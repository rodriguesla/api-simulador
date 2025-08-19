# Use official OpenJDK image
FROM eclipse-temurin:21-jre

# Set working directory
WORKDIR /app

# Copy built jar from target folder
COPY target/api-simulador-0.0.1-SNAPSHOT.jar app.jar

# Expose port
EXPOSE 8080

# Run the application
ENTRYPOINT ["java", "-jar", "app.jar"]


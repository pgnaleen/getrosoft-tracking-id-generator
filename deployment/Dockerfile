# Use a lightweight OpenJDK base image
FROM openjdk

# Set the working directory inside the container
WORKDIR /app

# Copy the JAR file into the container
COPY getrosoft-generate-id-0.0.1-SNAPSHOT.jar app.jar

# Expose the application port (change if needed)
EXPOSE 8080

# Run the Spring Boot application
ENTRYPOINT ["java", "-Xms256m", "-Xmx256m", "-jar", "app.jar"]
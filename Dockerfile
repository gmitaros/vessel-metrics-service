# Use an official OpenJDK runtime as a parent image
FROM openjdk:21-jdk-slim

# Set the working directory inside the container
WORKDIR /app

# Copy the Maven built .jar file to the container
COPY target/vessel-metrics-service-*.jar /app/vessel-metrics-service.jar
COPY src/main/resources/application.properties /app/config/application.properties

# Expose the application port
EXPOSE 8080

# Run the jar file
ENTRYPOINT ["java", "-jar", "/app/vessel-metrics-service.jar", "--spring.config.location=file:/app/config/application.properties"]

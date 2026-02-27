FROM openjdk:17-jdk-slim
WORKDIR /app
COPY target/file-storage-service-1.0-SNAPSHOT.jar app.jar
EXPOSE 8084
ENTRYPOINT ["java", "-jar", "app.jar"]
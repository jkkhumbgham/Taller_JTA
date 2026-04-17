FROM eclipse-temurin:21
WORKDIR /app
COPY target/correo-consumer-1.0-SNAPSHOT.jar app.jar
COPY target/dependency/ dependency/
CMD ["java", "-cp", "app.jar:dependency/*", "org.eclipse.jakarta.consumer.CorreoConsumer"]
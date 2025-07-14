FROM openjdk:21-jdk-slim

COPY build/libs/app.jar /app/app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "/app/app.jar"]

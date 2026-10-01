FROM mcr.microsoft.com/playwright/java:v1.40.0-jammy
WORKDIR /app

COPY target/ship-0.0.1-SNAPSHOT.jar app.jar

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]

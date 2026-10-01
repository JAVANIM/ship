FROM mcr.microsoft.com/playwright/java:v1.40.0-jammy AS build
WORKDIR /app
COPY . .
RUN mvn clean package -DskipTests

FROM mcr.microsoft.com/playwright/java:v1.40.0-jammy
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]

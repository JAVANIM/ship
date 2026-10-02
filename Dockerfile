# 1단계: 빌드 스테이지
FROM maven:3.9.6-eclipse-temurin-21 AS builder
WORKDIR /build
COPY pom.xml .
COPY src ./src
RUN mvn clean package -DskipTests

# 2단계: 실행 스테이지 (Playwright 공식 Java 이미지 사용)
FROM mcr.microsoft.com/playwright/java:v1.40.0-jammy

WORKDIR /app
COPY --from=builder /build/target/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]

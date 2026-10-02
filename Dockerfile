# 1단계: 빌드 스테이지 (Maven으로 jar 파일 생성)
FROM maven:3.9.6-eclipse-temurin-17 AS builder
WORKDIR /build

COPY pom.xml .
COPY src ./src

RUN mvn clean package -DskipTests

# 2단계: 실행 스테이지 (Playwright 공식 이미지 사용 - Node.js 및 브라우저 드라이버 완벽 내장)
FROM mcr.microsoft.com/playwright/java:v1.40.0-jammy
WORKDIR /app

COPY --from=builder /build/target/*.jar app.jar

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]

# 1단계: 빌드 스테이지
FROM maven:3.9.6-eclipse-temurin-21 AS builder
WORKDIR /build
COPY pom.xml .
COPY src ./src
RUN mvn clean package -DskipTests

# 2단계: 실행 스테이지
FROM eclipse-temurin:21-jre-jammy

# Playwright 브라우저 실행에 필요한 시스템 라이브러리 (존재하지 않는 패키지 제외 완료)
RUN apt-get update && apt-get install -y \
    libglib2.0-0 \
    libnss3 \
    libnspr4 \
    libdbus-1-3 \
    libatk1.0-0 \
    libatk-bridge2.0-0 \
    libcups2 \
    libdrm2 \
    libxcb1 \
    libxkbcommon0 \
    libatspi2.0-0 \
    libx11-6 \
    libxcomposite1 \
    libxdamage1 \
    libxext6 \
    libxfixes3 \
    libxrandr2 \
    libgbm1 \
    libpango-1.0-0 \
    libcairo2 \
    libasound2 \
    libasound2-plugins \
    libxi6 \
    libxtst6 \
    libx11-xcb1 \
    libxss1 \
    libgconf-2-4 \
    libgstreamer1.0-0 \
    libgstreamer-plugins-base1.0-0 \
    fonts-liberation \
    libu2f-udev \
    libvulkan1 \
    && rm -rf /var/lib/apt/lists/*

WORKDIR /app
COPY --from=builder /build/target/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]

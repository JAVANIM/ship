
# 1단계: 빌드 스테이지
FROM maven:3.9.6-eclipse-temurin-21 AS builder
WORKDIR /build
COPY pom.xml .
COPY src ./src
RUN mvn clean package -DskipTests

# 2단계: 실행 스테이지
FROM maven:3.9.6-eclipse-temurin-21-jre-jammy
# (※ 브라우저를 다운로드받는 명령어를 실행하기 위해 Maven/Java 런타임이 포함된 이미지를 쓰는 것이 안전합니다)

# Playwright 브라우저 실행에 필요한 시스템 라이브러리 설치
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
    && rm -rf /var/lib/apt/lists/*

WORKDIR /app
COPY --from=builder /build/target/*.jar app.jar

# 🔥 [핵심] 컨테이너 빌드 시점에 Playwright 브라우저 강제 다운로드 설치
RUN mvn exec:java -Dexec.mainClass=com.microsoft.playwright.CLI -Dexec.args="install"

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]

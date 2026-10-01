# 1단계: 빌드 스테이지 (Maven과 Java가 설치된 환경에서 소스코드를 직접 빌드)
FROM maven:3.9.6-eclipse-temurin-17 AS builder
WORKDIR /build

# pom.xml과 소스 코드를 복사
COPY pom.xml .
COPY src ./src

# 외부 서버 메모리 부하를 줄이기 위해 데몬 빌드 수행
RUN mvn clean package -DskipTests

# 2단계: 실행 스테이지 (Playwright와 가벼운 자바 런타임 환경)
FROM mcr.microsoft.com/playwright/java:v1.40.0-jammy
WORKDIR /app

# 1단계 빌드 결과물(target 폴더 안의 jar)만 쏙 복사해옴
COPY --from=builder /build/target/*.jar app.jar

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]

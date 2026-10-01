# 1단계: 빌드 스테이지 (Maven과 Java로 프로젝트를 직접 빌드)
FROM mcr.microsoft.com/playwright/java:v1.40.0-jammy AS build
WORKDIR /app
COPY . .
RUN chmod +x mvnw

RUN ./mvnw clean package -DskipTests

# 2단계: 실행 스테이지 (가벼운 실행 환경에 빌드된 파일만 쏙 가져옴)
FROM mcr.microsoft.com/playwright/java:v1.40.0-jammy
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]

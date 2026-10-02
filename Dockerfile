# 1단계: 빌드 환경 (Java 21 및 Maven 기본 설치 버전 사용)
FROM eclipse-temurin:21-jdk-alpine AS build
WORKDIR /app

# Maven 패키지 매니저 직접 설치
RUN apk add --no-cache maven

COPY . .
RUN mvn clean package -DskipTests

# 2단계: 실행 환경 (Java 21 런타임)
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]

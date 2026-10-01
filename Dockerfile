# 1단계: 빌드 스테이지 (도커 환경에 기본 탑재된 mvn 명령어 사용)
FROM mcr.microsoft.com/playwright/java:v1.40.0-jammy AS build
WORKDIR /app
COPY . .

# 복잡한 래퍼(mvnw)나 숨김 폴더(.mvn) 없이 일반 mvn으로 깔끔하게 빌드
RUN mvn clean package -DskipTests

# 2단계: 실행 스테이지 (가벼운 실행 환경에 빌드된 파일만 쏙 가져옴)
FROM mcr.microsoft.com/playwright/java:v1.40.0-jammy
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]

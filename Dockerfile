FROM mcr.microsoft.com/playwright/java:v1.40.0-jammy
WORKDIR /app

# 서버에서 빌드 안 함! 내 컴퓨터에서 만든 jar 파일을 그대로 복사해서 실행
COPY target/ship-0.0.1-SNAPSHOT.jar app.jar

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]

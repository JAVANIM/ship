FROM mcr.microsoft.com/playwright/java:v1.40.0-jammy
WORKDIR /app

RUN apt-get update && apt-get install -y curl && rm -rf /var/lib/apt/lists/*

# 구글 드라이브 대용량 파일 경고를 무시하고 순수 jar 파일만 다이렉트로 강제 다운로드
RUN curl -L "https://drive.google.com/uc?export=download&confirm=t&id=1GhTnvTkwfgRw121Vucmj9ZVeBqrEthlN" -o app.jar

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]

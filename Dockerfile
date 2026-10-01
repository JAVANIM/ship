FROM mcr.microsoft.com/playwright/java:v1.40.0-jammy
WORKDIR /app

# 다운로드 도구(curl) 설치
RUN apt-get update && apt-get install -y curl && rm -rf /var/lib/apt/lists/*

# 구글 드라이브 공유 파일 다운로드 (대용량 파일 경고 우회 쿠키 처리 포함)
RUN CONFIRM=$(curl -s -c /tmp/cookie "https://drive.google.com/uc?export=download&id=1GhTnvTkwfgRw121Vucmj9ZVeBqrEthlN" | grep -o 'confirm=[0-9A-Za-z_]*' | head -1) && \
    curl -Lb /tmp/cookie "https://drive.google.com/uc?export=download&confirm=${CONFIRM}&id=1GhTnvTkwfgRw121Vucmj9ZVeBqrEthlN" -o app.jar

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]

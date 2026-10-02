# 2단계: 실행 스테이지
FROM eclipse-temurin:21-jre-jammy

# Playwright 브라우저 실행에 필요한 시스템 라이브러리 (Webkit 필수 패키지 포함)
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
    libgtk-3-0 \
    libxcursor1 \
    libpangocairo-1.0-0 \
    libcairo-gobject2 \
    libgdk-pixbuf-2.0-0 \
    # === [여기서부터 Webkit 구동용 추가 라이브러리] ===
    libatomic1 \
    libxslt1.1 \
    libwoff2dec1 \
    libvpx7 \
    libevent-2.1-7 \
    libwebpdemux2 \
    libharfbuzz-icu0 \
    libenchant-2-2 \
    libsecret-1-0 \
    libhyphen0 \
    libmanette-0.2-0 \
    libgles2-mesa \
    libevent-core-2.1-7 \
    # ===============================================
    && rm -rf /var/lib/apt/lists/*

WORKDIR /app
COPY --from=builder /build/target/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]

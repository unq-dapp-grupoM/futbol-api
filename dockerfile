# --- Fase de Construcción (Build Stage) ---
FROM gradle:8.8-jdk21-jammy AS build

WORKDIR /app

# Copiamos archivos de configuración primero
COPY build.gradle settings.gradle gradlew ./
COPY gradle ./gradle
RUN chmod +x gradlew

# Descargamos dependencias (sin código aún, para cache)
RUN ./gradlew build -x test --no-daemon --stacktrace || true

# Copiamos el código fuente
COPY src ./src

# Construimos el JAR
RUN ./gradlew build -x test --no-daemon

# --- Fase Final (Runtime Stage) ---
FROM eclipse-temurin:21-jdk-jammy

WORKDIR /app

# Instalamos dependencias necesarias para Chromium
RUN apt-get update && apt-get install -y \
    wget \
    unzip \
    libnss3 \
    libx11-6 \
    libxcomposite1 \
    libxcursor1 \
    libxdamage1 \
    libxi6 \
    libxtst6 \
    libglib2.0-0 \
    libxrandr2 \
    libasound2 \
    libpangocairo-1.0-0 \
    libatk1.0-0 \
    libcups2 \
    libdrm2 \
    libgbm1 \
    libpango-1.0-0 \
    libatk-bridge2.0-0 \
    libgtk-3-0 \
    && rm -rf /var/lib/apt/lists/*

# Copiamos el JAR desde la fase de build
COPY --from=build /app/build/libs/*.jar app.jar

# Instalamos navegadores Playwright usando la CLI incluida en el JAR
RUN java -cp app.jar com.microsoft.playwright.CLI install --with-deps

# Exponemos el puerto
EXPOSE 8080

# Ejecutamos Spring Boot
ENTRYPOINT ["java", "-jar", "app.jar"]

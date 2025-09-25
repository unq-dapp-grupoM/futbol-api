# --- Fase de Construcción (Build Stage) ---
# Usamos una imagen de Gradle con Java 21 para compilar el proyecto.
FROM gradle:8.8-jdk21-jammy AS build

# Establecemos el directorio de trabajo
WORKDIR /app

# Copiamos los archivos de configuración de Gradle y el wrapper
COPY build.gradle settings.gradle gradlew ./
COPY gradle ./gradle

# Damos permisos de ejecución al script de Gradle antes de usarlo
RUN chmod +x gradlew

# Usamos 'build' para descargar dependencias y generar la caché.
# El '|| true' al final asegura que el build no falle si hay tests que no pasan (ya que no hemos copiado el código fuente aún).
RUN ./gradlew build -x test --no-daemon --stacktrace || true

# Copiamos el resto del código fuente y construimos el JAR
COPY src ./src
RUN ./gradlew build -x test --no-daemon

# --- Fase Final (Final Stage) ---
# Usamos una imagen base de Debian con Java 21.
FROM eclipse-temurin:21-jdk-jammy

# Establecemos el directorio de trabajo
WORKDIR /app

# Instalar dependencias, Google Chrome y ChromeDriver en una sola capa para optimizar
RUN apt-get update && apt-get install -y \
    wget \
    unzip \
    --no-install-recommends && \
    # Instalar Google Chrome
    wget -q https://dl.google.com/linux/direct/google-chrome-stable_current_amd64.deb && \
    apt-get install -y ./google-chrome-stable_current_amd64.deb && \
    rm google-chrome-stable_current_amd64.deb && \
    # Instalar ChromeDriver (versión correspondiente a Chrome)
    # Chrome 125 -> ChromeDriver 125.0.6422.78
    wget -q https://storage.googleapis.com/chrome-for-testing-public/125.0.6422.78/linux64/chromedriver-linux64.zip && \
    unzip chromedriver-linux64.zip && \
    mv chromedriver-linux64/chromedriver /usr/bin/chromedriver && \
    chmod +x /usr/bin/chromedriver && \
    rm chromedriver-linux64.zip && \
    # Limpiamos la caché de apt al final
    rm -rf /var/lib/apt/lists/*

# Copiamos el JAR construido desde la fase de 'build'
COPY --from=build /app/build/libs/*.jar app.jar

# Exponemos el puerto en el que corre la aplicación Spring Boot (por defecto 8080)
EXPOSE 8080

# Comando para ejecutar la aplicación
# Le indicamos a Selenium que no use el sandbox de Chrome, lo cual es necesario en contenedores.
ENTRYPOINT ["java", "-Dwebdriver.chrome.args=--no-sandbox", "-jar", "app.jar"]

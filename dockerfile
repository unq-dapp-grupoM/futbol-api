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

# Copiamos el JAR construido desde la fase de 'build'
COPY --from=build /app/build/libs/*.jar app.jar

# Exponemos el puerto en el que corre la aplicación Spring Boot (por defecto 8080)
EXPOSE 8080

# Comando para ejecutar la aplicación
# Ya no necesitamos el argumento --no-sandbox porque no usamos Chrome.
ENTRYPOINT ["java", "-jar", "app.jar"]

# Usamos una imagen con JDK (no solo JRE) para poder compilar con Gradle.
FROM eclipse-temurin:17-jdk-alpine AS build

# Carpeta de trabajo dentro de la imagen de build.
WORKDIR /workspace

# Copia el repo al contenedor de build
COPY . .

# Esto limpia los \r para que el shell de Alpine lo ejecute bien.
RUN sed -i 's/\r$//' gradlew

# Damos permisos de ejecución al wrapper de Gradle.
RUN chmod +x ./gradlew

# compila el micro y genera el JAR ejecutable del módulo app-service.
RUN sh ./gradlew :app-service:bootJar --no-daemon


# Imagen más liviana con solo el JRE (no trae herramientas de compilación).
FROM eclipse-temurin:17-jre-alpine

# Creamos usuario/grupo no-root por seguridad.
RUN addgroup -S app && adduser -S app -G app

# Carpeta de trabajo de la app en runtime.
WORKDIR /app

# Traemos SOLO el jar generado desde la etapa de build (multi-stage). así la imagen final queda pequeña y sin artefactos de compilación.
COPY --from=build /workspace/applications/app-service/build/libs/authentication.jar /app/app.jar

# - Ajusta el uso de memoria al tamaño del contenedor.
# - Usa fuente de entropía rápida para acelerar el arranque.
ENV JAVA_OPTS="-XX:MaxRAMPercentage=70 -Djava.security.egd=file:/dev/./urandom"

# Documenta el puerto en el que la app escucha dentro del contenedor.
EXPOSE 8081

USER app

# Comando de arranque: ejecuta el jar con los JAVA_OPTS.
ENTRYPOINT ["sh","-c","java $JAVA_OPTS -jar /app/app.jar"]

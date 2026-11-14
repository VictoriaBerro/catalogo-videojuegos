# Imagen del "ambiente simulado de producción".
# La construye el pipeline a partir del .jar ya probado (no recompila).
FROM eclipse-temurin:21-jre

WORKDIR /app
COPY target/catalogo-videojuegos.jar app.jar

# La base H2 vive en /data, que se monta como volumen para que sobreviva a los redespliegues
ENV DATA_DIR=/data \
    APP_ENV=produccion
VOLUME /data

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app/app.jar"]

#!/usr/bin/env bash
# Despliega una imagen en el ambiente simulado de producción (un contenedor Docker).
# Uso: ./deploy/desplegar.sh <imagen:tag>
# La versión que muestra la app es el tag de la imagen.
set -euo pipefail

IMAGEN="${1:?Falta la imagen a desplegar (ej: catalogo-videojuegos:v12-a1b2c3d)}"
VERSION="${IMAGEN##*:}"
CONTENEDOR="${CONTENEDOR_PROD:-catalogo-prod}"
RED="${RED_DOCKER:-cicd}"
PUERTO="${PUERTO_PROD:-8081}"
VOLUMEN="${VOLUMEN_PROD:-catalogo-prod-data}"

echo ">> Desplegando ${IMAGEN} en '${CONTENEDOR}' (puerto ${PUERTO})"

docker network inspect "${RED}" >/dev/null 2>&1 || docker network create "${RED}"
docker rm -f "${CONTENEDOR}" >/dev/null 2>&1 || true

docker run -d \
  --name "${CONTENEDOR}" \
  --network "${RED}" \
  -p "${PUERTO}:8080" \
  -e APP_VERSION="${VERSION}" \
  -e APP_ENV=produccion \
  -v "${VOLUMEN}:/data" \
  --restart unless-stopped \
  "${IMAGEN}"

echo ">> Contenedor '${CONTENEDOR}' iniciado con la versión ${VERSION}"

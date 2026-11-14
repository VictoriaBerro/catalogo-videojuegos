#!/usr/bin/env bash
# Verifica que la app desplegada en producción responde.
# Espera hasta ~60 s a que /actuator/health devuelva UP y que la página principal cargue.
# Las respuestas se guardan en variables antes de buscar el texto: con "curl | grep -q"
# grep cierra el pipe al encontrar el texto, curl falla con código 23 y pipefail corta el script.
set -euo pipefail

URL="${URL_PROD:-http://catalogo-prod:8080}"
INTENTOS="${INTENTOS:-30}"

echo ">> Smoke test contra ${URL}"
for i in $(seq 1 "${INTENTOS}"); do
  salud="$(curl -fsS "${URL}/actuator/health" 2>/dev/null || true)"
  if [[ "${salud}" == *'"UP"'* ]]; then
    echo ">> Health UP (intento ${i})"

    pagina="$(curl -fsS "${URL}/")"
    if [[ "${pagina}" != *"Catálogo de Videojuegos"* ]]; then
      echo "!! La página principal no muestra el catálogo" >&2
      exit 1
    fi

    curl -fsS "${URL}/api/generos" >/dev/null
    echo ">> Smoke test OK"
    exit 0
  fi
  sleep 2
done

echo "!! La aplicación no respondió a tiempo en ${URL}" >&2
exit 1

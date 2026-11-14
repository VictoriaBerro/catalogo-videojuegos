#!/usr/bin/env bash
# Verifica que la app desplegada en producción responde.
# Espera hasta ~60 s a que /actuator/health devuelva UP y que la página principal cargue.
set -euo pipefail

URL="${URL_PROD:-http://catalogo-prod:8080}"
INTENTOS="${INTENTOS:-30}"

echo ">> Smoke test contra ${URL}"
for i in $(seq 1 "${INTENTOS}"); do
  if curl -fsS "${URL}/actuator/health" 2>/dev/null | grep -q '"UP"'; then
    echo ">> Health UP (intento ${i})"
    curl -fsS "${URL}/" | grep -q "Catálogo de Videojuegos"
    curl -fsS "${URL}/api/generos" >/dev/null
    echo ">> Smoke test OK"
    exit 0
  fi
  sleep 2
done

echo "!! La aplicación no respondió a tiempo en ${URL}" >&2
exit 1

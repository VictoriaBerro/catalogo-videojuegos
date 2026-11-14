# Catálogo de Videojuegos — CI/CD con Jenkins

Entregable 4 · Programación Avanzada 2026 · Universidad de Montevideo
Autora: Victoria Berro Domínguez

Aplicación web Spring Boot para registrar videojuegos, clasificarlos por género y mostrar el catálogo con imágenes.
El foco del trabajo es el pipeline de CI/CD: cada push a GitHub dispara en Jenkins la compilación, las pruebas
unitarias y de integración, el empaquetado en una imagen Docker y el despliegue en un contenedor que simula producción.

## Estructura

```
catalogo-videojuegos/
├── pom.xml                  POM padre (Maven multi-módulo, Spring Boot 3.5, Java 21)
├── modulo-generos/          Módulo 1: enum Genero + ClasificadorGeneros (agrupar/contar)
├── modulo-catalogo/         Módulo 2: entidad Juego, JuegoRepository (JPA), CatalogoService (validaciones)
├── app-web/                 App Spring Boot: interfaz Thymeleaf + API REST + Dockerfile de producción
├── Jenkinsfile              Pipeline declarativo (pipeline as code)
├── deploy/desplegar.sh      Despliega una imagen en el contenedor catalogo-prod
├── deploy/smoke-test.sh     Verifica que producción responde (health + página + API)
└── jenkins/                 Dockerfile + docker-compose para levantar Jenkins
```

| Ambiente     | Cómo se levanta                  | URL                    |
|--------------|----------------------------------|------------------------|
| Desarrollo   | `java -jar` en tu máquina        | http://localhost:8080  |
| Jenkins      | `docker compose` en `jenkins/`   | http://localhost:8090  |
| Producción   | contenedor `catalogo-prod` (lo crea el pipeline) | http://localhost:8081 |

## Requisitos

- Java 21 y Maven 3.9 (para desarrollo local)
- Docker Desktop (o Docker Engine) corriendo
- Git y una cuenta de GitHub

## 1. Probar en desarrollo

```bash
mvn verify                                   # compila + pruebas unitarias + pruebas de integración
java -jar app-web/target/catalogo-videojuegos.jar
```

Abrir http://localhost:8080. La primera vez se cargan 5 juegos de ejemplo. Los datos quedan en `./data/` (H2 en archivo).

Comandos útiles:

```bash
mvn test                       # solo unitarias (los 3 módulos)
mvn verify -DskipUTs=true      # solo integración (*IT.java, con failsafe)
curl http://localhost:8080/api/juegos?genero=aventura
curl -X POST http://localhost:8080/api/juegos -H "Content-Type: application/json" \
     -d '{"titulo":"Celeste","genero":"Aventura","anio":2018,"plataforma":"PC"}'
```

## 2. Subir el código a GitHub

Crear un repositorio **público** vacío (ej. `catalogo-videojuegos`) y:

```bash
git init -b main          # (si la carpeta no tiene .git)
git add .
git commit -m "Versión inicial"
git remote add origin https://github.com/<tu-usuario>/catalogo-videojuegos.git
git push -u origin main
```

## 3. Levantar Jenkins

```bash
cd jenkins
docker compose up -d --build
docker exec jenkins cat /var/jenkins_home/secrets/initialAdminPassword
```

1. Abrir http://localhost:8090, pegar la contraseña, elegir **Install suggested plugins** y crear el usuario admin.
2. **New Item** → nombre `catalogo-videojuegos` → **Pipeline** → OK.
3. En **Pipeline**: Definition = *Pipeline script from SCM*, SCM = *Git*,
   Repository URL = la URL del repo, Branch = `*/main`, Script Path = `Jenkinsfile`. Guardar.
4. **Build Now** una vez. Esta primera ejecución es necesaria: recién ahí Jenkins lee el Jenkinsfile y registra
   los triggers (pollSCM y githubPush). Desde ese momento cada push a `main` dispara el pipeline solo
   (Jenkins revisa el repo cada minuto).
5. Al terminar en verde, abrir http://localhost:8081: es producción, con la etiqueta PRODUCCION y la versión
   `vN-<commit>` en el pie de página.

### (Opcional) Webhook de GitHub para disparo inmediato

GitHub no puede llegar a `localhost`, así que hace falta exponer Jenkins, por ejemplo con ngrok:

```bash
ngrok http 8090
```

En GitHub: Settings → Webhooks → Add webhook → Payload URL `https://<id>.ngrok-free.app/github-webhook/`,
Content type `application/json`, evento *Just the push event*. Con eso el build arranca a los pocos segundos del push.
Sin webhook, el polling de cada minuto ya cumple con detectar los cambios.

## 4. El pipeline

| Etapa                  | Qué hace                                                         | Si falla                         |
|------------------------|------------------------------------------------------------------|----------------------------------|
| Checkout               | Baja el commit y arma la versión `v<build>-<commit>`             | —                                |
| Compilación            | `mvn clean compile` de los 3 módulos                             | Se corta, no hay deploy          |
| Pruebas unitarias      | `mvn test` (JUnit 5 + Mockito); publica reportes JUnit           | Se corta, no hay deploy          |
| Pruebas de integración | `mvn verify -DskipUTs=true` (SpringBootTest + MockMvc + H2)      | Se corta, no hay deploy          |
| Empaquetado            | Archiva el `.jar` y construye la imagen `catalogo-videojuegos:vN-commit` | Se corta, no hay deploy  |
| Deploy a producción    | Reemplaza el contenedor `catalogo-prod` (datos en volumen)       | —                                |
| Smoke test             | Espera `/actuator/health = UP`, carga la página y la API         | Rollback a la imagen anterior    |

## 5. Guion para la defensa

**Antes de la clase:** Jenkins levantado, un build verde, producción abierta en http://localhost:8081
y la vista del job en Jenkins (Stage View) abierta en otra pestaña.

1. **Arquitectura (1–2 min):** mostrar la estructura de módulos y el Jenkinsfile, explicar las etapas con la tabla de arriba.
2. **App funcionando (1 min):** en producción, registrar un juego y filtrar por género.
3. **Caso 1 — integración exitosa:** agregar un género nuevo. En
   `modulo-generos/src/main/java/uy/edu/um/videojuegos/generos/Genero.java` reemplazar
   `TERROR("Terror");` por `TERROR("Terror"), PLATAFORMAS("Plataformas");` y:
   ```bash
   git commit -am "Agrega el género Plataformas" && git push
   ```
   En menos de un minuto arranca el build solo. Cuando termina en verde, recargar http://localhost:8081:
   aparece el filtro **Plataformas** y la versión nueva en el pie.
4. **Caso 2 — fallo en las pruebas:** introducir un bug. En `CatalogoService.java`, dentro de `registrar`,
   comentar el chequeo de duplicados (el `if (repositorio.existsByTituloIgnoreCase(titulo)) {...}`) y hacer push.
   - El pipeline falla en **Pruebas unitarias**: `registrarTituloDuplicadoLanzaExcepcion` detecta el bug.
   - Las etapas de empaquetado y deploy quedan salteadas: producción **sigue con la versión anterior** (mostrarlo).
   - Cómo se procede: abrir el build → *Test Result* para ver qué prueba falló y por qué → corregir
     (o `git revert HEAD`) → push → el pipeline vuelve a verde y despliega.

## Decisiones principales

- **Maven multi-módulo**: los módulos de géneros y catálogo son independientes, cada uno con sus pruebas unitarias;
  la app web los integra y concentra las pruebas de integración.
- **Unitarias vs. integración**: surefire corre `*Test.java` y failsafe corre `*IT.java`, así el pipeline las separa en etapas.
- **H2 en archivo**: persistencia real sin instalar un motor de base de datos; en producción vive en un volumen Docker
  para no perder datos al redesplegar.
- **Docker como producción simulada**: la misma imagen que se probó es la que se despliega; cada build queda
  etiquetado con su versión, lo que permite rollback.
- **pollSCM + githubPush**: el polling funciona sin exponer Jenkins a internet; el webhook es opcional.

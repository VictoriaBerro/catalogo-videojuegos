// Pipeline declarativo - Catálogo de Videojuegos
// Entregable 4 - Programación Avanzada 2026 - Universidad de Montevideo
//
// Flujo: checkout -> compilación -> pruebas unitarias -> pruebas de integración
//        -> empaquetado (jar + imagen Docker) -> deploy a producción -> smoke test
// Si cualquier etapa falla, las siguientes no se ejecutan y producción queda
// con la última versión buena.

pipeline {
    agent any

    options {
        timestamps()
        disableConcurrentBuilds()
        buildDiscarder(logRotator(numToKeepStr: '20'))
        timeout(time: 20, unit: 'MINUTES')
    }

    // Se dispara solo ante cambios en GitHub:
    //  - githubPush(): si se configura el webhook de GitHub (requiere URL pública, ej. ngrok)
    //  - pollSCM: Jenkins consulta el repo cada minuto (funciona sin URL pública)
    triggers {
        githubPush()
        pollSCM('* * * * *')
    }

    environment {
        // El repositorio local de Maven queda en el volumen de Jenkins para no re-descargar todo en cada build
        MVN    = 'mvn -B -ntp -Dmaven.repo.local=$JENKINS_HOME/.m2/repository'
        IMAGEN = 'catalogo-videojuegos'
    }

    stages {
        stage('Checkout') {
            steps {
                checkout scm
                script {
                    env.COMMIT_CORTO = sh(script: 'git rev-parse --short HEAD', returnStdout: true).trim()
                    env.VERSION = "v${env.BUILD_NUMBER}-${env.COMMIT_CORTO}"
                }
                echo "Versión a construir: ${env.VERSION}"
            }
        }

        stage('Compilación') {
            steps {
                sh "${env.MVN} clean compile"
            }
        }

        stage('Pruebas unitarias') {
            steps {
                sh "${env.MVN} test"
            }
            post {
                always {
                    junit allowEmptyResults: true, testResults: '**/target/surefire-reports/*.xml'
                }
            }
        }

        stage('Pruebas de integración') {
            steps {
                // skipUTs=true: no repite las unitarias, solo corre las *IT con failsafe
                sh "${env.MVN} verify -DskipUTs=true"
            }
            post {
                always {
                    junit allowEmptyResults: true, testResults: '**/target/failsafe-reports/*.xml'
                }
            }
        }

        stage('Empaquetado') {
            steps {
                archiveArtifacts artifacts: 'app-web/target/catalogo-videojuegos.jar', fingerprint: true
                sh "docker build -t ${env.IMAGEN}:${env.VERSION} -t ${env.IMAGEN}:latest app-web"
            }
        }

        stage('Deploy a producción') {
            steps {
                script {
                    // Se guarda la imagen que está corriendo ahora, por si hay que volver atrás
                    env.IMAGEN_ANTERIOR = sh(
                        script: "docker inspect --format '{{.Config.Image}}' catalogo-prod 2>/dev/null || true",
                        returnStdout: true).trim()
                }
                echo "Versión en producción antes del deploy: ${env.IMAGEN_ANTERIOR ?: 'ninguna'}"
                sh "bash deploy/desplegar.sh ${env.IMAGEN}:${env.VERSION}"
            }
        }

        stage('Smoke test') {
            steps {
                sh 'bash deploy/smoke-test.sh'
            }
            post {
                failure {
                    script {
                        if (env.IMAGEN_ANTERIOR) {
                            echo "Smoke test falló: rollback a ${env.IMAGEN_ANTERIOR}"
                            sh "bash deploy/desplegar.sh ${env.IMAGEN_ANTERIOR}"
                        }
                    }
                }
            }
        }
    }

    post {
        success {
            echo "OK: versión ${env.VERSION} desplegada en producción -> http://localhost:8081"
        }
        failure {
            echo "FALLÓ el pipeline (build ${env.BUILD_NUMBER}). Producción NO se modificó / volvió a la versión anterior."
        }
    }
}

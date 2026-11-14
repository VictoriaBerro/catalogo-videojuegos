package uy.edu.um.videojuegos;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Punto de entrada. Está en el paquete raíz (uy.edu.um.videojuegos) para que Spring
 * escanee los componentes, entidades y repositorios de los tres módulos.
 */
@SpringBootApplication
public class VideojuegosApplication {

    public static void main(String[] args) {
        SpringApplication.run(VideojuegosApplication.class, args);
    }
}

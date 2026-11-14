package uy.edu.um.videojuegos.web;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import uy.edu.um.videojuegos.catalogo.CatalogoService;
import uy.edu.um.videojuegos.catalogo.NuevoJuego;

import java.util.List;

/**
 * Carga unos juegos de ejemplo la primera vez que arranca la app (base vacía).
 * Se desactiva con catalogo.datos-iniciales=false (lo usan las pruebas de integración).
 */
@Component
@ConditionalOnProperty(name = "catalogo.datos-iniciales", havingValue = "true", matchIfMissing = true)
public class DatosIniciales implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DatosIniciales.class);

    private final CatalogoService catalogo;

    public DatosIniciales(CatalogoService catalogo) {
        this.catalogo = catalogo;
    }

    @Override
    public void run(String... args) {
        if (!catalogo.listar().isEmpty()) {
            return;
        }
        List<NuevoJuego> ejemplos = List.of(
                new NuevoJuego("The Legend of Zelda: Breath of the Wild", "Aventura", 2017, "Nintendo Switch",
                        "https://placehold.co/300x400/2e7d32/ffffff?text=Zelda"),
                new NuevoJuego("Hades", "Acción", 2020, "PC",
                        "https://placehold.co/300x400/b71c1c/ffffff?text=Hades"),
                new NuevoJuego("Civilization VI", "Estrategia", 2016, "PC",
                        "https://placehold.co/300x400/1565c0/ffffff?text=Civ+VI"),
                new NuevoJuego("Tetris Effect", "Puzzle", 2018, "PS4",
                        "https://placehold.co/300x400/6a1b9a/ffffff?text=Tetris"),
                new NuevoJuego("Gran Turismo 7", "Carreras", 2022, "PS5",
                        "https://placehold.co/300x400/ef6c00/ffffff?text=GT7"));
        ejemplos.forEach(catalogo::registrar);
        log.info("Se cargaron {} juegos de ejemplo", ejemplos.size());
    }
}

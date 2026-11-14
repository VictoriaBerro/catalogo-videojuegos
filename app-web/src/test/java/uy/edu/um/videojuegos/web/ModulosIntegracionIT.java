package uy.edu.um.videojuegos.web;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import uy.edu.um.videojuegos.catalogo.CatalogoService;
import uy.edu.um.videojuegos.catalogo.Juego;
import uy.edu.um.videojuegos.catalogo.JuegoRepository;
import uy.edu.um.videojuegos.catalogo.NuevoJuego;
import uy.edu.um.videojuegos.generos.Genero;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integración entre módulos sin pasar por HTTP: el servicio de catálogo real
 * + el clasificador de géneros real + persistencia JPA sobre H2.
 */
@SpringBootTest
@ActiveProfiles("test")
class ModulosIntegracionIT {

    @Autowired
    private CatalogoService catalogo;

    @Autowired
    private JuegoRepository repositorio;

    @BeforeEach
    void limpiarBase() {
        repositorio.deleteAll();
    }

    @Test
    void elCatalogoPersistidoSeAgrupaPorGenero() {
        catalogo.registrar(new NuevoJuego("Portal 2", "puzzle", 2011, "PC", null));
        catalogo.registrar(new NuevoJuego("Age of Empires II", "estrategia", 1999, "PC", null));
        catalogo.registrar(new NuevoJuego("Baba Is You", "Puzzle", 2019, "Switch", null));

        Map<Genero, List<Juego>> grupos = catalogo.catalogoPorGenero();

        assertThat(grupos.keySet()).containsExactly(Genero.ESTRATEGIA, Genero.PUZZLE);
        assertThat(grupos.get(Genero.PUZZLE))
                .extracting(Juego::getTitulo)
                .containsExactly("Baba Is You", "Portal 2");
    }

    @Test
    void losJuegosQuedanGuardadosEnLaBase() {
        Juego juego = catalogo.registrar(new NuevoJuego("Hollow Knight", "aventura", 2017, null, null));

        assertThat(juego.getId()).isNotNull();
        assertThat(repositorio.findById(juego.getId()))
                .get()
                .extracting(Juego::getGenero)
                .isEqualTo(Genero.AVENTURA);
    }
}

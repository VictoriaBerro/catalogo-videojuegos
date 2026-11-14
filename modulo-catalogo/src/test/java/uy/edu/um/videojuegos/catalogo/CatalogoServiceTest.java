package uy.edu.um.videojuegos.catalogo;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import uy.edu.um.videojuegos.generos.ClasificadorGeneros;
import uy.edu.um.videojuegos.generos.Genero;
import uy.edu.um.videojuegos.generos.GeneroInvalidoException;

import java.time.Year;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Pruebas unitarias del servicio: el repositorio se reemplaza por un mock,
 * así que no se usa base de datos ni contexto de Spring.
 */
class CatalogoServiceTest {

    private JuegoRepository repositorio;
    private CatalogoService servicio;

    @BeforeEach
    void setUp() {
        repositorio = mock(JuegoRepository.class);
        when(repositorio.save(any(Juego.class))).thenAnswer(inv -> inv.getArgument(0));
        servicio = new CatalogoService(repositorio, new ClasificadorGeneros());
    }

    @Test
    void registrarGuardaElJuegoConDatosLimpios() {
        Juego guardado = servicio.registrar(
                new NuevoJuego("  Hollow Knight  ", "aventura", 2017, "  Switch ", " "));

        ArgumentCaptor<Juego> captor = ArgumentCaptor.forClass(Juego.class);
        verify(repositorio).save(captor.capture());
        Juego juego = captor.getValue();
        assertThat(juego.getTitulo()).isEqualTo("Hollow Knight");
        assertThat(juego.getGenero()).isEqualTo(Genero.AVENTURA);
        assertThat(juego.getAnio()).isEqualTo(2017);
        assertThat(juego.getPlataforma()).isEqualTo("Switch");
        assertThat(juego.getImagenUrl()).isNull();
        assertThat(guardado).isSameAs(juego);
    }

    @Test
    void registrarSinTituloLanzaExcepcion() {
        assertThatThrownBy(() -> servicio.registrar(new NuevoJuego(" ", "RPG", 2020, null, null)))
                .isInstanceOf(DatosInvalidosException.class)
                .hasMessageContaining("título");
        verify(repositorio, never()).save(any());
    }

    @Test
    void registrarTituloDemasiadoLargoLanzaExcepcion() {
        String largo = "x".repeat(CatalogoService.LARGO_MAXIMO_TITULO + 1);
        assertThatThrownBy(() -> servicio.registrar(new NuevoJuego(largo, "RPG", 2020, null, null)))
                .isInstanceOf(DatosInvalidosException.class);
    }

    @Test
    void registrarConGeneroInvalidoLanzaExcepcion() {
        assertThatThrownBy(() -> servicio.registrar(new NuevoJuego("Juego X", "Cocina", 2020, null, null)))
                .isInstanceOf(GeneroInvalidoException.class);
        verify(repositorio, never()).save(any());
    }

    @Test
    void registrarConAnioFueraDeRangoLanzaExcepcion() {
        int futuro = Year.now().getValue() + 5;
        assertThatThrownBy(() -> servicio.registrar(new NuevoJuego("Juego X", "RPG", futuro, null, null)))
                .isInstanceOf(DatosInvalidosException.class)
                .hasMessageContaining("año");
        assertThatThrownBy(() -> servicio.registrar(new NuevoJuego("Juego Y", "RPG", 1900, null, null)))
                .isInstanceOf(DatosInvalidosException.class);
    }

    @Test
    void registrarSinAnioEsValido() {
        Juego juego = servicio.registrar(new NuevoJuego("Tetris", "puzzle", null, null, null));
        assertThat(juego.getAnio()).isNull();
    }

    @Test
    void registrarTituloDuplicadoLanzaExcepcion() {
        when(repositorio.existsByTituloIgnoreCase(anyString())).thenReturn(true);

        assertThatThrownBy(() -> servicio.registrar(new NuevoJuego("Doom", "shooter", 1993, null, null)))
                .isInstanceOf(DatosInvalidosException.class)
                .hasMessageContaining("Ya existe");
        verify(repositorio, never()).save(any());
    }

    @Test
    void buscarPorGeneroConvierteElTextoAlEnum() {
        Juego fifa = new Juego("FIFA", Genero.DEPORTES, 2024, null, null);
        when(repositorio.findByGeneroOrderByTituloAsc(Genero.DEPORTES)).thenReturn(List.of(fifa));

        assertThat(servicio.buscarPorGenero("deportes")).containsExactly(fifa);
    }

    @Test
    void catalogoPorGeneroAgrupaUsandoElModuloDeGeneros() {
        Juego doom = new Juego("Doom", Genero.SHOOTER, 1993, null, null);
        Juego halo = new Juego("Halo", Genero.SHOOTER, 2001, null, null);
        Juego civ = new Juego("Civilization", Genero.ESTRATEGIA, 1991, null, null);
        when(repositorio.findAllByOrderByTituloAsc()).thenReturn(List.of(civ, doom, halo));

        Map<Genero, List<Juego>> grupos = servicio.catalogoPorGenero();

        assertThat(grupos.get(Genero.SHOOTER)).containsExactly(doom, halo);
        assertThat(grupos.get(Genero.ESTRATEGIA)).containsExactly(civ);
        assertThat(servicio.cantidadPorGenero()).containsEntry(Genero.SHOOTER, 2);
    }
}

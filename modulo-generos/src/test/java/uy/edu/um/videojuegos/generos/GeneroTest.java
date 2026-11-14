package uy.edu.um.videojuegos.generos;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class GeneroTest {

    @ParameterizedTest
    @ValueSource(strings = {"ACCION", "accion", "Acción", "  acción  ", "ACCIÓN"})
    void desdeTextoReconoceVariantesDeAccion(String texto) {
        assertThat(Genero.desdeTexto(texto)).isEqualTo(Genero.ACCION);
    }

    @Test
    void desdeTextoReconoceSimulacionSinTilde() {
        assertThat(Genero.desdeTexto("simulacion")).isEqualTo(Genero.SIMULACION);
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"   "})
    void desdeTextoRechazaVacio(String texto) {
        assertThatThrownBy(() -> Genero.desdeTexto(texto))
                .isInstanceOf(GeneroInvalidoException.class)
                .hasMessageContaining("obligatorio");
    }

    @Test
    void desdeTextoRechazaGeneroDesconocido() {
        assertThatThrownBy(() -> Genero.desdeTexto("Cocina"))
                .isInstanceOf(GeneroInvalidoException.class)
                .hasMessageContaining("Cocina");
    }

    @Test
    void cadaGeneroTieneNombreParaMostrar() {
        for (Genero genero : Genero.values()) {
            assertThat(genero.getNombre()).isNotBlank();
        }
    }
}

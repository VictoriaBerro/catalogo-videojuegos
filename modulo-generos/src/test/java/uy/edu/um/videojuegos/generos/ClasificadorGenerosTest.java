package uy.edu.um.videojuegos.generos;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ClasificadorGenerosTest {

    private record Item(String nombre, Genero genero) {}

    private final ClasificadorGeneros clasificador = new ClasificadorGeneros();

    @Test
    void todosDevuelveTodosLosGeneros() {
        assertThat(clasificador.todos()).containsExactly(Genero.values());
    }

    @Test
    void agruparSeparaPorGeneroYRespetaElOrdenDelEnum() {
        List<Item> items = List.of(
                new Item("Zelda", Genero.AVENTURA),
                new Item("Doom", Genero.SHOOTER),
                new Item("Hades", Genero.ACCION),
                new Item("Myst", Genero.AVENTURA));

        Map<Genero, List<Item>> grupos = clasificador.agrupar(items, Item::genero);

        assertThat(grupos.keySet()).containsExactly(Genero.ACCION, Genero.AVENTURA, Genero.SHOOTER);
        assertThat(grupos.get(Genero.AVENTURA)).extracting(Item::nombre).containsExactly("Zelda", "Myst");
    }

    @Test
    void agruparListaVaciaDevuelveMapaVacio() {
        assertThat(clasificador.agrupar(List.<Item>of(), Item::genero)).isEmpty();
    }

    @Test
    void agruparFallaSiUnElementoNoTieneGenero() {
        List<Item> items = List.of(new Item("Sin género", null));
        assertThatThrownBy(() -> clasificador.agrupar(items, Item::genero))
                .isInstanceOf(GeneroInvalidoException.class);
    }

    @Test
    void contarDevuelveCantidadPorGenero() {
        List<Item> items = List.of(
                new Item("FIFA", Genero.DEPORTES),
                new Item("NBA 2K", Genero.DEPORTES),
                new Item("Tetris", Genero.PUZZLE));

        assertThat(clasificador.contar(items, Item::genero))
                .containsEntry(Genero.DEPORTES, 2)
                .containsEntry(Genero.PUZZLE, 1)
                .hasSize(2);
    }
}

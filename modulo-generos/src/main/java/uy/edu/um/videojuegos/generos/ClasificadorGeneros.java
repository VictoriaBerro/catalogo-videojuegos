package uy.edu.um.videojuegos.generos;

import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

/**
 * Agrupa y cuenta elementos (por ejemplo, videojuegos) según su género.
 * Es genérico para no depender del módulo de catálogo.
 */
@Service
public class ClasificadorGeneros {

    /** Todos los géneros disponibles, en el orden en que se declaran. */
    public List<Genero> todos() {
        return List.of(Genero.values());
    }

    /**
     * Agrupa los elementos por género. El resultado mantiene el orden de los géneros
     * e incluye solo los géneros que tienen al menos un elemento.
     */
    public <T> Map<Genero, List<T>> agrupar(Collection<T> elementos, Function<T, Genero> generoDe) {
        Map<Genero, List<T>> grupos = new EnumMap<>(Genero.class);
        for (T elemento : elementos) {
            Genero genero = generoDe.apply(elemento);
            if (genero == null) {
                throw new GeneroInvalidoException("Hay un elemento sin género asignado");
            }
            grupos.computeIfAbsent(genero, g -> new ArrayList<>()).add(elemento);
        }
        return grupos;
    }

    /** Cantidad de elementos por género (solo géneros con al menos uno). */
    public <T> Map<Genero, Integer> contar(Collection<T> elementos, Function<T, Genero> generoDe) {
        Map<Genero, Integer> conteo = new EnumMap<>(Genero.class);
        agrupar(elementos, generoDe).forEach((genero, lista) -> conteo.put(genero, lista.size()));
        return conteo;
    }
}

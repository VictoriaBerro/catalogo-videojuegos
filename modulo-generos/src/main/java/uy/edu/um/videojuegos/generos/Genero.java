package uy.edu.um.videojuegos.generos;

import java.text.Normalizer;
import java.util.Locale;

/**
 * Géneros de videojuegos soportados por el catálogo.
 */
public enum Genero {
    ACCION("Acción"),
    AVENTURA("Aventura"),
    RPG("RPG"),
    ESTRATEGIA("Estrategia"),
    DEPORTES("Deportes"),
    CARRERAS("Carreras"),
    SHOOTER("Shooter"),
    PUZZLE("Puzzle"),
    SIMULACION("Simulación"),
    TERROR("Terror"), PLATAFORMAS("Plataformas"), MUSICAL("Musical"), LUCHA("Lucha");

    private final String nombre;

    Genero(String nombre) {
        this.nombre = nombre;
    }

    public String getNombre() {
        return nombre;
    }

    /**
     * Convierte un texto libre ("acción", " Accion ", "ACCION") al género correspondiente.
     * Ignora mayúsculas, espacios y tildes.
     *
     * @throws GeneroInvalidoException si el texto es vacío o no corresponde a ningún género
     */
    public static Genero desdeTexto(String texto) {
        if (texto == null || texto.isBlank()) {
            throw new GeneroInvalidoException("El género es obligatorio");
        }
        String clave = normalizar(texto);
        for (Genero genero : values()) {
            if (genero.name().equals(clave) || normalizar(genero.nombre).equals(clave)) {
                return genero;
            }
        }
        throw new GeneroInvalidoException("Género desconocido: " + texto.trim());
    }

    static String normalizar(String texto) {
        String sinTildes = Normalizer.normalize(texto.trim(), Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "");
        return sinTildes.toUpperCase(Locale.ROOT);
    }
}

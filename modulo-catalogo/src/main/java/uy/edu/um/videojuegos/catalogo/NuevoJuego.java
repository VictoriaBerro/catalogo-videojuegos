package uy.edu.um.videojuegos.catalogo;

/**
 * Datos de entrada para registrar un juego. El género llega como texto
 * y lo valida el módulo de géneros.
 */
public record NuevoJuego(String titulo, String genero, Integer anio, String plataforma, String imagenUrl) {
}

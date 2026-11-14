package uy.edu.um.videojuegos.generos;

public class GeneroInvalidoException extends IllegalArgumentException {

    public GeneroInvalidoException(String mensaje) {
        super(mensaje);
    }
}

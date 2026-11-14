package uy.edu.um.videojuegos.catalogo;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uy.edu.um.videojuegos.generos.ClasificadorGeneros;
import uy.edu.um.videojuegos.generos.Genero;

import java.time.Year;
import java.util.List;
import java.util.Map;

@Service
public class CatalogoService {

    static final int ANIO_MINIMO = 1950;
    static final int LARGO_MAXIMO_TITULO = 100;

    private final JuegoRepository repositorio;
    private final ClasificadorGeneros clasificador;

    public CatalogoService(JuegoRepository repositorio, ClasificadorGeneros clasificador) {
        this.repositorio = repositorio;
        this.clasificador = clasificador;
    }

    @Transactional
    public Juego registrar(NuevoJuego datos) {
        if (datos == null) {
            throw new DatosInvalidosException("No se recibieron datos del juego");
        }
        String titulo = validarTitulo(datos.titulo());
        Genero genero = Genero.desdeTexto(datos.genero());
        validarAnio(datos.anio());

        if (repositorio.existsByTituloIgnoreCase(titulo)) {
            throw new DatosInvalidosException("Ya existe un juego con el título \"" + titulo + "\"");
        }

        Juego juego = new Juego(titulo, genero, datos.anio(),
                textoOpcional(datos.plataforma()), textoOpcional(datos.imagenUrl()));
        return repositorio.save(juego);
    }

    @Transactional(readOnly = true)
    public List<Juego> listar() {
        return repositorio.findAllByOrderByTituloAsc();
    }

    @Transactional(readOnly = true)
    public List<Juego> buscarPorGenero(String genero) {
        return repositorio.findByGeneroOrderByTituloAsc(Genero.desdeTexto(genero));
    }

    @Transactional(readOnly = true)
    public Map<Genero, List<Juego>> catalogoPorGenero() {
        return clasificador.agrupar(listar(), Juego::getGenero);
    }

    @Transactional(readOnly = true)
    public Map<Genero, Integer> cantidadPorGenero() {
        return clasificador.contar(listar(), Juego::getGenero);
    }

    public List<Genero> generosDisponibles() {
        return clasificador.todos();
    }

    private String validarTitulo(String titulo) {
        if (titulo == null || titulo.isBlank()) {
            throw new DatosInvalidosException("El título es obligatorio");
        }
        String limpio = titulo.trim();
        if (limpio.length() > LARGO_MAXIMO_TITULO) {
            throw new DatosInvalidosException("El título no puede superar " + LARGO_MAXIMO_TITULO + " caracteres");
        }
        return limpio;
    }

    private void validarAnio(Integer anio) {
        if (anio == null) {
            return; // el año es opcional
        }
        int maximo = Year.now().getValue() + 1;
        if (anio < ANIO_MINIMO || anio > maximo) {
            throw new DatosInvalidosException("El año debe estar entre " + ANIO_MINIMO + " y " + maximo);
        }
    }

    private String textoOpcional(String texto) {
        return (texto == null || texto.isBlank()) ? null : texto.trim();
    }
}

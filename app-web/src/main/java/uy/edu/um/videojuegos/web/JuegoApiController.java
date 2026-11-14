package uy.edu.um.videojuegos.web;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import uy.edu.um.videojuegos.catalogo.CatalogoService;
import uy.edu.um.videojuegos.catalogo.Juego;
import uy.edu.um.videojuegos.catalogo.NuevoJuego;
import uy.edu.um.videojuegos.generos.Genero;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** API REST del catálogo (útil para las pruebas y para probar con curl/Postman). */
@RestController
@RequestMapping("/api")
public class JuegoApiController {

    private final CatalogoService catalogo;

    public JuegoApiController(CatalogoService catalogo) {
        this.catalogo = catalogo;
    }

    @GetMapping("/juegos")
    public List<Juego> listar(@RequestParam(required = false) String genero) {
        return (genero == null || genero.isBlank()) ? catalogo.listar() : catalogo.buscarPorGenero(genero);
    }

    @PostMapping("/juegos")
    @ResponseStatus(HttpStatus.CREATED)
    public Juego registrar(@RequestBody NuevoJuego datos) {
        return catalogo.registrar(datos);
    }

    /** Cantidad de juegos por género, con el nombre legible del género como clave. */
    @GetMapping("/generos/resumen")
    public Map<String, Integer> resumenPorGenero() {
        Map<String, Integer> resumen = new LinkedHashMap<>();
        catalogo.cantidadPorGenero().forEach((genero, cantidad) -> resumen.put(genero.getNombre(), cantidad));
        return resumen;
    }

    @GetMapping("/generos")
    public List<String> generos() {
        return catalogo.generosDisponibles().stream().map(Genero::getNombre).toList();
    }
}

package uy.edu.um.videojuegos.web;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import uy.edu.um.videojuegos.catalogo.CatalogoService;
import uy.edu.um.videojuegos.catalogo.Juego;
import uy.edu.um.videojuegos.catalogo.NuevoJuego;
import uy.edu.um.videojuegos.generos.Genero;

import java.util.List;

/** Interfaz web (Thymeleaf): catálogo con imágenes, filtro por género y formulario de alta. */
@Controller
public class CatalogoWebController {

    private final CatalogoService catalogo;
    private final String version;
    private final String entorno;

    public CatalogoWebController(CatalogoService catalogo,
                                 @Value("${app.version}") String version,
                                 @Value("${app.entorno}") String entorno) {
        this.catalogo = catalogo;
        this.version = version;
        this.entorno = entorno;
    }

    @ModelAttribute
    public void datosComunes(Model model) {
        model.addAttribute("version", version);
        model.addAttribute("entorno", entorno);
        model.addAttribute("generos", catalogo.generosDisponibles());
        model.addAttribute("resumen", catalogo.cantidadPorGenero());
    }

    @GetMapping("/")
    public String inicio(@RequestParam(required = false) String genero, Model model) {
        List<Juego> juegos = catalogo.listar();
        Genero seleccionado = null;
        if (genero != null && !genero.isBlank()) {
            try {
                seleccionado = Genero.desdeTexto(genero);
                juegos = catalogo.buscarPorGenero(genero);
            } catch (IllegalArgumentException e) {
                model.addAttribute("error", e.getMessage());
            }
        }
        model.addAttribute("juegos", juegos);
        model.addAttribute("seleccionado", seleccionado);
        return "index";
    }

    @PostMapping("/juegos")
    public String registrar(@RequestParam String titulo,
                            @RequestParam String genero,
                            @RequestParam(required = false) Integer anio,
                            @RequestParam(required = false) String plataforma,
                            @RequestParam(required = false) String imagenUrl,
                            RedirectAttributes redirect) {
        try {
            Juego juego = catalogo.registrar(new NuevoJuego(titulo, genero, anio, plataforma, imagenUrl));
            redirect.addFlashAttribute("mensaje", "Se registró \"" + juego.getTitulo() + "\"");
        } catch (IllegalArgumentException e) {
            redirect.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/";
    }
}

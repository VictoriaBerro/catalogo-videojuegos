package uy.edu.um.videojuegos.web;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import uy.edu.um.videojuegos.catalogo.JuegoRepository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.flash;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** Pruebas de integración de la interfaz web (controlador + Thymeleaf + módulos + base). */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class CatalogoWebIT {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private JuegoRepository repositorio;

    @BeforeEach
    void limpiarBase() {
        repositorio.deleteAll();
    }

    @Test
    void paginaPrincipalMuestraElCatalogo() throws Exception {
        mvc.perform(get("/"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Catálogo de Videojuegos")))
                .andExpect(content().string(containsString("Registrar juego")));
    }

    @Test
    void registrarDesdeElFormularioYVerloEnLaLista() throws Exception {
        mvc.perform(post("/juegos")
                        .param("titulo", "Stardew Valley")
                        .param("genero", "SIMULACION")
                        .param("anio", "2016")
                        .param("plataforma", "PC"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/"))
                .andExpect(flash().attributeExists("mensaje"));

        assertThat(repositorio.existsByTituloIgnoreCase("Stardew Valley")).isTrue();

        mvc.perform(get("/"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Stardew Valley")));
    }

    @Test
    void formularioConDatosInvalidosMuestraError() throws Exception {
        mvc.perform(post("/juegos")
                        .param("titulo", "  ")
                        .param("genero", "RPG"))
                .andExpect(status().is3xxRedirection())
                .andExpect(flash().attributeExists("error"));

        assertThat(repositorio.count()).isZero();
    }

    @Test
    void filtroConGeneroDesconocidoNoRompeLaPagina() throws Exception {
        mvc.perform(get("/").param("genero", "inexistente"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Género desconocido")));
    }
}

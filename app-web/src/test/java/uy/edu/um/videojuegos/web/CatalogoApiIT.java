package uy.edu.um.videojuegos.web;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import uy.edu.um.videojuegos.catalogo.JuegoRepository;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Pruebas de integración de la API REST: levantan el contexto completo de Spring
 * (web + catálogo + géneros + base H2 en memoria) y prueban de punta a punta.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class CatalogoApiIT {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private JuegoRepository repositorio;

    @BeforeEach
    void limpiarBase() {
        repositorio.deleteAll();
    }

    private void registrar(String json) throws Exception {
        mvc.perform(post("/api/juegos").contentType(MediaType.APPLICATION_JSON).content(json))
                .andExpect(status().isCreated());
    }

    @Test
    void registrarYListarJuego() throws Exception {
        mvc.perform(post("/api/juegos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"titulo":"Celeste","genero":"aventura","anio":2018,"plataforma":"PC"}
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.titulo").value("Celeste"))
                .andExpect(jsonPath("$.genero").value("AVENTURA"));

        mvc.perform(get("/api/juegos"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].titulo").value("Celeste"));
    }

    @Test
    void filtrarPorGenero() throws Exception {
        registrar("""
                {"titulo":"Doom","genero":"Shooter","anio":1993}""");
        registrar("""
                {"titulo":"Halo","genero":"shooter","anio":2001}""");
        registrar("""
                {"titulo":"FIFA 24","genero":"Deportes","anio":2023}""");

        mvc.perform(get("/api/juegos").param("genero", "SHOOTER"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].titulo").value("Doom"))
                .andExpect(jsonPath("$[1].titulo").value("Halo"));
    }

    @Test
    void resumenCuentaJuegosPorGenero() throws Exception {
        registrar("""
                {"titulo":"Hades","genero":"Acción"}""");
        registrar("""
                {"titulo":"Celeste","genero":"Aventura"}""");
        registrar("""
                {"titulo":"Inside","genero":"Aventura"}""");

        mvc.perform(get("/api/generos/resumen"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.Aventura").value(2))
                .andExpect(jsonPath("$['Acción']").value(1));
    }

    @Test
    void generoInvalidoDevuelve400() throws Exception {
        mvc.perform(post("/api/juegos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"titulo":"Juego raro","genero":"Cocina"}"""))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error", containsString("Cocina")));
    }

    @Test
    void tituloDuplicadoDevuelve400() throws Exception {
        registrar("""
                {"titulo":"Tetris","genero":"Puzzle"}""");

        mvc.perform(post("/api/juegos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"titulo":"tetris","genero":"Puzzle"}"""))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error", containsString("Ya existe")));
    }

    @Test
    void healthCheckEstaUp() throws Exception {
        mvc.perform(get("/actuator/health"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("UP"));
    }
}

package uy.edu.um.videojuegos.web;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Map;

/** Convierte los errores de validación de los módulos en respuestas 400 con un mensaje claro. */
@RestControllerAdvice(assignableTypes = JuegoApiController.class)
public class ManejoErroresApi {

    @ExceptionHandler(IllegalArgumentException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Map<String, String> datosInvalidos(IllegalArgumentException e) {
        return Map.of("error", e.getMessage());
    }
}

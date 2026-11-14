package uy.edu.um.videojuegos.catalogo;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import uy.edu.um.videojuegos.generos.Genero;

@Entity
public class Juego {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 100)
    private String titulo;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private Genero genero;

    private Integer anio;

    @Column(length = 60)
    private String plataforma;

    @Column(length = 500)
    private String imagenUrl;

    protected Juego() {
        // requerido por JPA
    }

    public Juego(String titulo, Genero genero, Integer anio, String plataforma, String imagenUrl) {
        this.titulo = titulo;
        this.genero = genero;
        this.anio = anio;
        this.plataforma = plataforma;
        this.imagenUrl = imagenUrl;
    }

    public Long getId() { return id; }
    public String getTitulo() { return titulo; }
    public Genero getGenero() { return genero; }
    public Integer getAnio() { return anio; }
    public String getPlataforma() { return plataforma; }
    public String getImagenUrl() { return imagenUrl; }
}

package uy.edu.um.videojuegos.catalogo;

import org.springframework.data.jpa.repository.JpaRepository;
import uy.edu.um.videojuegos.generos.Genero;

import java.util.List;

public interface JuegoRepository extends JpaRepository<Juego, Long> {

    List<Juego> findAllByOrderByTituloAsc();

    List<Juego> findByGeneroOrderByTituloAsc(Genero genero);

    boolean existsByTituloIgnoreCase(String titulo);
}

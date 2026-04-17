package cl.patrulleroapp.backend.repository;

import cl.patrulleroapp.backend.model.Imagen;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ImagenRepository extends JpaRepository<Imagen, Integer> {
    List<Imagen> findBySolicitud_IdSolicitud(Integer idSolicitud);
}
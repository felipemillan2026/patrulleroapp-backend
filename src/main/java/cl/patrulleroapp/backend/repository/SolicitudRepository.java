package cl.patrulleroapp.backend.repository;

import cl.patrulleroapp.backend.model.Solicitud;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface SolicitudRepository extends JpaRepository<Solicitud, Integer> {
    List<Solicitud> findByTurno_IdTurno(Integer idTurno);
    List<Solicitud> findByPatrullero_IdUsuario(Integer idUsuario);
    List<Solicitud> findByEstado(String estado);
}
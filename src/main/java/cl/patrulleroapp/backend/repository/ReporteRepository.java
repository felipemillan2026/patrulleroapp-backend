package cl.patrulleroapp.backend.repository;

import cl.patrulleroapp.backend.model.Reporte;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface ReporteRepository extends JpaRepository<Reporte, Integer> {
    Optional<Reporte> findByTurno_IdTurno(Integer idTurno);
}
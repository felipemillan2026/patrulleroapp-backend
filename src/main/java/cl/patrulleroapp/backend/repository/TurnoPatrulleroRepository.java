package cl.patrulleroapp.backend.repository;

import cl.patrulleroapp.backend.model.TurnoPatrullero;
import cl.patrulleroapp.backend.model.TurnoPatrulleroId;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface TurnoPatrulleroRepository 
    extends JpaRepository<TurnoPatrullero, TurnoPatrulleroId> {
    List<TurnoPatrullero> findByTurno_IdTurno(Integer idTurno);
}
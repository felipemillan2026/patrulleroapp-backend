package cl.patrulleroapp.backend.repository;

import cl.patrulleroapp.backend.model.Turno;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import java.util.Optional;

public interface TurnoRepository extends JpaRepository<Turno, Integer> {
    Optional<Turno> findByEstado(String estado);

    @Query("SELECT t FROM Turno t WHERE t.estado = 'cerrado' ORDER BY t.fechaCierre DESC LIMIT 1")
    Optional<Turno> findUltimoCerrado();
}
package cl.patrulleroapp.backend.repository;

import cl.patrulleroapp.backend.model.Departamento;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface DepartamentoRepository extends JpaRepository<Departamento, Integer> {
    List<Departamento> findByActivoTrue();
}
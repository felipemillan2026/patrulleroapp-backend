package cl.patrulleroapp.backend.repository;

import cl.patrulleroapp.backend.model.TipoCaso;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface TipoCasoRepository extends JpaRepository<TipoCaso, Integer> {
    List<TipoCaso> findByDepartamento_IdDepartamento(Integer idDepartamento);
}
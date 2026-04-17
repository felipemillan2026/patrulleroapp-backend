package cl.patrulleroapp.backend.controller;

import cl.patrulleroapp.backend.model.Departamento;
import cl.patrulleroapp.backend.model.TipoCaso;
import cl.patrulleroapp.backend.repository.DepartamentoRepository;
import cl.patrulleroapp.backend.repository.TipoCasoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/departamentos")
@RequiredArgsConstructor
public class DepartamentoController {

    private final DepartamentoRepository departamentoRepository;
    private final TipoCasoRepository tipoCasoRepository;

    @GetMapping
    public ResponseEntity<List<Departamento>> getDepartamentos() {
        return ResponseEntity.ok(departamentoRepository.findByActivoTrue());
    }

    @GetMapping("/{id}/tipos-caso")
    public ResponseEntity<List<TipoCaso>> getTiposCaso(@PathVariable Integer id) {
        return ResponseEntity.ok(
            tipoCasoRepository.findByDepartamento_IdDepartamento(id)
        );
    }
}
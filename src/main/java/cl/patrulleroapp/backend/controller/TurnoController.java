package cl.patrulleroapp.backend.controller;

import cl.patrulleroapp.backend.dto.PatrulleroDto;
import cl.patrulleroapp.backend.dto.TurnoRequest;
import cl.patrulleroapp.backend.dto.TurnoResponse;
import cl.patrulleroapp.backend.service.ReporteService;
import cl.patrulleroapp.backend.service.TurnoService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/turnos")
@RequiredArgsConstructor
public class TurnoController {

    private final TurnoService turnoService;
    private final ReporteService reporteService;

    @PostMapping("/abrir")
    public ResponseEntity<?> abrirTurno(@RequestBody TurnoRequest request) {
        try {
            TurnoResponse response = turnoService.abrirTurno(request);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/activo")
    public ResponseEntity<?> getTurnoActivo() {
        try {
            TurnoResponse response = turnoService.getTurnoActivo();
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.status(404).body(e.getMessage());
        }
    }

    @PutMapping("/cerrar")
    public ResponseEntity<?> cerrarTurno() {
        try {
            TurnoResponse response = turnoService.cerrarTurno();
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/patrulleros")
    public ResponseEntity<List<PatrulleroDto>> getPatrulleros() {
        return ResponseEntity.ok(turnoService.getPatrulleros());
    }

    @GetMapping("/ultimo-cerrado")
    public ResponseEntity<?> getUltimoCerrado() {
        try {
            TurnoResponse response = turnoService.getUltimoCerrado();
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.status(404).body(e.getMessage());
        }
    }

    @GetMapping("/{id}/reporte")
    public ResponseEntity<?> descargarReporte(@PathVariable Integer id) {
        try {
            byte[] pdf = reporteService.generarReporteTurno(id);
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);
            headers.setContentDispositionFormData("attachment",
                "reporte_turno_" + id + ".pdf");
            return ResponseEntity.ok()
                .headers(headers)
                .body(pdf);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}
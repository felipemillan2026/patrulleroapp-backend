package cl.patrulleroapp.backend.controller;

import cl.patrulleroapp.backend.dto.SolicitudRequest;
import cl.patrulleroapp.backend.dto.SolicitudResponse;
import cl.patrulleroapp.backend.service.SolicitudService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/solicitudes")
@RequiredArgsConstructor
public class SolicitudController {

    private final SolicitudService solicitudService;

    @PostMapping
    public ResponseEntity<?> crearSolicitud(@RequestBody SolicitudRequest request) {
        try {
            SolicitudResponse response = solicitudService.crearSolicitud(request);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/mis-solicitudes")
    public ResponseEntity<List<SolicitudResponse>> getMisSolicitudes() {
        return ResponseEntity.ok(solicitudService.getMisSolicitudes());
    }

    @GetMapping("/turno-activo")
    public ResponseEntity<?> getSolicitudesTurnoActivo() {
        try {
            return ResponseEntity.ok(solicitudService.getSolicitudesTurnoActivo());
        } catch (RuntimeException e) {
            return ResponseEntity.status(404).body(e.getMessage());
        }
    }

    @PutMapping("/{id}/estado")
    public ResponseEntity<?> actualizarEstado(
            @PathVariable Integer id,
            @RequestParam String estado) {
        try {
            return ResponseEntity.ok(solicitudService.actualizarEstado(id, estado));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> editarSolicitud(
            @PathVariable Integer id,
            @RequestBody SolicitudRequest request) {
        try {
            return ResponseEntity.ok(solicitudService.editarSolicitud(id, request));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
    }   }
}

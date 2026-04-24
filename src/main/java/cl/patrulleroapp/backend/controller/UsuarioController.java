package cl.patrulleroapp.backend.controller;

import cl.patrulleroapp.backend.dto.UsuarioRequest;
import cl.patrulleroapp.backend.dto.UsuarioResponse;
import cl.patrulleroapp.backend.service.UsuarioService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/usuarios")
@RequiredArgsConstructor
public class UsuarioController {

    private final UsuarioService usuarioService;

    @GetMapping
    public ResponseEntity<List<UsuarioResponse>> getAll() {
        return ResponseEntity.ok(usuarioService.getAll());
    }

    @PostMapping
    public ResponseEntity<?> crear(@RequestBody UsuarioRequest request) {
        try {
            return ResponseEntity.ok(usuarioService.crear(request));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> actualizar(
            @PathVariable Integer id,
            @RequestBody UsuarioRequest request) {
        try {
            return ResponseEntity.ok(usuarioService.actualizar(id, request));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PutMapping("/{id}/toggle-activo")
    public ResponseEntity<?> toggleActivo(@PathVariable Integer id) {
        try {
            usuarioService.toggleActivo(id);
            return ResponseEntity.ok("Estado actualizado");
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/mi-perfil")
public ResponseEntity<?> getMiPerfil() {
    try {
        return ResponseEntity.ok(usuarioService.getMiPerfil());
    } catch (RuntimeException e) {
        return ResponseEntity.badRequest().body(e.getMessage());
    }
}

@PutMapping("/mi-perfil")
public ResponseEntity<?> actualizarMiPerfil(@RequestBody UsuarioRequest request) {
    try {
        return ResponseEntity.ok(usuarioService.actualizarMiPerfil(request));
    } catch (RuntimeException e) {
        return ResponseEntity.badRequest().body(e.getMessage());
    }
}
}
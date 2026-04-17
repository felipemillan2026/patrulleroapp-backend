package cl.patrulleroapp.backend.service;

import cl.patrulleroapp.backend.dto.UsuarioRequest;
import cl.patrulleroapp.backend.dto.UsuarioResponse;
import cl.patrulleroapp.backend.model.Rol;
import cl.patrulleroapp.backend.model.Usuario;
import cl.patrulleroapp.backend.repository.RolRepository;
import cl.patrulleroapp.backend.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
@RequiredArgsConstructor
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final RolRepository rolRepository;
    private final PasswordEncoder passwordEncoder;

    public List<UsuarioResponse> getAll() {
        return usuarioRepository.findAll().stream()
            .map(this::toResponse)
            .toList();
    }

    public UsuarioResponse crear(UsuarioRequest request) {
        if (usuarioRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("El email ya está registrado");
        }
        Rol rol = rolRepository.findById(request.getIdRol())
            .orElseThrow(() -> new RuntimeException("Rol no encontrado"));

        Usuario usuario = new Usuario();
        usuario.setNombre(request.getNombre());
        usuario.setApellido(request.getApellido());
        usuario.setEmail(request.getEmail());
        usuario.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        usuario.setRol(rol);
        usuario.setActivo(true);
        usuarioRepository.save(usuario);

        return toResponse(usuario);
    }

    public UsuarioResponse actualizar(Integer id, UsuarioRequest request) {
        Usuario usuario = usuarioRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        usuario.setNombre(request.getNombre());
        usuario.setApellido(request.getApellido());

        if (request.getPassword() != null && !request.getPassword().isEmpty()) {
            usuario.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        }

        if (request.getIdRol() != null) {
            Rol rol = rolRepository.findById(request.getIdRol())
                .orElseThrow(() -> new RuntimeException("Rol no encontrado"));
            usuario.setRol(rol);
        }

        usuarioRepository.save(usuario);
        return toResponse(usuario);
    }

    public void toggleActivo(Integer id) {
        Usuario usuario = usuarioRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
        usuario.setActivo(!usuario.getActivo());
        usuarioRepository.save(usuario);
    }

    private UsuarioResponse toResponse(Usuario u) {
        return new UsuarioResponse(
            u.getIdUsuario(),
            u.getNombre(),
            u.getApellido(),
            u.getEmail(),
            u.getRol().getNombre(),
            u.getActivo()
        );
    }
}
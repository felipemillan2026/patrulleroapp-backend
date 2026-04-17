package cl.patrulleroapp.backend.service;

import cl.patrulleroapp.backend.dto.LoginRequest;
import cl.patrulleroapp.backend.dto.LoginResponse;
import cl.patrulleroapp.backend.model.Usuario;
import cl.patrulleroapp.backend.repository.UsuarioRepository;
import cl.patrulleroapp.backend.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    public LoginResponse login(LoginRequest request) {
        Usuario usuario = usuarioRepository.findByEmail(request.getEmail())
            .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        if (!passwordEncoder.matches(request.getPassword(), usuario.getPasswordHash())) {
            throw new RuntimeException("Contraseña incorrecta");
        }

        if (!usuario.getActivo()) {
            throw new RuntimeException("Usuario inactivo");
        }

        String rol = usuario.getRol().getNombre();
        String token = jwtUtil.generateToken(usuario.getEmail(), rol);

        return new LoginResponse(
            token,
            usuario.getEmail(),
            usuario.getNombre(),
            usuario.getApellido(),
            rol
        );
    }
}
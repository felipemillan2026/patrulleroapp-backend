package cl.patrulleroapp.backend.dto;

import lombok.Data;
import lombok.AllArgsConstructor;

@Data
@AllArgsConstructor
public class UsuarioResponse {
    private Integer idUsuario;
    private String nombre;
    private String apellido;
    private String email;
    private String rol;
    private Boolean activo;
}
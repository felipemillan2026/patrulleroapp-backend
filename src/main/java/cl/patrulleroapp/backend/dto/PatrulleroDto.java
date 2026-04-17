package cl.patrulleroapp.backend.dto;

import lombok.Data;
import lombok.AllArgsConstructor;

@Data
@AllArgsConstructor
public class PatrulleroDto {
    private Integer idUsuario;
    private String nombre;
    private String apellido;
    private String email;
}
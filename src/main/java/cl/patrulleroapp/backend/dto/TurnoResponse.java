package cl.patrulleroapp.backend.dto;

import lombok.Data;
import lombok.AllArgsConstructor;
import java.time.LocalDateTime;
import java.util.List;

@Data
@AllArgsConstructor
public class TurnoResponse {
    private Integer idTurno;
    private String tipo;
    private String estado;
    private LocalDateTime fechaInicio;
    private LocalDateTime fechaCierre;
    private String supervisorNombre;
    private List<PatrulleroDto> patrulleros;
}
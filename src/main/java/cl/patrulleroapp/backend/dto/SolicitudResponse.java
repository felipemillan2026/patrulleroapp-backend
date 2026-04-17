package cl.patrulleroapp.backend.dto;

import lombok.Data;
import lombok.AllArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@AllArgsConstructor
public class SolicitudResponse {
    private Integer idSolicitud;
    private String descripcion;
    private String estado;
    private LocalDateTime fechaHora;
    private String direccion;
    private BigDecimal latitud;
    private BigDecimal longitud;
    private String patrulleroNombre;
    private String departamentoNombre;
    private List<String> tiposCaso;
}
package cl.patrulleroapp.backend.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.util.List;

@Data
public class SolicitudRequest {
    private String descripcion;
    private BigDecimal latitud;
    private BigDecimal longitud;
    private String direccion;
    private Integer idDepartamento;
    private List<Integer> idTiposCaso;
    private List<String> urlsImagenes;
    private boolean notificarEmail;
    private String emailDestino;
}
package cl.patrulleroapp.backend.dto;

import lombok.Data;
import java.util.List;

@Data
public class TurnoRequest {
    private String tipo;
    private List<Integer> idPatrulleros;
}
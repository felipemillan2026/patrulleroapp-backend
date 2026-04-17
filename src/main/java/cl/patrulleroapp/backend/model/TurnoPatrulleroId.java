package cl.patrulleroapp.backend.model;

import jakarta.persistence.Embeddable;
import lombok.Data;
import java.io.Serializable;

@Data
@Embeddable
public class TurnoPatrulleroId implements Serializable {

    @jakarta.persistence.Column(name = "id_turno")
    private Integer idTurno;

    @jakarta.persistence.Column(name = "id_patrullero")
    private Integer idPatrullero;
}
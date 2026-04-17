package cl.patrulleroapp.backend.model;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "turno_patrullero")
public class TurnoPatrullero {

    @EmbeddedId
    private TurnoPatrulleroId id;

    @ManyToOne(fetch = FetchType.EAGER)
    @MapsId("idTurno")
    @JoinColumn(name = "id_turno")
    private Turno turno;

    @ManyToOne(fetch = FetchType.EAGER)
    @MapsId("idPatrullero")
    @JoinColumn(name = "id_patrullero")
    private Usuario patrullero;

    @Column(name = "estado_asignacion", nullable = false, length = 20)
    private String estadoAsignacion = "activo";
}
package cl.patrulleroapp.backend.model;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "turnos")
public class Turno {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_turno")
    private Integer idTurno;

    @Column(name = "tipo", nullable = false, length = 20)
    private String tipo;

    @Column(name = "fecha_inicio", nullable = false)
    private LocalDateTime fechaInicio;

    @Column(name = "fecha_cierre")
    private LocalDateTime fechaCierre;

    @Column(name = "estado", nullable = false, length = 20)
    private String estado = "activo";

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "id_supervisor", nullable = false)
    private Usuario supervisor;
}
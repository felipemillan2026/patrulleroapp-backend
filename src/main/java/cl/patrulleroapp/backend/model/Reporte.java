package cl.patrulleroapp.backend.model;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "reportes")
public class Reporte {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_reporte")
    private Integer idReporte;

    @Column(name = "fecha_generacion", nullable = false)
    private LocalDateTime fechaGeneracion;

    @Column(name = "url_archivo", nullable = false, length = 500)
    private String urlArchivo;

    @OneToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "id_turno", nullable = false, unique = true)
    private Turno turno;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "id_supervisor", nullable = false)
    private Usuario supervisor;
}
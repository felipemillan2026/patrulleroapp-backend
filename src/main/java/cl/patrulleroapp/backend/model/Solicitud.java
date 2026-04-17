package cl.patrulleroapp.backend.model;

import jakarta.persistence.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Entity
@Table(name = "solicitudes")
public class Solicitud {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_solicitud")
    private Integer idSolicitud;

    @Column(name = "descripcion", nullable = false, columnDefinition = "TEXT")
    private String descripcion;

    @Column(name = "fecha_hora", nullable = false)
    private LocalDateTime fechaHora;

    @Column(name = "estado", nullable = false, length = 30)
    private String estado = "pendiente";

    @Column(name = "latitud", precision = 10, scale = 7)
    private BigDecimal latitud;

    @Column(name = "longitud", precision = 10, scale = 7)
    private BigDecimal longitud;

    @Column(name = "direccion", length = 300)
    private String direccion;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "id_patrullero", nullable = false)
    private Usuario patrullero;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "id_turno", nullable = false)
    private Turno turno;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "id_departamento", nullable = false)
    private Departamento departamento;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
        name = "solicitud_tipo_caso",
        joinColumns = @JoinColumn(name = "id_solicitud"),
        inverseJoinColumns = @JoinColumn(name = "id_tipo_caso")
    )
    private List<TipoCaso> tiposCaso;

    @OneToMany(mappedBy = "solicitud", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Imagen> imagenes;
}
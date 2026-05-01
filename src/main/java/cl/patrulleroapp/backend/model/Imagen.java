package cl.patrulleroapp.backend.model;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "imagenes")
public class Imagen {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_imagen")
    private Integer idImagen;

    // Renombrado de urlFirebase → urlCloudinary
    // La columna en BD se mantiene como url_firebase para no requerir migración
    // Si quieres renombrar la columna en BD ejecuta:
    // ALTER TABLE imagenes RENAME COLUMN url_firebase TO url_cloudinary;
    @Column(name = "url_firebase", nullable = false, length = 500)
    private String urlCloudinary;

    @Column(name = "fecha_subida", nullable = false)
    private LocalDateTime fechaSubida;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_solicitud", nullable = false)
    private Solicitud solicitud;
}

package cl.patrulleroapp.backend.service;

import cl.patrulleroapp.backend.dto.SolicitudRequest;
import cl.patrulleroapp.backend.dto.SolicitudResponse;
import cl.patrulleroapp.backend.model.*;
import cl.patrulleroapp.backend.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class SolicitudService {

    private final SolicitudRepository solicitudRepository;
    private final UsuarioRepository usuarioRepository;
    private final TurnoRepository turnoRepository;
    private final DepartamentoRepository departamentoRepository;
    private final TipoCasoRepository tipoCasoRepository;
    private final ImagenRepository imagenRepository;
    private final EmailService emailService;

    public SolicitudResponse crearSolicitud(SolicitudRequest request) {
        String email = SecurityContextHolder.getContext()
            .getAuthentication().getName();
        Usuario patrullero = usuarioRepository.findByEmail(email)
            .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        Turno turno = turnoRepository.findByEstado("activo")
            .orElseThrow(() -> new RuntimeException("No hay turno activo"));

        Departamento departamento = departamentoRepository
            .findById(request.getIdDepartamento())
            .orElseThrow(() -> new RuntimeException("Departamento no encontrado"));

        List<TipoCaso> tiposCaso = request.getIdTiposCaso().stream()
            .map(id -> tipoCasoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Tipo de caso no encontrado")))
            .toList();

        Solicitud solicitud = new Solicitud();
        solicitud.setDescripcion(request.getDescripcion());
        solicitud.setFechaHora(LocalDateTime.now());
        solicitud.setEstado("pendiente");
        solicitud.setLatitud(request.getLatitud());
        solicitud.setLongitud(request.getLongitud());
        solicitud.setDireccion(request.getDireccion());
        solicitud.setNotas(request.getNotas());
        solicitud.setPatrullero(patrullero);
        solicitud.setTurno(turno);
        solicitud.setDepartamento(departamento);
        solicitud.setTiposCaso(tiposCaso);
        solicitudRepository.save(solicitud);

        if (request.getUrlsImagenes() != null && !request.getUrlsImagenes().isEmpty()) {
            for (String url : request.getUrlsImagenes()) {
                Imagen imagen = new Imagen();
                imagen.setUrlFirebase(url);
                imagen.setFechaSubida(LocalDateTime.now());
                imagen.setSolicitud(solicitud);
                imagenRepository.save(imagen);
            }
        }

        if (request.isNotificarEmail()) {
            emailService.enviarNotificacionSolicitud(solicitud, request.getEmailDestino());
        }

        return toResponse(solicitud);
    }

    public SolicitudResponse editarSolicitud(Integer id, SolicitudRequest request) {
        String email = SecurityContextHolder.getContext()
            .getAuthentication().getName();
        Usuario patrullero = usuarioRepository.findByEmail(email)
            .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        Solicitud solicitud = solicitudRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Solicitud no encontrada"));

        if (!solicitud.getPatrullero().getIdUsuario().equals(patrullero.getIdUsuario())) {
            throw new RuntimeException("No puedes editar una solicitud de otro patrullero");
        }

        solicitud.setDescripcion(request.getDescripcion());
        solicitud.setDireccion(request.getDireccion());

        if (request.getIdTiposCaso() != null && !request.getIdTiposCaso().isEmpty()) {
            List<TipoCaso> tiposCaso = request.getIdTiposCaso().stream()
                .map(tcId -> tipoCasoRepository.findById(tcId)
                    .orElseThrow(() -> new RuntimeException("Tipo de caso no encontrado")))
                .toList();
            solicitud.setTiposCaso(tiposCaso);
        }

        solicitudRepository.save(solicitud);

        if (request.getUrlsImagenes() != null && !request.getUrlsImagenes().isEmpty()) {
            for (String url : request.getUrlsImagenes()) {
                Imagen imagen = new Imagen();
                imagen.setUrlFirebase(url);
                imagen.setFechaSubida(LocalDateTime.now());
                imagen.setSolicitud(solicitud);
                imagenRepository.save(imagen);
            }
        }

        return toResponse(solicitud);
    }

    public SolicitudResponse editarSolicitudCentralista(Integer id, SolicitudRequest request) {
        Solicitud solicitud = solicitudRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Solicitud no encontrada"));

        if (request.getDescripcion() != null)
            solicitud.setDescripcion(request.getDescripcion());

        if (request.getDireccion() != null)
            solicitud.setDireccion(request.getDireccion());

        if (request.getNotas() != null)
            solicitud.setNotas(request.getNotas());

        if (request.getIdTiposCaso() != null && !request.getIdTiposCaso().isEmpty()) {
            List<TipoCaso> tiposCaso = request.getIdTiposCaso().stream()
                .map(tcId -> tipoCasoRepository.findById(tcId)
                    .orElseThrow(() -> new RuntimeException("Tipo de caso no encontrado")))
                .toList();
            solicitud.setTiposCaso(tiposCaso);
        }

        if (request.getIdPatrullero() != null) {
            Usuario patrullero = usuarioRepository.findById(request.getIdPatrullero())
                .orElseThrow(() -> new RuntimeException("Patrullero no encontrado"));
            solicitud.setPatrullero(patrullero);
        }

        solicitudRepository.save(solicitud);
        return toResponse(solicitud);
    }

    public List<SolicitudResponse> getMisSolicitudes() {
        String email = SecurityContextHolder.getContext()
            .getAuthentication().getName();
        Usuario patrullero = usuarioRepository.findByEmail(email)
            .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        return solicitudRepository
            .findByPatrullero_IdUsuario(patrullero.getIdUsuario())
            .stream().map(this::toResponse).toList();
    }

    public List<SolicitudResponse> getSolicitudesTurnoActivo() {
        Turno turno = turnoRepository.findByEstado("activo")
            .orElseThrow(() -> new RuntimeException("No hay turno activo"));

        return solicitudRepository
            .findByTurno_IdTurno(turno.getIdTurno())
            .stream().map(this::toResponse).toList();
    }

    public SolicitudResponse actualizarEstado(Integer id, String estado) {
        Solicitud solicitud = solicitudRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Solicitud no encontrada"));
        solicitud.setEstado(estado);
        solicitudRepository.save(solicitud);
        return toResponse(solicitud);
    }

    private SolicitudResponse toResponse(Solicitud s) {
        List<String> tiposCaso = List.of();
        try {
            if (s.getTiposCaso() != null) {
                tiposCaso = s.getTiposCaso().stream()
                    .map(TipoCaso::getDescripcion)
                    .toList();
            }
        } catch (Exception e) {
            tiposCaso = List.of();
        }

        List<String> urlsImagenes = List.of();
        try {
            List<Imagen> imagenes = imagenRepository
                .findBySolicitud_IdSolicitud(s.getIdSolicitud());
            urlsImagenes = imagenes.stream()
                .map(Imagen::getUrlFirebase)
                .toList();
        } catch (Exception e) {
            urlsImagenes = List.of();
        }

        return new SolicitudResponse(
            s.getIdSolicitud(),
            s.getDescripcion(),
            s.getEstado(),
            s.getFechaHora(),
            s.getDireccion(),
            s.getLatitud(),
            s.getLongitud(),
            s.getPatrullero().getNombre() + " " + s.getPatrullero().getApellido(),
            s.getPatrullero().getIdUsuario(),
            s.getDepartamento().getNombre(),
            s.getDepartamento().getIdDepartamento(),
            tiposCaso,
            urlsImagenes,
            s.getNotas()
        );
    }
}
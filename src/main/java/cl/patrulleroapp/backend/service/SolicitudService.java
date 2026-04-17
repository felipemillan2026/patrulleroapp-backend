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
        solicitud.setPatrullero(patrullero);
        solicitud.setTurno(turno);
        solicitud.setDepartamento(departamento);
        solicitud.setTiposCaso(tiposCaso);
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

    return new SolicitudResponse(
        s.getIdSolicitud(),
        s.getDescripcion(),
        s.getEstado(),
        s.getFechaHora(),
        s.getDireccion(),
        s.getLatitud(),
        s.getLongitud(),
        s.getPatrullero().getNombre() + " " + s.getPatrullero().getApellido(),
        s.getDepartamento().getNombre(),
        tiposCaso
    );
}
}
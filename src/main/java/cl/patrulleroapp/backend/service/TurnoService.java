package cl.patrulleroapp.backend.service;

import cl.patrulleroapp.backend.dto.PatrulleroDto;
import cl.patrulleroapp.backend.dto.TurnoRequest;
import cl.patrulleroapp.backend.dto.TurnoResponse;
import cl.patrulleroapp.backend.model.*;
import cl.patrulleroapp.backend.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class TurnoService {

    private final TurnoRepository turnoRepository;
    private final TurnoPatrulleroRepository turnoPatrulleroRepository;
    private final UsuarioRepository usuarioRepository;

    public TurnoResponse abrirTurno(TurnoRequest request) {
        Optional<Turno> turnoActivo = turnoRepository.findByEstado("activo");
        if (turnoActivo.isPresent()) {
            throw new RuntimeException("Ya existe un turno activo");
        }

        String email = SecurityContextHolder.getContext()
            .getAuthentication().getName();
        Usuario supervisor = usuarioRepository.findByEmail(email)
            .orElseThrow(() -> new RuntimeException("Supervisor no encontrado"));

        Turno turno = new Turno();
        turno.setTipo(request.getTipo());
        turno.setFechaInicio(LocalDateTime.now());
        turno.setEstado("activo");
        turno.setSupervisor(supervisor);
        turnoRepository.save(turno);

        List<PatrulleroDto> patrullerosDto = request.getIdPatrulleros().stream()
            .map(idPatrullero -> {
                Usuario patrullero = usuarioRepository.findById(idPatrullero)
                    .orElseThrow(() -> new RuntimeException("Patrullero no encontrado"));

                TurnoPatrulleroId tpId = new TurnoPatrulleroId();
                tpId.setIdTurno(turno.getIdTurno());
                tpId.setIdPatrullero(idPatrullero);

                TurnoPatrullero tp = new TurnoPatrullero();
                tp.setId(tpId);
                tp.setTurno(turno);
                tp.setPatrullero(patrullero);
                tp.setEstadoAsignacion("activo");
                turnoPatrulleroRepository.save(tp);

                return new PatrulleroDto(
                    patrullero.getIdUsuario(),
                    patrullero.getNombre(),
                    patrullero.getApellido(),
                    patrullero.getEmail()
                );
            }).toList();

        return new TurnoResponse(
            turno.getIdTurno(),
            turno.getTipo(),
            turno.getEstado(),
            turno.getFechaInicio(),
            null,
            supervisor.getNombre() + " " + supervisor.getApellido(),
            patrullerosDto
        );
    }

    public TurnoResponse getTurnoActivo() {
        Turno turno = turnoRepository.findByEstado("activo")
            .orElseThrow(() -> new RuntimeException("No hay turno activo"));

        List<PatrulleroDto> patrulleros = turnoPatrulleroRepository
            .findByTurno_IdTurno(turno.getIdTurno()).stream()
            .map(tp -> new PatrulleroDto(
                tp.getPatrullero().getIdUsuario(),
                tp.getPatrullero().getNombre(),
                tp.getPatrullero().getApellido(),
                tp.getPatrullero().getEmail()
            )).toList();

        return new TurnoResponse(
            turno.getIdTurno(),
            turno.getTipo(),
            turno.getEstado(),
            turno.getFechaInicio(),
            turno.getFechaCierre(),
            turno.getSupervisor().getNombre() + " " + turno.getSupervisor().getApellido(),
            patrulleros
        );
    }

    public TurnoResponse cerrarTurno() {
        Turno turno = turnoRepository.findByEstado("activo")
            .orElseThrow(() -> new RuntimeException("No hay turno activo"));

        turno.setEstado("cerrado");
        turno.setFechaCierre(LocalDateTime.now());
        turnoRepository.save(turno);

        return new TurnoResponse(
            turno.getIdTurno(),
            turno.getTipo(),
            turno.getEstado(),
            turno.getFechaInicio(),
            turno.getFechaCierre(),
            turno.getSupervisor().getNombre() + " " + turno.getSupervisor().getApellido(),
            List.of()
        );
    }

    public TurnoResponse getUltimoCerrado() {
        Turno turno = turnoRepository.findUltimoCerrado()
            .orElseThrow(() -> new RuntimeException("No hay turnos cerrados"));

        return new TurnoResponse(
            turno.getIdTurno(),
            turno.getTipo(),
            turno.getEstado(),
            turno.getFechaInicio(),
            turno.getFechaCierre(),
            turno.getSupervisor().getNombre() + " " + turno.getSupervisor().getApellido(),
            List.of()
        );
    }

    public List<PatrulleroDto> getPatrulleros() {
        return usuarioRepository.findAll().stream()
            .filter(u -> u.getRol().getNombre().equals("patrullero"))
            .filter(u -> u.getActivo())
            .map(u -> new PatrulleroDto(
                u.getIdUsuario(),
                u.getNombre(),
                u.getApellido(),
                u.getEmail()
            )).toList();
    }
}
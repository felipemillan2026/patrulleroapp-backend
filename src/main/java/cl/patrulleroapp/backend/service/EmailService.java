package cl.patrulleroapp.backend.service;

import cl.patrulleroapp.backend.model.Solicitud;
import lombok.RequiredArgsConstructor;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import jakarta.mail.internet.MimeMessage;
import java.time.format.DateTimeFormatter;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;

    private static final DateTimeFormatter FMT =
        DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    /**
     * Envía notificación al correoDestino del departamento asociado a la solicitud.
     * @Async: no bloquea el hilo del request — el correo sale en segundo plano.
     */
    @Async
    public void enviarNotificacionSolicitud(Solicitud solicitud) {
        try {
            String correoDestino = solicitud.getDepartamento().getCorreoDestino();

            if (correoDestino == null || correoDestino.isBlank()) {
                System.err.println("[Email] Departamento sin correo destino, se omite envío. " +
                    "Solicitud #" + solicitud.getIdSolicitud());
                return;
            }

            String departamento = solicitud.getDepartamento().getNombre();
            String patrullero   = solicitud.getPatrullero().getNombre() + " "
                                + solicitud.getPatrullero().getApellido();

            // Tipos de caso como lista
            String tiposCaso = "No especificado";
            try {
                if (solicitud.getTiposCaso() != null && !solicitud.getTiposCaso().isEmpty()) {
                    tiposCaso = solicitud.getTiposCaso().stream()
                        .map(tc -> tc.getDescripcion())
                        .collect(Collectors.joining(", "));
                }
            } catch (Exception ignored) {}

            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom("rohirrimfelipe666@gmail.com");
            helper.setTo(correoDestino);
            helper.setSubject("🛡️ Nueva solicitud — " + departamento + " · PatrulleroApp");

            String html = """
                <div style="font-family:Arial,sans-serif;max-width:620px;margin:0 auto;border:1px solid #e5e7eb;border-radius:10px;overflow:hidden;">

                  <!-- Header -->
                  <div style="background:#1a2b4a;padding:24px 28px;">
                    <h1 style="color:#fff;margin:0;font-size:20px;font-weight:700;">
                      🛡️ PatrulleroApp
                    </h1>
                    <p style="color:#93a8c8;margin:4px 0 0;font-size:13px;">
                      Sistema de Gestión de Patrullaje Municipal
                    </p>
                  </div>

                  <!-- Alerta -->
                  <div style="background:#dbeafe;padding:14px 28px;border-left:4px solid #2563eb;">
                    <p style="margin:0;color:#1e3a5f;font-size:15px;font-weight:600;">
                      Nueva solicitud de procedimiento para su departamento
                    </p>
                  </div>

                  <!-- Detalle -->
                  <div style="padding:24px 28px;background:#f8fafc;">
                    <table style="width:100%%;border-collapse:collapse;font-size:14px;">

                      <tr>
                        <td style="padding:9px 12px;background:#e8f0fe;font-weight:600;color:#374151;width:38%%;">
                          N° Solicitud
                        </td>
                        <td style="padding:9px 12px;background:#ffffff;color:#111827;">
                          <strong>#%d</strong>
                        </td>
                      </tr>

                      <tr>
                        <td style="padding:9px 12px;background:#e8f0fe;font-weight:600;color:#374151;">
                          Departamento
                        </td>
                        <td style="padding:9px 12px;background:#f9fafb;color:#111827;">
                          %s
                        </td>
                      </tr>

                      <tr>
                        <td style="padding:9px 12px;background:#e8f0fe;font-weight:600;color:#374151;">
                          Patrullero
                        </td>
                        <td style="padding:9px 12px;background:#ffffff;color:#111827;">
                          %s
                        </td>
                      </tr>

                      <tr>
                        <td style="padding:9px 12px;background:#e8f0fe;font-weight:600;color:#374151;">
                          Fecha y hora
                        </td>
                        <td style="padding:9px 12px;background:#f9fafb;color:#111827;">
                          %s
                        </td>
                      </tr>

                      <tr>
                        <td style="padding:9px 12px;background:#e8f0fe;font-weight:600;color:#374151;">
                          Dirección
                        </td>
                        <td style="padding:9px 12px;background:#ffffff;color:#111827;">
                          %s
                        </td>
                      </tr>

                      <tr>
                        <td style="padding:9px 12px;background:#e8f0fe;font-weight:600;color:#374151;">
                          Tipos de caso
                        </td>
                        <td style="padding:9px 12px;background:#f9fafb;color:#111827;">
                          %s
                        </td>
                      </tr>

                      <tr>
                        <td style="padding:9px 12px;background:#e8f0fe;font-weight:600;color:#374151;vertical-align:top;">
                          Descripción
                        </td>
                        <td style="padding:9px 12px;background:#ffffff;color:#111827;">
                          %s
                        </td>
                      </tr>

                    </table>
                  </div>

                  <!-- Footer -->
                  <div style="background:#1a2b4a;padding:14px 28px;text-align:center;">
                    <p style="color:#93a8c8;margin:0;font-size:11px;">
                      Municipalidad · PatrulleroApp · Gestión de procedimientos en terreno
                    </p>
                  </div>

                </div>
                """.formatted(
                    solicitud.getIdSolicitud(),
                    departamento,
                    patrullero,
                    solicitud.getFechaHora().format(FMT),
                    solicitud.getDireccion() != null && !solicitud.getDireccion().isBlank()
                        ? solicitud.getDireccion() : "No registrada",
                    tiposCaso,
                    solicitud.getDescripcion()
                );

            helper.setText(html, true);
            mailSender.send(message);

            System.out.println("[Email] ✓ Notificación enviada a " + correoDestino
                + " — Solicitud #" + solicitud.getIdSolicitud());

        } catch (Exception e) {
            System.err.println("[Email] ✗ Error al enviar — Solicitud #"
                + solicitud.getIdSolicitud() + ": " + e.getMessage());
        }
    }
}

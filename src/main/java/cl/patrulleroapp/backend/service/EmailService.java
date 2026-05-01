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

/**
 * ═══════════════════════════════════════════════════════════════════
 * SERVICIO DE NOTIFICACIONES POR CORREO — PatrulleroApp
 * ═══════════════════════════════════════════════════════════════════
 *
 * RESPONSABILIDAD:
 * Enviar un correo HTML automático al departamento responsable cada
 * vez que un patrullero registra una nueva solicitud de procedimiento.
 *
 * ¿A QUÉ CORREO SE ENVÍA?
 * Al campo "correo_destino" del departamento seleccionado en la
 * solicitud. Este campo se gestiona directamente en la base de datos,
 * en la tabla "departamentos". No requiere modificar código para
 * cambiar destinatarios.
 *
 * ¿DÓNDE SE REGISTRAN Y MODIFICAN LOS CORREOS?
 * En la tabla MySQL "departamentos", columna "correo_destino".
 * Consultar el archivo correos_departamentos.sql para instrucciones
 * detalladas de cómo agregar, cambiar o revisar correos.
 *
 * CONFIGURACIÓN SMTP (application-prod.properties):
 * - Servidor: smtp.gmail.com puerto 587 (STARTTLS)
 * - Remitente: definido en SPRING_MAIL_USERNAME (variable Railway)
 * - Se requiere una "App Password" de Google, no la contraseña normal
 *
 * COMPORTAMIENTO ASÍNCRONO (@Async):
 * El correo se envía en un hilo separado. El backend retorna HTTP 200
 * al patrullero de forma inmediata sin esperar a que el correo salga.
 * Esto evita timeouts visibles y mejora la experiencia de usuario.
 * Requiere @EnableAsync en BackendApplication.java.
 * ═══════════════════════════════════════════════════════════════════
 */
@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;

    private static final DateTimeFormatter FMT =
        DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    /**
     * Envía notificación HTML al correo_destino del departamento.
     *
     * @param solicitud La solicitud recién creada con todos sus datos.
     *
     * NOTA: El destinatario se obtiene de solicitud.getDepartamento().getCorreoDestino()
     * Para cambiar a qué correo llega, actualizar ese campo en la BD:
     *   UPDATE departamentos SET correo_destino = 'nuevo@correo.cl'
     *   WHERE id_departamento = X;
     */
    @Async
    public void enviarNotificacionSolicitud(Solicitud solicitud) {
        try {
            // ── DESTINATARIO ────────────────────────────────────────────────
            // El correo va al campo correo_destino del departamento.
            // Para modificar destinatarios: actualizar tabla "departamentos"
            // en MySQL, columna "correo_destino". Ver correos_departamentos.sql
            String correoDestino = solicitud.getDepartamento().getCorreoDestino();

            if (correoDestino == null || correoDestino.isBlank()) {
                System.err.println("[Email] ⚠ Departamento '"
                    + solicitud.getDepartamento().getNombre()
                    + "' no tiene correo_destino configurado. "
                    + "Ejecutar: UPDATE departamentos SET correo_destino = 'correo@municipio.cl' "
                    + "WHERE id_departamento = " + solicitud.getDepartamento().getIdDepartamento());
                return;
            }

            // ── DATOS DE LA SOLICITUD ────────────────────────────────────────
            String departamento = solicitud.getDepartamento().getNombre();
            String patrullero   = solicitud.getPatrullero().getNombre() + " "
                                + solicitud.getPatrullero().getApellido();

            String tiposCaso = "No especificado";
            try {
                if (solicitud.getTiposCaso() != null && !solicitud.getTiposCaso().isEmpty()) {
                    tiposCaso = solicitud.getTiposCaso().stream()
                        .map(tc -> tc.getDescripcion())
                        .collect(Collectors.joining(", "));
                }
            } catch (Exception ignored) {}

            // ── CONSTRUCCIÓN DEL MENSAJE ─────────────────────────────────────
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom("rohirrimfelipe666@gmail.com");
            helper.setTo(correoDestino);
            helper.setSubject("🛡️ Nueva solicitud — " + departamento + " · PatrulleroApp");

            // ── CUERPO HTML ──────────────────────────────────────────────────
            String html = """
                <div style="font-family:Arial,sans-serif;max-width:620px;margin:0 auto;border:1px solid #e5e7eb;border-radius:10px;overflow:hidden;">

                  <!-- Encabezado -->
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

                  <!-- Pie -->
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
                + " — Solicitud #" + solicitud.getIdSolicitud()
                + " — Departamento: " + departamento);

        } catch (Exception e) {
            System.err.println("[Email] ✗ Error al enviar — Solicitud #"
                + solicitud.getIdSolicitud() + ": " + e.getMessage());
        }
    }
}

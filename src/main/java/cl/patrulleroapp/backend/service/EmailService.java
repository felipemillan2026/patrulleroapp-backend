package cl.patrulleroapp.backend.service;

import cl.patrulleroapp.backend.model.Solicitud;
import lombok.RequiredArgsConstructor;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import jakarta.mail.internet.MimeMessage;
import java.time.format.DateTimeFormatter;

@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;

    private static final DateTimeFormatter FMT =
        DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    public void enviarNotificacionSolicitud(Solicitud solicitud, String emailDestino) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            String correo = (emailDestino != null && !emailDestino.isEmpty())
                ? emailDestino
                : solicitud.getDepartamento().getCorreoDestino();

            String departamento = solicitud.getDepartamento().getNombre();
            String patrullero   = solicitud.getPatrullero().getNombre() + " "
                                + solicitud.getPatrullero().getApellido();

            helper.setFrom("rohirrimfelipe666@gmail.com");
            helper.setTo(correo);
            helper.setSubject("🛡️ Nueva solicitud de procedimiento — PatrulleroApp");

            String html = """
                <div style="font-family:Arial,sans-serif;max-width:600px;margin:0 auto;">
                  <div style="background:#1a2b4a;padding:24px;border-radius:8px 8px 0 0;">
                    <h1 style="color:#fff;margin:0;font-size:22px;">🛡️ PatrulleroApp</h1>
                    <p style="color:#aab4c8;margin:4px 0 0;">Sistema de Gestión Municipal</p>
                  </div>
                  <div style="background:#f8fafc;padding:24px;border:1px solid #e5e7eb;">
                    <h2 style="color:#1a2b4a;margin-top:0;">Nueva solicitud de procedimiento</h2>
                    <table style="width:100%%;border-collapse:collapse;">
                      <tr>
                        <td style="padding:8px;background:#eef2ff;font-weight:bold;width:35%%;">N° Solicitud</td>
                        <td style="padding:8px;background:#f8fafc;">#%d</td>
                      </tr>
                      <tr>
                        <td style="padding:8px;background:#eef2ff;font-weight:bold;">Departamento</td>
                        <td style="padding:8px;background:#f8fafc;">%s</td>
                      </tr>
                      <tr>
                        <td style="padding:8px;background:#eef2ff;font-weight:bold;">Patrullero</td>
                        <td style="padding:8px;background:#f8fafc;">%s</td>
                      </tr>
                      <tr>
                        <td style="padding:8px;background:#eef2ff;font-weight:bold;">Fecha y hora</td>
                        <td style="padding:8px;background:#f8fafc;">%s</td>
                      </tr>
                      <tr>
                        <td style="padding:8px;background:#eef2ff;font-weight:bold;">Dirección</td>
                        <td style="padding:8px;background:#f8fafc;">%s</td>
                      </tr>
                      <tr>
                        <td style="padding:8px;background:#eef2ff;font-weight:bold;">Descripción</td>
                        <td style="padding:8px;background:#f8fafc;">%s</td>
                      </tr>
                    </table>
                  </div>
                  <div style="background:#1a2b4a;padding:12px;border-radius:0 0 8px 8px;text-align:center;">
                    <p style="color:#aab4c8;margin:0;font-size:12px;">
                      Municipalidad · PatrulleroApp · Gestión de procedimientos en terreno
                    </p>
                  </div>
                </div>
                """.formatted(
                    solicitud.getIdSolicitud(),
                    departamento,
                    patrullero,
                    solicitud.getFechaHora().format(FMT),
                    solicitud.getDireccion() != null ? solicitud.getDireccion() : "No registrada",
                    solicitud.getDescripcion()
                );

            helper.setText(html, true);
            mailSender.send(message);

        } catch (Exception e) {
            System.err.println("Error al enviar email: " + e.getMessage());
        }
    }
}
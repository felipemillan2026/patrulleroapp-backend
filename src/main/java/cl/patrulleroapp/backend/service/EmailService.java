package cl.patrulleroapp.backend.service;

import cl.patrulleroapp.backend.model.Solicitud;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

/**
 * ═══════════════════════════════════════════════════════════════════
 * SERVICIO DE NOTIFICACIONES POR CORREO — PatrulleroApp
 * ═══════════════════════════════════════════════════════════════════
 *
 * IMPLEMENTACIÓN: Resend API (HTTP)
 * Railway bloquea SMTP (puertos 465 y 587) en su plan gratuito.
 * Por eso se usa Resend, que envía correos via HTTPS API.
 * No requiere SMTP ni dependencias adicionales en pom.xml.
 *
 * ¿A QUÉ CORREO SE ENVÍA?
 * Al campo "correo_destino" de la tabla "departamentos" en MySQL.
 * Para cambiar destinatarios, actualizar ese campo en la BD:
 *   UPDATE departamentos
 *   SET correo_destino = 'nuevo@correo.cl'
 *   WHERE id_departamento = X;
 *
 * Para poner el mismo correo en todos los departamentos:
 *   UPDATE departamentos SET correo_destino = 'correo@iplacex.cl';
 *
 * ¿DÓNDE SE CONFIGURA LA API KEY DE RESEND?
 * En Railway como variable de entorno: RESEND_API_KEY
 *
 * ¿CÓMO AGREGAR MÁS DEPARTAMENTOS CON SUS CORREOS?
 *   INSERT INTO departamentos (nombre, codigo, correo_destino, activo)
 *   VALUES ('Nuevo Depto', 'COD', 'depto@municipio.cl', 1);
 *
 * IMÁGENES EN EL CORREO:
 * Las imágenes se embeben directamente en el HTML usando sus URLs
 * públicas de Cloudinary. El destinatario las ve sin necesidad de
 * iniciar sesión en el sistema.
 * ═══════════════════════════════════════════════════════════════════
 */
@Service
public class EmailService {

    @Value("${resend.api.key}")
    private String resendApiKey;

    private static final String RESEND_URL = "https://api.resend.com/emails";
    private static final DateTimeFormatter FMT =
        DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    /**
     * Envía notificación HTML con imágenes embebidas al correo_destino del departamento.
     *
     * @param solicitud    La solicitud recién creada con todos sus datos.
     * @param urlsImagenes Lista de URLs públicas de Cloudinary para embeber en el correo.
     *
     * Las imágenes vienen de Cloudinary — son URLs públicas, el destinatario
     * las ve directamente en el correo sin necesitar credenciales del sistema.
     *
     * Para cambiar el destinatario:
     *   UPDATE departamentos SET correo_destino = 'nuevo@correo.cl'
     *   WHERE id_departamento = X;
     */
    @Async
    public void enviarNotificacionSolicitud(Solicitud solicitud, List<String> urlsImagenes) {
        try {
            // ── DESTINATARIO ─────────────────────────────────────────────────
            // Viene del campo correo_destino de la tabla departamentos en MySQL.
            // Para modificarlo: UPDATE departamentos SET correo_destino = '...'
            String correoDestino = solicitud.getDepartamento().getCorreoDestino();

            if (correoDestino == null || correoDestino.isBlank()) {
                System.err.println("[Email] Sin correo_destino en departamento #"
                    + solicitud.getDepartamento().getIdDepartamento());
                return;
            }

            // ── DATOS ─────────────────────────────────────────────────────────
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

            String direccion = (solicitud.getDireccion() != null && !solicitud.getDireccion().isBlank())
                ? solicitud.getDireccion() : "No registrada";

            String asunto = "Nueva solicitud #" + solicitud.getIdSolicitud()
                + " — " + departamento + " · PatrulleroApp";

            // ── BLOQUE DE IMÁGENES ────────────────────────────────────────────
            // Las URLs vienen de Cloudinary (almacenamiento público de imágenes).
            // Se embeben directamente en el HTML — el destinatario las ve sin login.
            StringBuilder imgBlock = new StringBuilder();
            if (urlsImagenes != null && !urlsImagenes.isEmpty()) {
                imgBlock.append("<div style='padding:16px 28px;background:#f0f9ff;"
                    + "border-top:1px solid #e5e7eb;'>");
                imgBlock.append("<p style='margin:0 0 12px;font-size:13px;font-weight:600;"
                    + "color:#1e3a5f;'>Imagenes de evidencia (")
                    .append(urlsImagenes.size()).append("):</p>");
                imgBlock.append("<div style='display:flex;gap:10px;flex-wrap:wrap;'>");
                for (String url : urlsImagenes) {
                    imgBlock.append("<a href='").append(url)
                        .append("' target='_blank' style='display:inline-block;'>")
                        .append("<img src='").append(url).append("' ")
                        .append("style='width:160px;height:120px;object-fit:cover;")
                        .append("border-radius:8px;border:2px solid #bae6fd;' ")
                        .append("alt='Evidencia'/></a>");
                }
                imgBlock.append("</div></div>");
            } else {
                imgBlock.append("<div style='padding:12px 28px;background:#f9fafb;"
                    + "border-top:1px solid #e5e7eb;'>"
                    + "<p style='margin:0;font-size:13px;color:#9ca3af;font-style:italic;'>"
                    + "Sin imagenes adjuntas en esta solicitud.</p></div>");
            }

            // ── HTML COMPLETO ─────────────────────────────────────────────────
            String html = "<div style='font-family:Arial,sans-serif;max-width:640px;"
                + "margin:0 auto;border:1px solid #e5e7eb;border-radius:10px;overflow:hidden;'>"

                // Header
                + "<div style='background:#1a2b4a;padding:24px 28px;'>"
                + "<h1 style='color:#fff;margin:0;font-size:20px;'>PatrulleroApp</h1>"
                + "<p style='color:#93a8c8;margin:4px 0 0;font-size:13px;'>"
                + "Sistema de Gestion de Patrullaje Municipal</p>"
                + "</div>"

                // Alerta
                + "<div style='background:#dbeafe;padding:14px 28px;"
                + "border-left:4px solid #2563eb;'>"
                + "<p style='margin:0;color:#1e3a5f;font-size:15px;font-weight:600;'>"
                + "Nueva solicitud de procedimiento para su departamento</p>"
                + "</div>"

                // Datos
                + "<div style='padding:24px 28px;background:#f8fafc;'>"
                + "<table style='width:100%;border-collapse:collapse;font-size:14px;'>"
                + fila("N Solicitud", "<strong>#" + solicitud.getIdSolicitud() + "</strong>")
                + fila("Departamento", departamento)
                + fila("Patrullero", patrullero)
                + fila("Fecha y hora", solicitud.getFechaHora().format(FMT))
                + fila("Direccion", direccion)
                + fila("Tipos de caso", tiposCaso)
                + fila("Descripcion", solicitud.getDescripcion())
                + "</table>"
                + "</div>"

                // Bloque imágenes
                + imgBlock

                // Footer
                + "<div style='background:#1a2b4a;padding:14px 28px;text-align:center;'>"
                + "<p style='color:#93a8c8;margin:0;font-size:11px;'>"
                + "Municipalidad · PatrulleroApp · Gestion de procedimientos en terreno</p>"
                + "</div>"
                + "</div>";

            // ── LLAMADA A RESEND API ──────────────────────────────────────────
            String htmlEscaped = html
                .replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "");

            String jsonBody = "{"
                + "\"from\":\"PatrulleroApp <onboarding@resend.dev>\","
                + "\"to\":[\"" + correoDestino + "\"],"
                + "\"subject\":\"" + asunto + "\","
                + "\"html\":\"" + htmlEscaped + "\""
                + "}";

            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(RESEND_URL))
                .header("Authorization", "Bearer " + resendApiKey)
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                .build();

            HttpResponse<String> response = client.send(request,
                HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200 || response.statusCode() == 201) {
                System.out.println("[Email] ✓ Enviado a " + correoDestino
                    + " — Solicitud #" + solicitud.getIdSolicitud()
                    + " — Imagenes: " + (urlsImagenes != null ? urlsImagenes.size() : 0));
            } else {
                System.err.println("[Email] ✗ Resend " + response.statusCode()
                    + ": " + response.body());
            }

        } catch (Exception e) {
            System.err.println("[Email] ✗ Error — Solicitud #"
                + solicitud.getIdSolicitud() + ": " + e.getMessage());
        }
    }

    private String fila(String label, String valor) {
        return "<tr>"
            + "<td style='padding:9px 12px;background:#e8f0fe;font-weight:600;"
            + "color:#374151;width:38%;'>" + label + "</td>"
            + "<td style='padding:9px 12px;background:#fff;color:#111827;'>" + valor + "</td>"
            + "</tr>";
    }
}
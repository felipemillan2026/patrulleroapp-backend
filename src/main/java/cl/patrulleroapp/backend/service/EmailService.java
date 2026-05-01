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
import java.util.stream.Collectors;

@Service
public class EmailService {

    @Value("${resend.api.key}")
    private String resendApiKey;

    private static final String RESEND_URL = "https://api.resend.com/emails";
    private static final DateTimeFormatter FMT =
        DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    @Async
    public void enviarNotificacionSolicitud(Solicitud solicitud) {
        try {
            String correoDestino = solicitud.getDepartamento().getCorreoDestino();

            if (correoDestino == null || correoDestino.isBlank()) {
                System.err.println("[Email] Sin correo_destino en departamento #"
                    + solicitud.getDepartamento().getIdDepartamento());
                return;
            }

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

            String asunto = "Nueva solicitud — " + departamento + " · PatrulleroApp";

            String html = buildHtml(
                solicitud.getIdSolicitud(), departamento, patrullero,
                solicitud.getFechaHora().format(FMT), direccion,
                tiposCaso, solicitud.getDescripcion()
            );

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
                    + " — Solicitud #" + solicitud.getIdSolicitud());
            } else {
                System.err.println("[Email] ✗ Resend " + response.statusCode()
                    + ": " + response.body());
            }

        } catch (Exception e) {
            System.err.println("[Email] ✗ Error — Solicitud #"
                + solicitud.getIdSolicitud() + ": " + e.getMessage());
        }
    }

    private String buildHtml(int id, String depto, String patrullero,
                              String fecha, String dir, String tipos, String desc) {
        return "<div style='font-family:Arial,sans-serif;max-width:620px;margin:0 auto;"
            + "border:1px solid #e5e7eb;border-radius:10px;overflow:hidden;'>"
            + "<div style='background:#1a2b4a;padding:24px 28px;'>"
            + "<h1 style='color:#fff;margin:0;font-size:20px;'>PatrulleroApp</h1>"
            + "<p style='color:#93a8c8;margin:4px 0 0;font-size:13px;'>Sistema de Gestion Municipal</p>"
            + "</div>"
            + "<div style='background:#dbeafe;padding:14px 28px;border-left:4px solid #2563eb;'>"
            + "<p style='margin:0;color:#1e3a5f;font-size:15px;font-weight:600;'>Nueva solicitud de procedimiento</p>"
            + "</div>"
            + "<div style='padding:24px 28px;background:#f8fafc;'>"
            + "<table style='width:100%;border-collapse:collapse;font-size:14px;'>"
            + fila("N Solicitud", "#" + id)
            + fila("Departamento", depto)
            + fila("Patrullero", patrullero)
            + fila("Fecha y hora", fecha)
            + fila("Direccion", dir)
            + fila("Tipos de caso", tipos)
            + fila("Descripcion", desc)
            + "</table></div>"
            + "<div style='background:#1a2b4a;padding:14px 28px;text-align:center;'>"
            + "<p style='color:#93a8c8;margin:0;font-size:11px;'>Municipalidad · PatrulleroApp</p>"
            + "</div></div>";
    }

    private String fila(String label, String valor) {
        return "<tr>"
            + "<td style='padding:9px 12px;background:#e8f0fe;font-weight:600;color:#374151;width:38%;'>" + label + "</td>"
            + "<td style='padding:9px 12px;background:#fff;color:#111827;'>" + valor + "</td>"
            + "</tr>";
    }
}
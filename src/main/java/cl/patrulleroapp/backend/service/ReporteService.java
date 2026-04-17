package cl.patrulleroapp.backend.service;

import cl.patrulleroapp.backend.model.*;
import cl.patrulleroapp.backend.repository.*;
import com.itextpdf.text.*;
import com.itextpdf.text.pdf.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.io.ByteArrayOutputStream;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ReporteService {

    private final TurnoRepository turnoRepository;
    private final SolicitudRepository solicitudRepository;
    private final ReporteRepository reporteRepository;

    private static final DateTimeFormatter FMT =
        DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    public byte[] generarReporteTurno(Integer idTurno) throws Exception {
        Turno turno = turnoRepository.findById(idTurno)
            .orElseThrow(() -> new RuntimeException("Turno no encontrado"));

        List<Solicitud> solicitudes = solicitudRepository
            .findByTurno_IdTurno(idTurno);

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        Document doc = new Document(PageSize.A4, 40, 40, 60, 40);
        PdfWriter.getInstance(doc, baos);
        doc.open();

        // ── Colores ──────────────────────────────────────────
        BaseColor azulOscuro  = new BaseColor(26, 43, 74);
        BaseColor azulMedio   = new BaseColor(46, 74, 122);
        BaseColor grisClaro   = new BaseColor(240, 242, 245);
        BaseColor grisLinea   = new BaseColor(229, 231, 235);
        BaseColor blanco      = BaseColor.WHITE;
        BaseColor amarillo    = new BaseColor(254, 243, 199);
        BaseColor verde       = new BaseColor(220, 252, 231);
        BaseColor grisTexto   = new BaseColor(107, 114, 128);

        // ── Fuentes ──────────────────────────────────────────
        Font fTitulo    = new Font(Font.FontFamily.HELVETICA, 22, Font.BOLD, blanco);
        Font fSubtitulo = new Font(Font.FontFamily.HELVETICA, 11, Font.NORMAL, blanco);
        Font fSeccion   = new Font(Font.FontFamily.HELVETICA, 13, Font.BOLD, azulOscuro);
        Font fLabel     = new Font(Font.FontFamily.HELVETICA, 10, Font.BOLD, grisTexto);
        Font fValor     = new Font(Font.FontFamily.HELVETICA, 11, Font.NORMAL, azulOscuro);
        Font fTabHead   = new Font(Font.FontFamily.HELVETICA, 10, Font.BOLD, blanco);
        Font fTabCell   = new Font(Font.FontFamily.HELVETICA, 9,  Font.NORMAL, azulOscuro);
        Font fFooter    = new Font(Font.FontFamily.HELVETICA, 8,  Font.ITALIC, grisTexto);

        // ── HEADER ───────────────────────────────────────────
        PdfPTable header = new PdfPTable(1);
        header.setWidthPercentage(100);
        header.setSpacingAfter(20);

        PdfPCell headerCell = new PdfPCell();
        headerCell.setBackgroundColor(azulOscuro);
        headerCell.setPadding(20);
        headerCell.setBorder(Rectangle.NO_BORDER);

        Paragraph titulo = new Paragraph("🛡 PatrulleroApp", fTitulo);
        titulo.setAlignment(Element.ALIGN_CENTER);
        headerCell.addElement(titulo);

        Paragraph subtitulo = new Paragraph("Reporte de Cierre de Turno", fSubtitulo);
        subtitulo.setAlignment(Element.ALIGN_CENTER);
        headerCell.addElement(subtitulo);

        header.addCell(headerCell);
        doc.add(header);

        // ── INFO TURNO ───────────────────────────────────────
        Paragraph secTurno = new Paragraph("Información del Turno", fSeccion);
        secTurno.setSpacingBefore(10);
        secTurno.setSpacingAfter(10);
        doc.add(secTurno);

        PdfPTable infoTurno = new PdfPTable(4);
        infoTurno.setWidthPercentage(100);
        infoTurno.setWidths(new float[]{1, 2, 1, 2});
        infoTurno.setSpacingAfter(20);

        agregarParInfoTurno(infoTurno, "Tipo de turno",
            turno.getTipo().toUpperCase(), fLabel, fValor, grisClaro);
        agregarParInfoTurno(infoTurno, "Estado",
            turno.getEstado().toUpperCase(), fLabel, fValor, grisClaro);
        agregarParInfoTurno(infoTurno, "Inicio",
            turno.getFechaInicio().format(FMT), fLabel, fValor, grisClaro);
        agregarParInfoTurno(infoTurno, "Cierre",
            turno.getFechaCierre() != null
                ? turno.getFechaCierre().format(FMT) : "—",
            fLabel, fValor, grisClaro);
        agregarParInfoTurno(infoTurno, "Supervisor",
            turno.getSupervisor().getNombre() + " " +
            turno.getSupervisor().getApellido(),
            fLabel, fValor, grisClaro);
        agregarParInfoTurno(infoTurno, "Total solicitudes",
            String.valueOf(solicitudes.size()), fLabel, fValor, grisClaro);

        doc.add(infoTurno);

        // ── RESUMEN ESTADÍSTICAS ──────────────────────────────
        Paragraph secEstadisticas = new Paragraph("Resumen de Solicitudes", fSeccion);
        secEstadisticas.setSpacingAfter(10);
        doc.add(secEstadisticas);

        long pendientes = solicitudes.stream()
            .filter(s -> s.getEstado().equals("pendiente")).count();
        long enProceso = solicitudes.stream()
            .filter(s -> s.getEstado().equals("en_proceso")).count();
        long cerradas = solicitudes.stream()
            .filter(s -> s.getEstado().equals("cerrada")).count();

        PdfPTable resumen = new PdfPTable(3);
        resumen.setWidthPercentage(60);
        resumen.setHorizontalAlignment(Element.ALIGN_LEFT);
        resumen.setSpacingAfter(20);

        agregarCeldaResumen(resumen, "Pendientes",
            String.valueOf(pendientes), amarillo, fLabel, fValor);
        agregarCeldaResumen(resumen, "En Proceso",
            String.valueOf(enProceso),
            new BaseColor(219, 234, 254), fLabel, fValor);
        agregarCeldaResumen(resumen, "Cerradas",
            String.valueOf(cerradas), verde, fLabel, fValor);

        doc.add(resumen);

        // ── TABLA DE SOLICITUDES ──────────────────────────────
        if (!solicitudes.isEmpty()) {
            Paragraph secSolicitudes = new Paragraph("Detalle de Solicitudes", fSeccion);
            secSolicitudes.setSpacingAfter(10);
            doc.add(secSolicitudes);

            PdfPTable tabla = new PdfPTable(5);
            tabla.setWidthPercentage(100);
            tabla.setWidths(new float[]{0.5f, 2.5f, 1.5f, 1.5f, 1f});
            tabla.setSpacingAfter(20);

            // Encabezados
            String[] headers = {"#", "Descripción", "Departamento", "Fecha", "Estado"};
            for (String h : headers) {
                PdfPCell cell = new PdfPCell(new Phrase(h, fTabHead));
                cell.setBackgroundColor(azulMedio);
                cell.setPadding(8);
                cell.setBorderColor(azulOscuro);
                cell.setHorizontalAlignment(Element.ALIGN_CENTER);
                tabla.addCell(cell);
            }

            // Filas
            boolean alterno = false;
            for (Solicitud s : solicitudes) {
                BaseColor bg = alterno ? grisClaro : blanco;

                PdfPCell cId = new PdfPCell(
                    new Phrase(String.valueOf(s.getIdSolicitud()), fTabCell));
                cId.setBackgroundColor(bg);
                cId.setPadding(7);
                cId.setHorizontalAlignment(Element.ALIGN_CENTER);
                cId.setBorderColor(grisLinea);
                tabla.addCell(cId);

                String desc = s.getDescripcion();
                if (desc.length() > 60) desc = desc.substring(0, 57) + "...";
                PdfPCell cDesc = new PdfPCell(new Phrase(desc, fTabCell));
                cDesc.setBackgroundColor(bg);
                cDesc.setPadding(7);
                cDesc.setBorderColor(grisLinea);
                tabla.addCell(cDesc);

                PdfPCell cDepto = new PdfPCell(
                    new Phrase(s.getDepartamento().getNombre(), fTabCell));
                cDepto.setBackgroundColor(bg);
                cDepto.setPadding(7);
                cDepto.setBorderColor(grisLinea);
                tabla.addCell(cDepto);

                PdfPCell cFecha = new PdfPCell(
                    new Phrase(s.getFechaHora().format(FMT), fTabCell));
                cFecha.setBackgroundColor(bg);
                cFecha.setPadding(7);
                cFecha.setHorizontalAlignment(Element.ALIGN_CENTER);
                cFecha.setBorderColor(grisLinea);
                tabla.addCell(cFecha);

                PdfPCell cEstado = new PdfPCell(
                    new Phrase(s.getEstado().replace("_", " ").toUpperCase(), fTabCell));
                cEstado.setBackgroundColor(bg);
                cEstado.setPadding(7);
                cEstado.setHorizontalAlignment(Element.ALIGN_CENTER);
                cEstado.setBorderColor(grisLinea);
                tabla.addCell(cEstado);

                alterno = !alterno;
            }
            doc.add(tabla);
        }

        // ── FOOTER ───────────────────────────────────────────
        Paragraph footer = new Paragraph(
            "Documento generado automáticamente por PatrulleroApp · " +
            java.time.LocalDateTime.now().format(FMT), fFooter);
        footer.setAlignment(Element.ALIGN_CENTER);
        doc.add(footer);

        doc.close();
        return baos.toByteArray();
    }

    private void agregarParInfoTurno(PdfPTable tabla, String label,
            String valor, Font fLabel, Font fValor, BaseColor bg) {
        PdfPCell cLabel = new PdfPCell(new Phrase(label, fLabel));
        cLabel.setBackgroundColor(bg);
        cLabel.setPadding(8);
        cLabel.setBorder(Rectangle.NO_BORDER);
        tabla.addCell(cLabel);

        PdfPCell cValor = new PdfPCell(new Phrase(valor, fValor));
        cValor.setBackgroundColor(BaseColor.WHITE);
        cValor.setPadding(8);
        cValor.setBorder(Rectangle.NO_BORDER);
        tabla.addCell(cValor);
    }

    private void agregarCeldaResumen(PdfPTable tabla, String label,
            String valor, BaseColor bg, Font fLabel, Font fValor) {
        PdfPCell cell = new PdfPCell();
        cell.setBackgroundColor(bg);
        cell.setPadding(12);
        cell.setBorder(Rectangle.NO_BORDER);

        Paragraph p = new Paragraph();
        p.add(new Chunk(valor + "\n", fValor));
        p.add(new Chunk(label, fLabel));
        cell.addElement(p);
        tabla.addCell(cell);
    }

    public void guardarReporte(Integer idTurno, String urlArchivo) {
        Turno turno = turnoRepository.findById(idTurno)
            .orElseThrow(() -> new RuntimeException("Turno no encontrado"));

        Usuario supervisor = turno.getSupervisor();

        Reporte reporte = new Reporte();
        reporte.setFechaGeneracion(java.time.LocalDateTime.now());
        reporte.setUrlArchivo(urlArchivo);
        reporte.setTurno(turno);
        reporte.setSupervisor(supervisor);
        reporteRepository.save(reporte);
    }
}
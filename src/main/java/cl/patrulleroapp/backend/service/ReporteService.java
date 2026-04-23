package cl.patrulleroapp.backend.service;

import cl.patrulleroapp.backend.model.*;
import cl.patrulleroapp.backend.repository.*;
import com.itextpdf.text.*;
import com.itextpdf.text.pdf.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.URI;
import java.net.URL;
import java.time.format.DateTimeFormatter;
import java.util.List;


@Service
@RequiredArgsConstructor
public class ReporteService {

    private final TurnoRepository turnoRepository;
    private final SolicitudRepository solicitudRepository;
    private final ReporteRepository reporteRepository;
    private final ImagenRepository imagenRepository;

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

        BaseColor azulOscuro  = new BaseColor(26, 43, 74);
        BaseColor azulMedio   = new BaseColor(46, 74, 122);
        BaseColor grisClaro   = new BaseColor(240, 242, 245);
        BaseColor grisLinea   = new BaseColor(229, 231, 235);
        BaseColor blanco      = BaseColor.WHITE;
        BaseColor amarillo    = new BaseColor(254, 243, 199);
        BaseColor verde       = new BaseColor(220, 252, 231);
        BaseColor grisTexto   = new BaseColor(107, 114, 128);

        Font fTitulo    = new Font(Font.FontFamily.HELVETICA, 22, Font.BOLD, blanco);
        Font fSubtitulo = new Font(Font.FontFamily.HELVETICA, 11, Font.NORMAL, blanco);
        Font fSeccion   = new Font(Font.FontFamily.HELVETICA, 13, Font.BOLD, azulOscuro);
        Font fLabel     = new Font(Font.FontFamily.HELVETICA, 10, Font.BOLD, grisTexto);
        Font fValor     = new Font(Font.FontFamily.HELVETICA, 11, Font.NORMAL, azulOscuro);
        Font fTabHead   = new Font(Font.FontFamily.HELVETICA, 10, Font.BOLD, blanco);
        Font fTabCell   = new Font(Font.FontFamily.HELVETICA, 9,  Font.NORMAL, azulOscuro);
        Font fFooter    = new Font(Font.FontFamily.HELVETICA, 8,  Font.ITALIC, grisTexto);

        // HEADER
        PdfPTable header = new PdfPTable(1);
        header.setWidthPercentage(100);
        header.setSpacingAfter(20);
        PdfPCell headerCell = new PdfPCell();
        headerCell.setBackgroundColor(azulOscuro);
        headerCell.setPadding(20);
        headerCell.setBorder(Rectangle.NO_BORDER);
        Paragraph titulo = new Paragraph("PatrulleroApp", fTitulo);
        titulo.setAlignment(Element.ALIGN_CENTER);
        headerCell.addElement(titulo);
        Paragraph subtitulo = new Paragraph("Reporte de Cierre de Turno", fSubtitulo);
        subtitulo.setAlignment(Element.ALIGN_CENTER);
        headerCell.addElement(subtitulo);
        header.addCell(headerCell);
        doc.add(header);

        // INFO TURNO
        Paragraph secTurno = new Paragraph("Información del Turno", fSeccion);
        secTurno.setSpacingBefore(10);
        secTurno.setSpacingAfter(10);
        doc.add(secTurno);

        PdfPTable infoTurno = new PdfPTable(4);
        infoTurno.setWidthPercentage(100);
        infoTurno.setWidths(new float[]{1, 2, 1, 2});
        infoTurno.setSpacingAfter(20);
        agregarParInfoTurno(infoTurno, "Tipo de turno", turno.getTipo().toUpperCase(), fLabel, fValor, grisClaro);
        agregarParInfoTurno(infoTurno, "Estado", turno.getEstado().toUpperCase(), fLabel, fValor, grisClaro);
        agregarParInfoTurno(infoTurno, "Inicio", turno.getFechaInicio().format(FMT), fLabel, fValor, grisClaro);
        agregarParInfoTurno(infoTurno, "Cierre",
            turno.getFechaCierre() != null ? turno.getFechaCierre().format(FMT) : "—",
            fLabel, fValor, grisClaro);
        agregarParInfoTurno(infoTurno, "Supervisor",
            turno.getSupervisor().getNombre() + " " + turno.getSupervisor().getApellido(),
            fLabel, fValor, grisClaro);
        agregarParInfoTurno(infoTurno, "Total solicitudes",
            String.valueOf(solicitudes.size()), fLabel, fValor, grisClaro);
        doc.add(infoTurno);

        // RESUMEN
        Paragraph secEstadisticas = new Paragraph("Resumen de Solicitudes", fSeccion);
        secEstadisticas.setSpacingAfter(10);
        doc.add(secEstadisticas);

        long pendientes = solicitudes.stream().filter(s -> s.getEstado().equals("pendiente")).count();
        long enProceso  = solicitudes.stream().filter(s -> s.getEstado().equals("en_proceso")).count();
        long cerradas   = solicitudes.stream().filter(s -> s.getEstado().equals("cerrada")).count();

        PdfPTable resumen = new PdfPTable(3);
        resumen.setWidthPercentage(60);
        resumen.setHorizontalAlignment(Element.ALIGN_LEFT);
        resumen.setSpacingAfter(20);
        agregarCeldaResumen(resumen, "Pendientes", String.valueOf(pendientes), amarillo, fLabel, fValor);
        agregarCeldaResumen(resumen, "En Proceso", String.valueOf(enProceso), new BaseColor(219, 234, 254), fLabel, fValor);
        agregarCeldaResumen(resumen, "Cerradas", String.valueOf(cerradas), verde, fLabel, fValor);
        doc.add(resumen);

        // DETALLE DE SOLICITUDES CON IMÁGENES
        if (!solicitudes.isEmpty()) {
            Paragraph secSolicitudes = new Paragraph("Detalle de Solicitudes", fSeccion);
            secSolicitudes.setSpacingAfter(10);
            doc.add(secSolicitudes);

            for (Solicitud s : solicitudes) {
                // Tabla info solicitud
                PdfPTable tablaSol = new PdfPTable(2);
                tablaSol.setWidthPercentage(100);
                tablaSol.setWidths(new float[]{1.5f, 3.5f});
                tablaSol.setSpacingAfter(8);

                BaseColor bgHeader = azulMedio;

                // Header solicitud
                PdfPCell hId = new PdfPCell(new Phrase("Solicitud #" + s.getIdSolicitud(), fTabHead));
                hId.setBackgroundColor(bgHeader);
                hId.setPadding(8);
                hId.setColspan(2);
                hId.setBorderColor(azulOscuro);
                tablaSol.addCell(hId);

                agregarFilaSolicitud(tablaSol, "Descripción", s.getDescripcion(), grisClaro, blanco, fLabel, fTabCell);
                agregarFilaSolicitud(tablaSol, "Departamento", s.getDepartamento().getNombre(), blanco, grisClaro, fLabel, fTabCell);
                agregarFilaSolicitud(tablaSol, "Dirección", s.getDireccion() != null ? s.getDireccion() : "No registrada", grisClaro, blanco, fLabel, fTabCell);
                agregarFilaSolicitud(tablaSol, "Fecha", s.getFechaHora().format(FMT), blanco, grisClaro, fLabel, fTabCell);
                agregarFilaSolicitud(tablaSol, "Estado", s.getEstado().replace("_", " ").toUpperCase(), grisClaro, blanco, fLabel, fTabCell);
                agregarFilaSolicitud(tablaSol, "Patrullero",
                    s.getPatrullero().getNombre() + " " + s.getPatrullero().getApellido(),
                    blanco, grisClaro, fLabel, fTabCell);

                doc.add(tablaSol);

                // IMÁGENES DE LA SOLICITUD
                List<Imagen> imagenes = imagenRepository.findBySolicitud_IdSolicitud(s.getIdSolicitud());
                if (!imagenes.isEmpty()) {
                    Paragraph imgTitulo = new Paragraph("Imágenes de evidencia:", fLabel);
                    imgTitulo.setSpacingBefore(4);
                    imgTitulo.setSpacingAfter(4);
                    doc.add(imgTitulo);

                    PdfPTable tablaImg = new PdfPTable(Math.min(imagenes.size(), 3));
                    tablaImg.setWidthPercentage(100);
                    tablaImg.setSpacingAfter(16);

                    for (Imagen imagen : imagenes) {
                        try {
                            URI imgUri = new URI(imagen.getUrlFirebase());
                            URL imgUrl = imgUri.toURL();
                            InputStream is = imgUrl.openStream();
                            byte[] imgBytes = is.readAllBytes();
                            is.close();

                            Image img = Image.getInstance(imgBytes);
                            img.scaleToFit(160, 120);

                            PdfPCell imgCell = new PdfPCell(img);
                            imgCell.setBorder(Rectangle.BOX);
                            imgCell.setBorderColor(grisLinea);
                            imgCell.setPadding(4);
                            imgCell.setHorizontalAlignment(Element.ALIGN_CENTER);
                            tablaImg.addCell(imgCell);
                        } catch (Exception e) {
                            PdfPCell errCell = new PdfPCell(new Phrase("Imagen no disponible", fTabCell));
                            errCell.setPadding(8);
                            tablaImg.addCell(errCell);
                        }
                    }

                    // Completar celdas vacías si hay menos de 3 imágenes
                    int resto = imagenes.size() % 3;
                    if (resto != 0) {
                        for (int i = 0; i < (3 - resto); i++) {
                            PdfPCell vacia = new PdfPCell(new Phrase(""));
                            vacia.setBorder(Rectangle.NO_BORDER);
                            tablaImg.addCell(vacia);
                        }
                    }

                    doc.add(tablaImg);
                }
            }
        }

        // FOOTER
        Paragraph footer = new Paragraph(
            "Documento generado automáticamente por PatrulleroApp · " +
            java.time.LocalDateTime.now().format(FMT), fFooter);
        footer.setAlignment(Element.ALIGN_CENTER);
        doc.add(footer);

        doc.close();
        return baos.toByteArray();
    }

    private void agregarFilaSolicitud(PdfPTable tabla, String label, String valor,
            BaseColor bg1, BaseColor bg2, Font fLabel, Font fTabCell) {
        PdfPCell cLabel = new PdfPCell(new Phrase(label, fLabel));
        cLabel.setBackgroundColor(bg1);
        cLabel.setPadding(7);
        tabla.addCell(cLabel);

        PdfPCell cValor = new PdfPCell(new Phrase(valor, fTabCell));
        cValor.setBackgroundColor(bg2);
        cValor.setPadding(7);
        tabla.addCell(cValor);
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
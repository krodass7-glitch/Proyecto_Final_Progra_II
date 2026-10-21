package com.taller.service;

import com.lowagie.text.Document;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.FontFactory;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import com.taller.exception.NegocioException;
import com.taller.model.Reporte;
import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import org.springframework.stereotype.Service;

@Service
public class PdfService {
    public byte[] generar(Reporte reporte) {
        try {
            return construir(reporte);
        } catch (Exception e) {
            throw new NegocioException("No se pudo generar el documento PDF");
        }
    }

    private byte[] construir(Reporte reporte) throws Exception {
        ByteArrayOutputStream salida = new ByteArrayOutputStream();
        Document documento = new Document(PageSize.A4.rotate(), 36, 36, 36, 36);
        PdfWriter.getInstance(documento, salida);
        documento.open();

        Font marca = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 11, new Color(90, 90, 90));
        Font titulo = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 17);
        Font normal = FontFactory.getFont(FontFactory.HELVETICA, 9);
        Font encabezado = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 9, Color.WHITE);

        documento.add(new Paragraph("Taller Mecánico El Último Arranque", marca));
        documento.add(new Paragraph(reporte.getTitulo(), titulo));
        Paragraph generado = new Paragraph("Generado: "
                + LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")), normal);
        generado.setSpacingAfter(12);
        documento.add(generado);

        PdfPTable tabla = new PdfPTable(reporte.getColumnas().size());
        tabla.setWidthPercentage(100);
        for (String columna : reporte.getColumnas()) {
            PdfPCell celda = new PdfPCell(new Phrase(columna, encabezado));
            celda.setBackgroundColor(new Color(31, 58, 66));
            celda.setPadding(5);
            tabla.addCell(celda);
        }
        boolean alterna = false;
        for (java.util.List<String> fila : reporte.getFilas()) {
            for (String valor : fila) {
                PdfPCell celda = new PdfPCell(new Phrase(valor, normal));
                celda.setPadding(4);
                celda.setBackgroundColor(alterna ? new Color(240, 243, 244) : Color.WHITE);
                tabla.addCell(celda);
            }
            alterna = !alterna;
        }
        tabla.setHeaderRows(1);
        documento.add(tabla);

        if (reporte.getPie() != null) {
            Paragraph pie = new Paragraph(reporte.getPie(), FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10));
            pie.setSpacingBefore(14);
            pie.setAlignment(Element.ALIGN_LEFT);
            documento.add(pie);
        }
        documento.close();
        return salida.toByteArray();
    }
}

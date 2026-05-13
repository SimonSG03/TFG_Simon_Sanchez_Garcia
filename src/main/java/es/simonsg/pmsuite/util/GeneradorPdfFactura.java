package es.simonsg.pmsuite.util;

import com.lowagie.text.*;
import com.lowagie.text.pdf.*;
import com.lowagie.text.pdf.draw.LineSeparator;
import es.simonsg.pmsuite.model.Hotel;
import es.simonsg.pmsuite.model.Factura;
import es.simonsg.pmsuite.model.LineaFactura;

import java.awt.Color;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Genera un PDF de factura/ticket usando OpenPDF.
 * Los datos del establecimiento se obtienen de {@link Hotel} y son
 * configurables desde el módulo Maestros.
 */
public class GeneradorPdfFactura {

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    // ── Colores de la paleta corporativa ──────────────────────────
    private static final Color COLOR_PRIMARY    = new Color(44, 62, 143);   // azul #2C3E8F
    private static final Color COLOR_ACCENT     = new Color(39, 174, 96);   // verde #27AE60
    private static final Color COLOR_HEADER_BG  = new Color(235, 245, 251); // azul pálido
    private static final Color COLOR_ROW_ALT    = new Color(249, 249, 249);
    private static final Color COLOR_TEXT_DARK  = new Color(26, 37, 53);
    private static final Color COLOR_TEXT_LIGHT = new Color(127, 140, 154);

    // ── Fuentes ───────────────────────────────────────────────────
    private static final Font FONT_HOTEL_NAME   = FontFactory.getFont(FontFactory.HELVETICA_BOLD,  18, COLOR_PRIMARY);
    private static final Font FONT_SECTION      = FontFactory.getFont(FontFactory.HELVETICA_BOLD,  10, COLOR_PRIMARY);
    private static final Font FONT_LABEL        = FontFactory.getFont(FontFactory.HELVETICA,        9, COLOR_TEXT_LIGHT);
    private static final Font FONT_VALUE        = FontFactory.getFont(FontFactory.HELVETICA,        9, COLOR_TEXT_DARK);
    private static final Font FONT_VALUE_BOLD   = FontFactory.getFont(FontFactory.HELVETICA_BOLD,   9, COLOR_TEXT_DARK);
    private static final Font FONT_TABLE_HEADER = FontFactory.getFont(FontFactory.HELVETICA_BOLD,   9, Color.WHITE);
    private static final Font FONT_TABLE_BODY   = FontFactory.getFont(FontFactory.HELVETICA,        9, COLOR_TEXT_DARK);
    private static final Font FONT_TOTAL_LABEL  = FontFactory.getFont(FontFactory.HELVETICA_BOLD,  10, COLOR_TEXT_DARK);
    private static final Font FONT_TOTAL_VALUE  = FontFactory.getFont(FontFactory.HELVETICA_BOLD,  12, COLOR_PRIMARY);
    private static final Font FONT_INVOICE_NUM  = FontFactory.getFont(FontFactory.HELVETICA_BOLD,  14, COLOR_PRIMARY);

    /**
     * Genera el PDF de la factura y lo guarda en {@code destFile}.
     *
     * @param factura  factura con datos del huésped y cabecera
     * @param lineas   líneas de la factura
     * @param hotel    datos del establecimiento (configurados en Maestros)
     * @param destFile fichero de destino
     */
    public static void generate(Factura factura, List<LineaFactura> lineas,
                                Hotel hotel, File destFile) throws DocumentException, IOException {

        Document doc = new Document(PageSize.A4, 40, 40, 50, 50);
        try (FileOutputStream fos = new FileOutputStream(destFile)) {
            PdfWriter writer = PdfWriter.getInstance(doc, fos);
            doc.open();

            // ── 1. Cabecera: logo + datos empresa ────────────────
            doc.add(buildHeader(hotel));
            doc.add(Chunk.NEWLINE);

            // ── 2. Separador + número de factura ────────────────
            doc.add(buildInvoiceTitle(factura));
            doc.add(Chunk.NEWLINE);

            // ── 3. Datos del cliente ─────────────────────────────
            doc.add(buildClientSection(factura));
            doc.add(Chunk.NEWLINE);

            // ── 4. Tabla de líneas ───────────────────────────────
            doc.add(buildLinesTable(lineas));
            doc.add(Chunk.NEWLINE);

            // ── 5. Totales ───────────────────────────────────────
            doc.add(buildTotals(factura, lineas));

            // ── 6. Pie: forma de pago / notas ────────────────────
            doc.add(Chunk.NEWLINE);
            doc.add(buildFooter(factura, hotel));

            doc.close();
        }
    }

    // ── Cabecera (logo + empresa) ─────────────────────────────────

    private static PdfPTable buildHeader(Hotel hotel) throws DocumentException {
        PdfPTable t = new PdfPTable(2);
        t.setWidthPercentage(100);
        t.setWidths(new float[]{1.4f, 1f});

        // Columna izquierda: logo o nombre grande
        PdfPCell logoCell = new PdfPCell();
        logoCell.setBorder(Rectangle.NO_BORDER);
        logoCell.setPadding(4);

        if (hotel.getRutaLogo() != null && !hotel.getRutaLogo().isBlank()) {
            try {
                Image logo = Image.getInstance(hotel.getRutaLogo());
                logo.scaleToFit(140, 60);
                logoCell.addElement(logo);
            } catch (Exception e) {
                logoCell.addElement(new Paragraph(hotel.getNombre() != null ? hotel.getNombre() : "Hotel",
                        FONT_HOTEL_NAME));
            }
        } else {
            logoCell.addElement(new Paragraph(hotel.getNombre() != null ? hotel.getNombre() : "Hotel",
                    FONT_HOTEL_NAME));
        }
        t.addCell(logoCell);

        // Columna derecha: datos de la empresa
        PdfPCell infoCell = new PdfPCell();
        infoCell.setBorder(Rectangle.NO_BORDER);
        infoCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
        infoCell.setPaddingTop(4);

        Paragraph info = new Paragraph();
        info.setAlignment(Element.ALIGN_RIGHT);
        appendLine(info, hotel.getNombre(),            FONT_VALUE_BOLD);
        appendLine(info, hotel.getDireccionCompleta(), FONT_VALUE);
        appendLine(info, hotel.getTelefono(),          FONT_VALUE);
        appendLine(info, hotel.getEmail(),             FONT_VALUE);
        appendLine(info, hotel.getSitioWeb(),          FONT_VALUE);
        if (hotel.getNif() != null && !hotel.getNif().isBlank())
            appendLine(info, "NIF/CIF: " + hotel.getNif(), FONT_VALUE);

        infoCell.addElement(info);
        t.addCell(infoCell);

        // Línea divisoria debajo
        PdfPCell divider = new PdfPCell();
        divider.setColspan(2);
        divider.setBackgroundColor(COLOR_PRIMARY);
        divider.setFixedHeight(2f);
        divider.setBorder(Rectangle.NO_BORDER);
        t.addCell(divider);

        return t;
    }

    // ── Bloque número de factura ──────────────────────────────────

    private static PdfPTable buildInvoiceTitle(Factura factura) throws DocumentException {
        PdfPTable t = new PdfPTable(2);
        t.setWidthPercentage(100);
        t.setWidths(new float[]{1f, 1f});

        PdfPCell left = noBorderCell(Element.ALIGN_LEFT);
        String num = factura.getNumeroFactura() != null && !factura.getNumeroFactura().isBlank()
                ? factura.getNumeroFactura() : "TICKET #" + factura.getId();
        left.addElement(new Paragraph("FACTURA  " + num, FONT_INVOICE_NUM));
        t.addCell(left);

        PdfPCell right = noBorderCell(Element.ALIGN_RIGHT);
        Paragraph dates = new Paragraph();
        dates.setAlignment(Element.ALIGN_RIGHT);
        appendLabelValue(dates, "Fecha de emisión: ",
                factura.getFechaEmision() != null ? factura.getFechaEmision().format(DATE_FMT) : "");
        if (factura.getFechaVencimiento() != null)
            appendLabelValue(dates, "Fecha de vencimiento: ", factura.getFechaVencimiento().format(DATE_FMT));
        appendLabelValue(dates, "Estado: ",
                factura.getEstado() != null ? factura.getEstado().toSpanish() : "");
        right.addElement(dates);
        t.addCell(right);

        return t;
    }

    // ── Sección cliente ───────────────────────────────────────────

    private static PdfPTable buildClientSection(Factura factura) throws DocumentException {
        boolean hasBilling = factura.tieneDatosFacturacion();

        // Si hay datos de facturación alternativos mostramos dos bloques; si no, uno.
        int cols = hasBilling ? 2 : 1;
        float[] widths = hasBilling ? new float[]{1f, 1f} : new float[]{1f};

        PdfPTable t = new PdfPTable(cols);
        t.setWidthPercentage(hasBilling ? 100 : 55);
        t.setHorizontalAlignment(Element.ALIGN_LEFT);
        if (hasBilling) t.setWidths(widths);

        // ── Bloque huésped ────────────────────────────────────────
        PdfPCell guestHeader = new PdfPCell(new Phrase("HUÉSPED", FONT_SECTION));
        guestHeader.setBackgroundColor(COLOR_HEADER_BG);
        guestHeader.setBorderColor(COLOR_PRIMARY);
        guestHeader.setBorderWidth(0.5f);
        guestHeader.setPadding(5);
        t.addCell(guestHeader);

        if (hasBilling) {
            // Cabecera del bloque de empresa
            PdfPCell billHeader = new PdfPCell(new Phrase("DATOS DE FACTURACIÓN", FONT_SECTION));
            billHeader.setBackgroundColor(COLOR_HEADER_BG);
            billHeader.setBorderColor(COLOR_PRIMARY);
            billHeader.setBorderWidth(0.5f);
            billHeader.setPadding(5);
            t.addCell(billHeader);
        }

        // Cuerpo huésped
        Paragraph guestBody = new Paragraph();
        if (factura.getHuesped() != null) {
            var g = factura.getHuesped();
            appendLabelValue(guestBody, "Nombre: ", g.getNombre() + " " + g.getApellidos());
            if (g.getNif() != null && !g.getNif().isBlank())
                appendLabelValue(guestBody, "NIF: ", g.getNif());
            if (g.getEmail() != null && !g.getEmail().isBlank())
                appendLabelValue(guestBody, "Email: ", g.getEmail());
            if (g.getTelefono() != null && !g.getTelefono().isBlank())
                appendLabelValue(guestBody, "Teléfono: ", g.getTelefono());
        } else {
            guestBody.add(new Chunk("(cliente no especificado)", FONT_VALUE));
        }
        PdfPCell guestCell = new PdfPCell();
        guestCell.setBorderColor(new Color(200, 200, 200));
        guestCell.setBorderWidth(0.5f);
        guestCell.setPadding(6);
        guestCell.addElement(guestBody);
        t.addCell(guestCell);

        if (hasBilling) {
            // Cuerpo datos de facturación alternativa
            Paragraph billBody = new Paragraph();
            appendLabelValue(billBody, "Empresa/Nombre: ", factura.getNombreFacturacion());
            if (factura.getNifFacturacion() != null && !factura.getNifFacturacion().isBlank())
                appendLabelValue(billBody, "NIF/CIF: ", factura.getNifFacturacion());
            // Dirección completa
            String addr = buildBillingAddress(factura);
            if (!addr.isBlank()) appendLabelValue(billBody, "Dirección: ", addr);

            PdfPCell billCell = new PdfPCell();
            billCell.setBorderColor(new Color(200, 200, 200));
            billCell.setBorderWidth(0.5f);
            billCell.setPadding(6);
            billCell.addElement(billBody);
            t.addCell(billCell);
        }

        return t;
    }

    private static String buildBillingAddress(Factura factura) {
        StringBuilder sb = new StringBuilder();
        if (factura.getDomicilioFacturacion() != null && !factura.getDomicilioFacturacion().isBlank())
            sb.append(factura.getDomicilioFacturacion());
        if (factura.getCpFacturacion() != null && !factura.getCpFacturacion().isBlank()) {
            if (!sb.isEmpty()) sb.append(", ");
            sb.append(factura.getCpFacturacion());
        }
        if (factura.getCiudadFacturacion() != null && !factura.getCiudadFacturacion().isBlank()) {
            if (!sb.isEmpty()) sb.append(" ");
            sb.append(factura.getCiudadFacturacion());
        }
        if (factura.getPaisFacturacion() != null && !factura.getPaisFacturacion().isBlank()) {
            if (!sb.isEmpty()) sb.append(" – ");
            sb.append(factura.getPaisFacturacion());
        }
        return sb.toString();
    }

    // ── Tabla de líneas ───────────────────────────────────────────

    private static PdfPTable buildLinesTable(List<LineaFactura> lineas) throws DocumentException {
        PdfPTable t = new PdfPTable(4);
        t.setWidthPercentage(100);
        t.setWidths(new float[]{4f, 1f, 1.5f, 1.5f});
        t.setSpacingBefore(4f);

        // Cabecera
        for (String h : new String[]{"DESCRIPCIÓN", "CANT.", "PRECIO/UD.", "TOTAL"}) {
            PdfPCell c = new PdfPCell(new Phrase(h, FONT_TABLE_HEADER));
            c.setBackgroundColor(COLOR_PRIMARY);
            c.setPadding(6);
            c.setBorder(Rectangle.NO_BORDER);
            c.setHorizontalAlignment(h.equals("DESCRIPCIÓN") ? Element.ALIGN_LEFT : Element.ALIGN_RIGHT);
            t.addCell(c);
        }

        // Filas
        boolean alt = false;
        for (LineaFactura linea : lineas) {
            Color bg = alt ? COLOR_ROW_ALT : Color.WHITE;
            alt = !alt;

            addBodyCell(t, linea.getDescripcion(), bg, Element.ALIGN_LEFT);
            addBodyCell(t, formatQty(linea.getCantidad()), bg, Element.ALIGN_RIGHT);
            addBodyCell(t, formatAmt(linea.getPrecioUnitario()), bg, Element.ALIGN_RIGHT);
            addBodyCell(t, formatAmt(linea.getPrecioTotal()), bg, Element.ALIGN_RIGHT);
        }

        // Fila vacía de cierre
        PdfPCell empty = new PdfPCell();
        empty.setColspan(4);
        empty.setFixedHeight(2f);
        empty.setBackgroundColor(COLOR_PRIMARY);
        empty.setBorder(Rectangle.NO_BORDER);
        t.addCell(empty);

        return t;
    }

    // ── Totales ───────────────────────────────────────────────────

    private static PdfPTable buildTotals(Factura factura, List<LineaFactura> lineas) throws DocumentException {
        PdfPTable outer = new PdfPTable(2);
        outer.setWidthPercentage(100);
        outer.setWidths(new float[]{1f, 1f});

        // Celda izquierda vacía
        PdfPCell left = noBorderCell(Element.ALIGN_LEFT);
        left.addElement(new Phrase(" "));
        outer.addCell(left);

        // Celda derecha: resumen de importes
        PdfPTable totals = new PdfPTable(2);
        totals.setWidthPercentage(100);

        BigDecimal subtotal = sum(lineas);
        BigDecimal taxRate  = factura.getTasaImpuesto() != null ? factura.getTasaImpuesto()
                : new BigDecimal("10.00");
        BigDecimal iva      = subtotal.multiply(taxRate).divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
        BigDecimal total    = subtotal.add(iva);

        addTotalRow(totals, "Subtotal:", formatAmt(subtotal), false);
        addTotalRow(totals, "IVA (" + taxRate.stripTrailingZeros().toPlainString() + "%):",
                formatAmt(iva), false);
        addTotalRow(totals, "TOTAL:",    formatAmt(total),    true);

        PdfPCell right = noBorderCell(Element.ALIGN_RIGHT);
        right.addElement(totals);
        outer.addCell(right);

        return outer;
    }

    // ── Pie: forma de pago + IBAN + notas ─────────────────────────

    private static Paragraph buildFooter(Factura factura, Hotel hotel) {
        Paragraph p = new Paragraph();
        p.add(new LineSeparator(0.5f, 100, COLOR_PRIMARY, Element.ALIGN_CENTER, -2));
        p.add(Chunk.NEWLINE);

        if (factura.getMetodoPago() != null)
            appendLabelValue(p, "Forma de pago: ", factura.getMetodoPago().toSpanish());

        if (hotel.getIban() != null && !hotel.getIban().isBlank())
            appendLabelValue(p, "IBAN: ", hotel.getIban());

        if (factura.getNotas() != null && !factura.getNotas().isBlank()) {
            p.add(Chunk.NEWLINE);
            p.add(new Phrase("Notas: ", FONT_LABEL));
            p.add(new Phrase(factura.getNotas(), FONT_VALUE));
        }
        return p;
    }

    // ── Helpers ───────────────────────────────────────────────────

    private static void appendLine(Paragraph p, String text, Font font) {
        if (text != null && !text.isBlank()) {
            p.add(new Phrase(text + "\n", font));
        }
    }

    private static void appendLabelValue(Paragraph p, String label, String value) {
        if (value != null && !value.isBlank()) {
            p.add(new Phrase(label, FONT_LABEL));
            p.add(new Phrase(value + "\n", FONT_VALUE));
        }
    }

    private static PdfPCell noBorderCell(int align) {
        PdfPCell c = new PdfPCell();
        c.setBorder(Rectangle.NO_BORDER);
        c.setHorizontalAlignment(align);
        c.setPaddingTop(2);
        return c;
    }

    private static void addBodyCell(PdfPTable t, String text, Color bg, int align) {
        PdfPCell c = new PdfPCell(new Phrase(text != null ? text : "", FONT_TABLE_BODY));
        c.setBackgroundColor(bg);
        c.setBorderColor(new Color(220, 220, 220));
        c.setBorderWidth(0.3f);
        c.setPadding(5);
        c.setHorizontalAlignment(align);
        t.addCell(c);
    }

    private static void addTotalRow(PdfPTable t, String label, String value, boolean highlight) {
        Font labelFont = highlight ? FONT_TOTAL_LABEL : FONT_VALUE_BOLD;
        Font valueFont = highlight ? FONT_TOTAL_VALUE : FONT_VALUE_BOLD;

        PdfPCell lc = new PdfPCell(new Phrase(label, labelFont));
        lc.setBorder(Rectangle.NO_BORDER);
        lc.setHorizontalAlignment(Element.ALIGN_RIGHT);
        lc.setPadding(3);
        if (highlight) lc.setPaddingTop(6);
        t.addCell(lc);

        PdfPCell vc = new PdfPCell(new Phrase(value, valueFont));
        vc.setBorder(highlight ? Rectangle.TOP : Rectangle.NO_BORDER);
        if (highlight) { vc.setBorderColor(COLOR_PRIMARY); vc.setBorderWidth(1f); }
        vc.setHorizontalAlignment(Element.ALIGN_RIGHT);
        vc.setPadding(3);
        if (highlight) vc.setPaddingTop(6);
        t.addCell(vc);
    }

    private static BigDecimal sum(List<LineaFactura> lineas) {
        return lineas.stream()
                .map(LineaFactura::getPrecioTotal)
                .filter(v -> v != null)
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .setScale(2, RoundingMode.HALF_UP);
    }

    private static String formatAmt(BigDecimal v) {
        if (v == null) return "0,00 €";
        return v.setScale(2, RoundingMode.HALF_UP).toPlainString().replace(".", ",") + " €";
    }

    private static String formatQty(BigDecimal v) {
        if (v == null) return "1";
        return v.stripTrailingZeros().toPlainString();
    }
}

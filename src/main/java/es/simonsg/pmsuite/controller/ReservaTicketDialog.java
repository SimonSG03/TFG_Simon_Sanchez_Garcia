package es.simonsg.pmsuite.controller;

import es.simonsg.pmsuite.dao.FacturaDAO;
import es.simonsg.pmsuite.model.Factura;
import es.simonsg.pmsuite.model.LineaFactura;
import es.simonsg.pmsuite.model.Reserva;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Shows a full-detail ticket dialog for a given reservation,
 * including all invoice lines categorized by type and payment summary.
 */
public final class ReservaTicketDialog {

    private static final DateTimeFormatter DATE_FMT =
            DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private ReservaTicketDialog() {}

    public static void show(Reserva r) {
        Dialog<ButtonType> dlg = new Dialog<>();
        dlg.setTitle("Ticket — " + r.getNumeroReserva());
        dlg.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);

        VBox root = new VBox(0);
        root.setStyle("-fx-font-family: 'Segoe UI'; -fx-background-color: white;");
        root.setMinWidth(520);
        root.setMaxWidth(520);

        buildHeader(root, r);
        buildInfoSection(root, r);
        buildFacturacion(root, r);

        dlg.getDialogPane().setContent(root);
        dlg.getDialogPane().setStyle("-fx-padding: 0;");
        dlg.getDialogPane().setPrefWidth(540);
        dlg.showAndWait();
    }

    // ── Header ────────────────────────────────────────────────────────────────

    private static void buildHeader(VBox root, Reserva r) {
        String color = statusColor(r);

        // Colored top bar
        Rectangle bar = new Rectangle(520, 5, Color.web(color));

        // Reserva number
        Label numLbl = new Label(r.getNumeroReserva());
        numLbl.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #111827;");

        // Status badge
        String statusText = statusLabel(r);
        Label badge = new Label(statusText);
        badge.setStyle(
                "-fx-background-color: " + color + "22;" +
                        "-fx-text-fill: "         + color + ";" +
                        "-fx-font-size: 11px; -fx-padding: 3 10; -fx-background-radius: 12;");

        // Huesped name
        String guest = r.getHuesped() != null
                ? r.getHuesped().getNombre() + " " + r.getHuesped().getApellidos()
                : "Huésped desconocido";
        Label guestLbl = new Label(guest);
        guestLbl.setStyle("-fx-font-size: 14px; -fx-text-fill: #374151;");

        Region sp = new Region();
        HBox.setHgrow(sp, Priority.ALWAYS);

        HBox headerRow = new HBox(8, numLbl, sp, badge);
        headerRow.setAlignment(Pos.CENTER_LEFT);

        VBox headerBox = new VBox(4, headerRow, guestLbl);
        headerBox.setPadding(new Insets(14, 18, 14, 18));
        headerBox.setStyle("-fx-background-color: white;");

        root.getChildren().addAll(bar, headerBox, separator());
    }

    // ── Stay info ─────────────────────────────────────────────────────────────

    private static void buildInfoSection(VBox root, Reserva r) {
        Label secTitle = sectionTitle("ESTANCIA");

        String room = r.getHabitacion() != null ? "Hab. " + r.getHabitacion().getNumero() : "Sin asignar";
        long nights = r.getFechaEntrada().until(r.getFechaSalida()).getDays();
        String adultsStr = r.getAdultos() + " adulto" + (r.getAdultos() == 1 ? "" : "s");
        if (r.getNinos() > 0) adultsStr += " · " + r.getNinos() + " niño" + (r.getNinos() == 1 ? "" : "s");

        VBox box = new VBox(6,
                secTitle,
                infoRow("Habitación",  room),
                infoRow("Entrada",     r.getFechaEntrada().format(DATE_FMT)),
                infoRow("Salida",      r.getFechaSalida().format(DATE_FMT)),
                infoRow("Estancia",    nights + " noche" + (nights == 1 ? "" : "s")),
                infoRow("Ocupación",   adultsStr)
        );
        box.setPadding(new Insets(10, 18, 14, 18));

        root.getChildren().addAll(box, separator());
    }

    // ── Factura lines + totals ────────────────────────────────────────────────

    private static void buildFacturacion(VBox root, Reserva r) {
        try {
            Factura inv = FacturaDAO.getPorReserva(r.getId());
            List<LineaFactura> lines = FacturaDAO.getLineasPorReserva(r.getId());

            if (inv == null || lines.isEmpty()) {
                Label noInv = new Label("Sin factura registrada.");
                noInv.setStyle("-fx-text-fill: #6B7280; -fx-font-size: 12px; -fx-padding: 10 18;");
                root.getChildren().add(noInv);
                return;
            }

            // ── Lines section
            Label linesTitle = sectionTitle("CONCEPTOS");
            VBox linesBox = new VBox(0, linesTitle);
            linesBox.setPadding(new Insets(10, 18, 0, 18));

            for (LineaFactura il : lines) {
                linesBox.getChildren().add(lineRow(il));
            }

            root.getChildren().addAll(linesBox, separator());

            // ── Totals
            VBox totalsBox = new VBox(5);
            totalsBox.setPadding(new Insets(10, 18, 10, 18));

            if (inv.getSubtotal() != null)
                totalsBox.getChildren().add(totalRow("Subtotal", inv.getSubtotal(), false, "#374151"));
            if (inv.getTasaImpuesto() != null && inv.getImporteImpuesto() != null) {
                String taxLabel = "IVA (" + inv.getTasaImpuesto().stripTrailingZeros().toPlainString() + "%)";
                totalsBox.getChildren().add(totalRow(taxLabel, inv.getImporteImpuesto(), false, "#374151"));
            }
            if (inv.getImporteTotal() != null)
                totalsBox.getChildren().add(totalRow("TOTAL", inv.getImporteTotal(), true, "#111827"));

            root.getChildren().addAll(totalsBox, separator());

            // ── Payments
            VBox payBox = new VBox(5);
            payBox.setPadding(new Insets(10, 18, 14, 18));
            payBox.getChildren().add(sectionTitle("PAGOS"));

            if (inv.getImportePagado() != null)
                payBox.getChildren().add(totalRow("Pagado", inv.getImportePagado(), false, "#16A34A"));

            BigDecimal pending = inv.getImportePendiente();
            if (pending != null && pending.compareTo(BigDecimal.ZERO) > 0) {
                payBox.getChildren().add(totalRow("Pendiente", pending, true, "#DC2626"));
            } else {
                Label paidFull = new Label("✓ Totalmente pagado");
                paidFull.setStyle("-fx-text-fill: #16A34A; -fx-font-size: 12px; -fx-font-weight: bold;");
                payBox.getChildren().add(paidFull);
            }

            root.getChildren().add(payBox);

        } catch (SQLException ex) {
            ex.printStackTrace();
            Label err = new Label("Error al cargar la factura.");
            err.setStyle("-fx-text-fill: #DC2626; -fx-padding: 10 18;");
            root.getChildren().add(err);
        }
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private static Label sectionTitle(String text) {
        Label lbl = new Label(text);
        lbl.setStyle("-fx-font-size: 10px; -fx-font-weight: bold; " +
                "-fx-text-fill: #9CA3AF; -fx-padding: 0 0 4 0; -fx-letter-spacing: 1;");
        return lbl;
    }

    private static HBox infoRow(String label, String value) {
        Label lbl = new Label(label);
        lbl.setPrefWidth(100);
        lbl.setStyle("-fx-font-size: 12px; -fx-text-fill: #6B7280;");

        Label val = new Label(value);
        val.setStyle("-fx-font-size: 12px; -fx-text-fill: #111827; -fx-font-weight: bold;");

        return new HBox(8, lbl, val);
    }

    private static HBox lineRow(LineaFactura il) {
        String cat   = categoria(il.getDescripcion());
        String color = categoriaColor(cat);

        Label catBadge = new Label(cat);
        catBadge.setPrefWidth(88);
        catBadge.setStyle(
                "-fx-background-color: " + color + "20;" +
                        "-fx-text-fill: "         + color + ";" +
                        "-fx-font-size: 9px; -fx-padding: 2 6; -fx-background-radius: 8; " +
                        "-fx-font-weight: bold;");

        Label desc = new Label(il.getDescripcion());
        desc.setStyle("-fx-font-size: 11px; -fx-text-fill: #374151;");
        desc.setWrapText(false);
        desc.setMaxWidth(220);
        HBox.setHgrow(desc, Priority.ALWAYS);

        Label qtyLbl = new Label(il.getCantidad().stripTrailingZeros().toPlainString()
                + " × " + eur(il.getPrecioUnitario()));
        qtyLbl.setStyle("-fx-font-size: 10px; -fx-text-fill: #6B7280;");
        qtyLbl.setMinWidth(90);

        Label totLbl = new Label(eur(il.getPrecioTotal()));
        totLbl.setStyle("-fx-font-size: 11px; -fx-text-fill: #111827; -fx-font-weight: bold;");
        totLbl.setMinWidth(70);
        totLbl.setAlignment(Pos.CENTER_RIGHT);

        HBox row = new HBox(8, catBadge, desc, qtyLbl, totLbl);
        row.setAlignment(Pos.CENTER_LEFT);
        row.setPadding(new Insets(5, 0, 5, 0));
        return row;
    }

    private static HBox totalRow(String label, BigDecimal amount, boolean bold, String hexColor) {
        Label lbl = new Label(label);
        lbl.setStyle("-fx-font-size: 12px; -fx-text-fill: " + hexColor + ";"
                + (bold ? " -fx-font-weight: bold;" : ""));

        Region sp = new Region();
        HBox.setHgrow(sp, Priority.ALWAYS);

        Label val = new Label(eur(amount));
        val.setStyle("-fx-font-size: 12px; -fx-text-fill: " + hexColor + ";"
                + (bold ? " -fx-font-weight: bold;" : ""));

        HBox row = new HBox(lbl, sp, val);
        row.setAlignment(Pos.CENTER_LEFT);
        return row;
    }

    private static Separator separator() {
        Separator s = new Separator();
        s.setStyle("-fx-opacity: 0.4;");
        return s;
    }

    private static String categoria(String desc) {
        if (desc == null) return "Otro";
        String d = desc.toLowerCase();
        if (d.contains("minibar"))                                  return "Minibar";
        if (d.contains("restaurante") || d.contains("cena")
                || d.contains("comida") || d.contains("desayuno")
                || d.contains("room service") || d.contains("gourmet")) return "Restaurante";
        if (d.contains("parking"))                                  return "Parking";
        if (d.contains("spa") || d.contains("masaje")
                || d.contains("bienestar"))                         return "Spa";
        if (d.contains("alojamiento") || d.contains("estancia")
                || d.contains("noche"))                             return "Alojamiento";
        return "Otro";
    }

    private static String categoriaColor(String cat) {
        return switch (cat) {
            case "Alojamiento" -> "#2A9D8F";
            case "Minibar"     -> "#7C3AED";
            case "Restaurante" -> "#EA580C";
            case "Parking"     -> "#2563EB";
            case "Spa"         -> "#0D9488";
            default            -> "#6B7280";
        };
    }

    private static String statusColor(Reserva r) {
        if (r.getEstado() == null) return "#6B7280";
        return switch (r.getEstado()) {
            case CONFIRMADA   -> "#2A9D8F";
            case REGISTRADA  -> "#16A34A";
            case CHECKOUT_REALIZADO -> "#9CA3AF";
            case CANCELADA   -> "#DC2626";
            default          -> "#F59E0B";
        };
    }

    private static String statusLabel(Reserva r) {
        if (r.getEstado() == null) return "Desconocido";
        return switch (r.getEstado()) {
            case CONFIRMADA   -> "Confirmada";
            case REGISTRADA  -> "Check-in realizado";
            case CHECKOUT_REALIZADO -> "Check-out realizado";
            case CANCELADA   -> "Cancelada";
            default          -> "Pendiente";
        };
    }

    private static String eur(BigDecimal v) {
        if (v == null) return "—";
        return String.format("%,.2f €", v);
    }
}

package es.simonsg.pmsuite.dao;

import es.simonsg.pmsuite.db.GestorBD;
import es.simonsg.pmsuite.model.Factura;
import es.simonsg.pmsuite.model.Huesped;
import es.simonsg.pmsuite.model.LineaFactura;
import es.simonsg.pmsuite.model.Reserva;
import es.simonsg.pmsuite.model.Habitacion;

import java.math.BigDecimal;
import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class FacturaDAO {

    // ── Migración automática: añade columnas de facturación alternativa si no existen ──
    static {
        String[] migrations = {
                "ALTER TABLE invoices ADD COLUMN IF NOT EXISTS billing_name        VARCHAR(200)",
                "ALTER TABLE invoices ADD COLUMN IF NOT EXISTS billing_nif         VARCHAR(30)",
                "ALTER TABLE invoices ADD COLUMN IF NOT EXISTS billing_address     VARCHAR(300)",
                "ALTER TABLE invoices ADD COLUMN IF NOT EXISTS billing_city        VARCHAR(100)",
                "ALTER TABLE invoices ADD COLUMN IF NOT EXISTS billing_postal_code VARCHAR(10)",
                "ALTER TABLE invoices ADD COLUMN IF NOT EXISTS billing_country     VARCHAR(100)"
        };
        try (java.sql.Connection conn = GestorBD.getInstance().getConnection();
             java.sql.Statement st = conn.createStatement()) {
            for (String sql : migrations) st.executeUpdate(sql);
        } catch (Exception e) {
            System.err.println("[FacturaDAO] Migration warning: " + e.getMessage());
        }
    }

    private static final String BASE_SQL = """
            SELECT i.*, g.first_name, g.last_name, r.reservation_number
            FROM invoices i
            JOIN guests g ON g.id = i.guest_id
            LEFT JOIN reservations r ON r.id = i.reservation_id
            """;

    public static List<Factura> obtenerTodas() throws SQLException {
        String sql = BASE_SQL + "ORDER BY i.created_at DESC LIMIT 200";
        try (Connection conn = GestorBD.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            return mapearLista(ps.executeQuery());
        }
    }

    public static List<Factura> getPendientesYParciales() throws SQLException {
        String sql = BASE_SQL +
                "WHERE i.status IN ('PENDING', 'PARTIALLY_PAID') ORDER BY i.created_at DESC";
        try (Connection conn = GestorBD.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            return mapearLista(ps.executeQuery());
        }
    }

    /**
     * Facturas PENDING o PARTIALLY_PAID de reservas actualmente en CHECKED_IN.
     * Incluye número de habitación y fechas de estancia para la vista de recepción.
     */
    public static List<Factura> getAbiertasDeRegistrados() throws SQLException {
        String sql = """
                SELECT i.*, g.first_name, g.last_name, r.reservation_number,
                       r.check_in_date, r.check_out_date, rm.number as rm_number
                FROM invoices i
                JOIN guests g ON g.id = i.guest_id
                JOIN reservations r ON r.id = i.reservation_id
                LEFT JOIN rooms rm ON rm.id = r.room_id
                WHERE r.status = 'CHECKED_IN'
                  AND i.status IN ('PENDING', 'PARTIALLY_PAID')
                ORDER BY rm.number ASC NULLS LAST, i.created_at ASC
                """;
        try (Connection conn = GestorBD.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ResultSet rs = ps.executeQuery();
            List<Factura> list = new ArrayList<>();
            while (rs.next()) {
                Factura inv = mapear(rs);
                Reserva res = inv.getReserva();
                if (res != null) {
                    Date ci = rs.getDate("check_in_date");
                    Date co = rs.getDate("check_out_date");
                    if (ci != null) res.setFechaEntrada(ci.toLocalDate());
                    if (co != null) res.setFechaSalida(co.toLocalDate());
                    String rmNum = rs.getString("rm_number");
                    if (rmNum != null) {
                        Habitacion room = new Habitacion();
                        room.setNumero(rmNum);
                        res.setHabitacion(room);
                    }
                }
                list.add(inv);
            }
            return list;
        }
    }

    public static Factura getPorReserva(int reservaId) throws SQLException {
        String sql = BASE_SQL +
                "WHERE i.reservation_id = ? ORDER BY i.created_at ASC LIMIT 1";
        try (Connection conn = GestorBD.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, reservaId);
            ResultSet rs = ps.executeQuery();
            return rs.next() ? mapear(rs) : null;
        }
    }

    public static Factura getAbiertaPorHabitacion(int habitacionId) throws SQLException {
        try {
            Reserva res = ReservaDAO.getRegistradaPorHabitacion(habitacionId);
            if (res == null) return null;
            return getPorReserva(res.getId());
        } catch (Exception e) { return null; }
    }

    /**
     * Factura pendiente/parcial más reciente del huésped que NO tiene reserva asignada.
     * Útil como fallback cuando el ticket se creó sin vincular una reserva.
     */
    public static Factura getUltimaSinVincularPorHuesped(int huespedId) throws SQLException {
        String sql = BASE_SQL +
                "WHERE i.guest_id = ? AND i.reservation_id IS NULL " +
                "  AND i.status IN ('PENDING', 'PARTIALLY_PAID') " +
                "ORDER BY i.created_at DESC LIMIT 1";
        try (Connection conn = GestorBD.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, huespedId);
            ResultSet rs = ps.executeQuery();
            return rs.next() ? mapear(rs) : null;
        }
    }

    /** Vincula una factura a una reserva (cuando se creó sin reservation_id). */
    public static void vincularReserva(int facturaId, int reservaId) throws SQLException {
        String sql = "UPDATE invoices SET reservation_id = ? WHERE id = ?";
        try (Connection conn = GestorBD.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, reservaId);
            ps.setInt(2, facturaId);
            ps.executeUpdate();
        }
    }

    /** Devuelve TODAS las facturas vinculadas a una reserva (más antigua primero). */
    public static List<Factura> getTodasPorReserva(int reservaId) throws SQLException {
        String sql = BASE_SQL +
                "WHERE i.reservation_id = ? ORDER BY i.created_at ASC";
        try (Connection conn = GestorBD.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, reservaId);
            return mapearLista(ps.executeQuery());
        }
    }

    /**
     * Devuelve TODAS las líneas de TODAS las facturas vinculadas a una reserva.
     */
    public static List<LineaFactura> getLineasPorReserva(int reservaId) throws SQLException {
        String sql = """
                SELECT il.*
                FROM invoice_lines il
                JOIN invoices i ON i.id = il.invoice_id
                WHERE i.reservation_id = ?
                  AND i.status != 'CANCELLED'
                ORDER BY i.created_at ASC, il.id ASC
                """;
        try (Connection conn = GestorBD.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, reservaId);
            ResultSet rs = ps.executeQuery();
            List<LineaFactura> list = new ArrayList<>();
            while (rs.next()) {
                LineaFactura il = new LineaFactura();
                il.setId(rs.getInt("id"));
                il.setIdFactura(rs.getInt("invoice_id"));
                il.setDescripcion(rs.getString("description"));
                il.setCantidad(rs.getBigDecimal("quantity"));
                il.setPrecioUnitario(rs.getBigDecimal("unit_price"));
                il.setPrecioTotal(rs.getBigDecimal("total_price"));
                Timestamp cat = rs.getTimestamp("created_at");
                if (cat != null) il.setFechaCreacion(cat.toLocalDateTime());
                list.add(il);
            }
            return list;
        }
    }

    public static int crear(Factura inv) throws SQLException {
        String sql = """
                INSERT INTO invoices
                  (reservation_id, guest_id, issue_date, due_date, subtotal, tax_rate,
                   tax_amount, total_amount, paid_amount, status, notes, invoice_number)
                VALUES (?,?,?,?,?,?,?,?,?,'PENDING'::invoice_status,?,'')
                RETURNING id
                """;
        try (Connection conn = GestorBD.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            if (inv.getReserva() != null) ps.setInt(1, inv.getReserva().getId());
            else ps.setNull(1, Types.INTEGER);
            if (inv.getHuesped() != null) ps.setInt(2, inv.getHuesped().getId());
            else ps.setNull(2, Types.INTEGER);
            LocalDate issue = inv.getFechaEmision() != null ? inv.getFechaEmision() : LocalDate.now();
            ps.setDate(3, Date.valueOf(issue));
            ps.setDate(4, inv.getFechaVencimiento() != null ? Date.valueOf(inv.getFechaVencimiento()) : null);
            ps.setBigDecimal(5, inv.getSubtotal() != null ? inv.getSubtotal() : BigDecimal.ZERO);
            ps.setBigDecimal(6, new BigDecimal("10.00"));
            ps.setBigDecimal(7, BigDecimal.ZERO);
            ps.setBigDecimal(8, BigDecimal.ZERO);
            ps.setBigDecimal(9, BigDecimal.ZERO);
            ps.setString(10, inv.getNotas());
            ResultSet rs = ps.executeQuery();
            rs.next();
            return rs.getInt(1);
        }
    }

    public static void añadirLinea(LineaFactura linea) throws SQLException {
        String sql = """
                INSERT INTO invoice_lines (invoice_id, description, quantity, unit_price, total_price)
                VALUES (?,?,?,?,?)
                """;
        try (Connection conn = GestorBD.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, linea.getIdFactura());
            ps.setString(2, linea.getDescripcion());
            ps.setBigDecimal(3, linea.getCantidad());
            ps.setBigDecimal(4, linea.getPrecioUnitario());
            ps.setBigDecimal(5, linea.getPrecioTotal());
            ps.executeUpdate();
        }
        recalcularTotales(linea.getIdFactura());
    }

    public static List<LineaFactura> getLineas(int facturaId) throws SQLException {
        String sql = "SELECT * FROM invoice_lines WHERE invoice_id=? ORDER BY id";
        try (Connection conn = GestorBD.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, facturaId);
            ResultSet rs = ps.executeQuery();
            List<LineaFactura> list = new ArrayList<>();
            while (rs.next()) {
                LineaFactura il = new LineaFactura();
                il.setId(rs.getInt("id"));
                il.setIdFactura(facturaId);
                il.setDescripcion(rs.getString("description"));
                il.setCantidad(rs.getBigDecimal("quantity"));
                il.setPrecioUnitario(rs.getBigDecimal("unit_price"));
                il.setPrecioTotal(rs.getBigDecimal("total_price"));
                Timestamp cat = rs.getTimestamp("created_at");
                if (cat != null) il.setFechaCreacion(cat.toLocalDateTime());
                list.add(il);
            }
            return list;
        }
    }

    public static void recalcularTotales(int facturaId) throws SQLException {
        String sql = """
                UPDATE invoices SET
                  subtotal    = sub.s,
                  tax_amount  = sub.s * tax_rate / 100,
                  total_amount = sub.s + (sub.s * tax_rate / 100)
                FROM (SELECT COALESCE(SUM(total_price), 0) AS s
                      FROM invoice_lines WHERE invoice_id = ?) sub
                WHERE id = ?
                """;
        try (Connection conn = GestorBD.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, facturaId);
            ps.setInt(2, facturaId);
            ps.executeUpdate();
        }
        actualizarEstado(facturaId);
    }

    public static void actualizarEstado(int facturaId) throws SQLException {
        String sql = """
                UPDATE invoices SET status = CASE
                  WHEN paid_amount >= total_amount AND total_amount > 0 THEN 'PAID'::invoice_status
                  WHEN paid_amount > 0 THEN 'PARTIALLY_PAID'::invoice_status
                  ELSE 'PENDING'::invoice_status
                END
                WHERE id = ?
                """;
        try (Connection conn = GestorBD.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, facturaId);
            ps.executeUpdate();
        }
    }

    public static List<Factura> getPorRango(LocalDate desde, LocalDate hasta) throws SQLException {
        String sql = BASE_SQL +
                "WHERE i.issue_date BETWEEN ? AND ? ORDER BY i.issue_date DESC";
        try (Connection conn = GestorBD.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setDate(1, Date.valueOf(desde));
            ps.setDate(2, Date.valueOf(hasta));
            return mapearLista(ps.executeQuery());
        }
    }

    public static void cancelar(int facturaId) throws SQLException {
        String sql = "UPDATE invoices SET status = 'CANCELLED'::invoice_status WHERE id = ?";
        try (Connection conn = GestorBD.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, facturaId);
            ps.executeUpdate();
        }
    }

    /** Elimina todas las líneas de una factura (para volver a insertarlas en modo edición). */
    public static void eliminarTodasLineas(int facturaId) throws SQLException {
        String sql = "DELETE FROM invoice_lines WHERE invoice_id = ?";
        try (Connection conn = GestorBD.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, facturaId);
            ps.executeUpdate();
        }
    }

    /** Elimina una línea concreta por su id y recalcula los totales de la factura. */
    public static void eliminarLinea(int lineaId, int facturaId) throws SQLException {
        String sql = "DELETE FROM invoice_lines WHERE id = ?";
        try (Connection conn = GestorBD.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, lineaId);
            ps.executeUpdate();
        }
        recalcularTotales(facturaId);
    }

    /** Actualiza descripción, cantidad y precio de una línea existente y recalcula totales. */
    public static void actualizarLinea(LineaFactura linea) throws SQLException {
        BigDecimal total = linea.getCantidad().multiply(linea.getPrecioUnitario());
        String sql = """
                UPDATE invoice_lines
                SET description = ?, quantity = ?, unit_price = ?, total_price = ?
                WHERE id = ?
                """;
        try (Connection conn = GestorBD.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, linea.getDescripcion());
            ps.setBigDecimal(2, linea.getCantidad());
            ps.setBigDecimal(3, linea.getPrecioUnitario());
            ps.setBigDecimal(4, total);
            ps.setInt(5, linea.getId());
            ps.executeUpdate();
        }
        recalcularTotales(linea.getIdFactura());
    }

    /** Actualiza la forma de pago y las notas de cabecera de la factura. */
    public static void actualizarCabecera(int facturaId, Factura.MetodoPago metodo, String notas)
            throws SQLException {
        String sql = """
                UPDATE invoices
                SET payment_method = ?::payment_method, notes = ?
                WHERE id = ?
                """;
        try (Connection conn = GestorBD.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            if (metodo != null) ps.setString(1, metodo.valorBD);
            else                ps.setNull(1, java.sql.Types.OTHER);
            ps.setString(2, notas);
            ps.setInt(3, facturaId);
            ps.executeUpdate();
        }
    }

    /** Guarda los datos de facturación alternativos (empresa/tercero) de la factura. */
    public static void actualizarDatosFacturacion(int facturaId, String nombreFacturacion, String nifFacturacion,
                                                  String domicilioFacturacion, String ciudadFacturacion,
                                                  String cpFacturacion, String paisFacturacion)
            throws SQLException {
        String sql = """
                UPDATE invoices SET
                  billing_name        = ?,
                  billing_nif         = ?,
                  billing_address     = ?,
                  billing_city        = ?,
                  billing_postal_code = ?,
                  billing_country     = ?
                WHERE id = ?
                """;
        try (Connection conn = GestorBD.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, nullIfBlank(nombreFacturacion));
            ps.setString(2, nullIfBlank(nifFacturacion));
            ps.setString(3, nullIfBlank(domicilioFacturacion));
            ps.setString(4, nullIfBlank(ciudadFacturacion));
            ps.setString(5, nullIfBlank(cpFacturacion));
            ps.setString(6, nullIfBlank(paisFacturacion));
            ps.setInt(7, facturaId);
            ps.executeUpdate();
        }
    }

    private static String nullIfBlank(String s) {
        return (s == null || s.isBlank()) ? null : s.trim();
    }

    private static List<Factura> mapearLista(ResultSet rs) throws SQLException {
        List<Factura> list = new ArrayList<>();
        while (rs.next()) list.add(mapear(rs));
        return list;
    }

    private static Factura mapear(ResultSet rs) throws SQLException {
        Factura inv = new Factura();
        inv.setId(rs.getInt("id"));
        inv.setNumeroFactura(rs.getString("invoice_number"));

        Huesped g = new Huesped();
        g.setId(rs.getInt("guest_id"));
        g.setNombre(rs.getString("first_name"));
        g.setApellidos(rs.getString("last_name"));
        inv.setHuesped(g);

        String resNum = rs.getString("reservation_number");
        if (resNum != null) {
            Reserva res = new Reserva();
            res.setId(rs.getInt("reservation_id"));
            res.setNumeroReserva(resNum);
            inv.setReserva(res);
        }

        Date fechaEmision = rs.getDate("issue_date");
        if (fechaEmision != null) inv.setFechaEmision(fechaEmision.toLocalDate());
        Date fechaVencimiento = rs.getDate("due_date");
        if (fechaVencimiento != null) inv.setFechaVencimiento(fechaVencimiento.toLocalDate());

        inv.setSubtotal(rs.getBigDecimal("subtotal"));
        inv.setTasaImpuesto(rs.getBigDecimal("tax_rate"));
        inv.setImporteImpuesto(rs.getBigDecimal("tax_amount"));
        inv.setImporteTotal(rs.getBigDecimal("total_amount"));
        inv.setImportePagado(rs.getBigDecimal("paid_amount"));
        inv.setEstado(Factura.Estado.desdeBD(rs.getString("status")));

        String pm = rs.getString("payment_method");
        if (pm != null) {
            try { inv.setMetodoPago(Factura.MetodoPago.desdeBD(pm)); } catch (Exception ignored) {}
        }
        inv.setNotas(rs.getString("notes"));

        Timestamp cat = rs.getTimestamp("created_at");
        if (cat != null) inv.setFechaCreacion(cat.toLocalDateTime());

        // Datos de facturación alternativos
        try { inv.setNombreFacturacion(rs.getString("billing_name")); } catch (SQLException ignored) {}
        try { inv.setNifFacturacion(rs.getString("billing_nif")); } catch (SQLException ignored) {}
        try { inv.setDomicilioFacturacion(rs.getString("billing_address")); } catch (SQLException ignored) {}
        try { inv.setCiudadFacturacion(rs.getString("billing_city")); } catch (SQLException ignored) {}
        try { inv.setCpFacturacion(rs.getString("billing_postal_code")); } catch (SQLException ignored) {}
        try { inv.setPaisFacturacion(rs.getString("billing_country")); } catch (SQLException ignored) {}

        return inv;
    }
}

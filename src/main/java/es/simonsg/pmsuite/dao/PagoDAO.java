package es.simonsg.pmsuite.dao;

import es.simonsg.pmsuite.db.GestorBD;
import es.simonsg.pmsuite.model.Factura;
import es.simonsg.pmsuite.model.Pago;

import java.math.BigDecimal;
import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

public class PagoDAO {

    public static void añadirPago(Pago pago) throws SQLException {
        String insertSql = """
                INSERT INTO payments (invoice_id, amount, method, reference, notes)
                VALUES (?,?,?::payment_method,?,?)
                """;
        try (Connection conn = GestorBD.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(insertSql)) {
            ps.setInt(1, pago.getIdFactura());
            ps.setBigDecimal(2, pago.getImporte());
            ps.setString(3, pago.getMetodo().valorBD);
            ps.setString(4, pago.getReferencia());
            ps.setString(5, pago.getNotas());
            ps.executeUpdate();
        }

        String updateSql = """
                UPDATE invoices SET
                  paid_amount = (SELECT COALESCE(SUM(amount), 0) FROM payments WHERE invoice_id = ?)
                WHERE id = ?
                """;
        try (Connection conn = GestorBD.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(updateSql)) {
            ps.setInt(1, pago.getIdFactura());
            ps.setInt(2, pago.getIdFactura());
            ps.executeUpdate();
        }
        FacturaDAO.actualizarEstado(pago.getIdFactura());
    }

    public static List<Pago> getPorFactura(int facturaId) throws SQLException {
        String sql = "SELECT * FROM payments WHERE invoice_id=? ORDER BY paid_at DESC";
        try (Connection conn = GestorBD.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, facturaId);
            ResultSet rs = ps.executeQuery();
            List<Pago> list = new ArrayList<>();
            while (rs.next()) {
                list.add(mapearPago(rs, facturaId));
            }
            return list;
        }
    }

    public static List<Pago> getPorFecha(LocalDate fecha) throws SQLException {
        String sql = """
                SELECT p.*, i.invoice_number, i.guest_id, g.first_name, g.last_name
                FROM payments p
                JOIN invoices i ON i.id = p.invoice_id
                JOIN guests g ON g.id = i.guest_id
                WHERE p.paid_at::date = ?
                ORDER BY p.paid_at DESC
                """;
        try (Connection conn = GestorBD.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setDate(1, Date.valueOf(fecha));
            ResultSet rs = ps.executeQuery();
            List<Pago> list = new ArrayList<>();
            while (rs.next()) {
                Pago p = mapearPago(rs, rs.getInt("invoice_id"));
                p.setNumeroFactura(rs.getString("invoice_number"));
                p.setNombreHuesped(rs.getString("first_name") + " " + rs.getString("last_name"));
                list.add(p);
            }
            return list;
        }
    }

    public static List<Pago> getPorRango(LocalDate desde, LocalDate hasta) throws SQLException {
        String sql = """
                SELECT p.*, i.invoice_number, i.guest_id, g.first_name, g.last_name
                FROM payments p
                JOIN invoices i ON i.id = p.invoice_id
                JOIN guests g ON g.id = i.guest_id
                WHERE p.paid_at::date BETWEEN ? AND ?
                ORDER BY p.paid_at DESC
                """;
        try (Connection conn = GestorBD.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setDate(1, Date.valueOf(desde));
            ps.setDate(2, Date.valueOf(hasta));
            ResultSet rs = ps.executeQuery();
            List<Pago> list = new ArrayList<>();
            while (rs.next()) {
                Pago p = mapearPago(rs, rs.getInt("invoice_id"));
                p.setNumeroFactura(rs.getString("invoice_number"));
                p.setNombreHuesped(rs.getString("first_name") + " " + rs.getString("last_name"));
                list.add(p);
            }
            return list;
        }
    }

    public static Map<Factura.MetodoPago, BigDecimal> getResumenDiario(LocalDate fecha) throws SQLException {
        String sql = """
                SELECT method, COALESCE(SUM(amount), 0) AS total
                FROM payments
                WHERE paid_at::date = ?
                GROUP BY method
                """;
        Map<Factura.MetodoPago, BigDecimal> resumen = new EnumMap<>(Factura.MetodoPago.class);
        for (Factura.MetodoPago m : Factura.MetodoPago.values()) {
            resumen.put(m, BigDecimal.ZERO);
        }
        try (Connection conn = GestorBD.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setDate(1, Date.valueOf(fecha));
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                try {
                    Factura.MetodoPago metodo = Factura.MetodoPago.desdeBD(rs.getString("method"));
                    resumen.put(metodo, rs.getBigDecimal("total"));
                } catch (IllegalArgumentException ignored) {}
            }
        }
        return resumen;
    }

    private static Pago mapearPago(ResultSet rs, int facturaId) throws SQLException {
        Pago p = new Pago();
        p.setId(rs.getInt("id"));
        p.setIdFactura(facturaId);
        p.setImporte(rs.getBigDecimal("amount"));
        try { p.setMetodo(Factura.MetodoPago.desdeBD(rs.getString("method"))); } catch (Exception ignored) {}
        p.setReferencia(rs.getString("reference"));
        Timestamp paidAt = rs.getTimestamp("paid_at");
        if (paidAt != null) p.setFechaPago(paidAt.toLocalDateTime());
        p.setNotas(rs.getString("notes"));
        return p;
    }
}


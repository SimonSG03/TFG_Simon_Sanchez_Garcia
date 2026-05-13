package es.simonsg.pmsuite.dao;

import es.simonsg.pmsuite.db.GestorBD;
import es.simonsg.pmsuite.model.PlantillaEmail;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class PlantillaEmailDAO {

    public static List<PlantillaEmail> obtenerTodas() throws SQLException {
        String sql = "SELECT id, name, type, subject, body, active, created_at, updated_at " +
                "FROM email_templates ORDER BY type, name";
        try (Connection conn = GestorBD.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            return mapearLista(ps.executeQuery());
        }
    }

    public static List<PlantillaEmail> obtenerActivas() throws SQLException {
        String sql = "SELECT id, name, type, subject, body, active, created_at, updated_at " +
                "FROM email_templates WHERE active = TRUE ORDER BY type, name";
        try (Connection conn = GestorBD.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            return mapearLista(ps.executeQuery());
        }
    }

    public static PlantillaEmail buscarPorId(int id) throws SQLException {
        String sql = "SELECT id, name, type, subject, body, active, created_at, updated_at " +
                "FROM email_templates WHERE id = ?";
        try (Connection conn = GestorBD.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
            return rs.next() ? mapear(rs) : null;
        }
    }

    public static int crear(PlantillaEmail t) throws SQLException {
        String sql = """
                INSERT INTO email_templates (name, type, subject, body, active)
                VALUES (?, ?, ?, ?, ?)
                RETURNING id
                """;
        try (Connection conn = GestorBD.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, t.getNombre());
            ps.setString(2, t.getTipo().valorBD);
            ps.setString(3, t.getAsunto());
            ps.setString(4, t.getCuerpo());
            ps.setBoolean(5, t.isActivo());
            ResultSet rs = ps.executeQuery();
            rs.next();
            return rs.getInt(1);
        }
    }

    public static void actualizar(PlantillaEmail t) throws SQLException {
        String sql = """
                UPDATE email_templates
                SET name=?, type=?, subject=?, body=?, active=?, updated_at=CURRENT_TIMESTAMP
                WHERE id=?
                """;
        try (Connection conn = GestorBD.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, t.getNombre());
            ps.setString(2, t.getTipo().valorBD);
            ps.setString(3, t.getAsunto());
            ps.setString(4, t.getCuerpo());
            ps.setBoolean(5, t.isActivo());
            ps.setInt(6, t.getId());
            ps.executeUpdate();
        }
    }

    public static void eliminar(int id) throws SQLException {
        String sql = "DELETE FROM email_templates WHERE id = ?";
        try (Connection conn = GestorBD.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        }
    }

    private static List<PlantillaEmail> mapearLista(ResultSet rs) throws SQLException {
        List<PlantillaEmail> list = new ArrayList<>();
        while (rs.next()) list.add(mapear(rs));
        return list;
    }

    private static PlantillaEmail mapear(ResultSet rs) throws SQLException {
        PlantillaEmail t = new PlantillaEmail();
        t.setId(rs.getInt("id"));
        t.setNombre(rs.getString("name"));
        String type = rs.getString("type");
        if (type != null) {
            try { t.setTipo(PlantillaEmail.Tipo.desdeBD(type)); }
            catch (IllegalArgumentException e) { t.setTipo(PlantillaEmail.Tipo.PERSONALIZADO); }
        }
        t.setAsunto(rs.getString("subject"));
        t.setCuerpo(rs.getString("body"));
        t.setActivo(rs.getBoolean("active"));
        Timestamp cat = rs.getTimestamp("created_at");
        if (cat != null) t.setFechaCreacion(cat.toLocalDateTime());
        Timestamp uat = rs.getTimestamp("updated_at");
        if (uat != null) t.setFechaActualizacion(uat.toLocalDateTime());
        return t;
    }
}

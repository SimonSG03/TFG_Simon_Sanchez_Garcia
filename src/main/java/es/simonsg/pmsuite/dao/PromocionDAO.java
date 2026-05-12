package es.simonsg.pmsuite.dao;

import es.simonsg.pmsuite.db.GestorBD;
import es.simonsg.pmsuite.model.Promocion;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class PromocionDAO {

    public static List<Promocion> obtenerTodas() throws SQLException {
        String sql = "SELECT * FROM promotions ORDER BY start_date DESC";
        try (Connection conn = GestorBD.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            return mapearLista(ps.executeQuery());
        }
    }

    public static List<Promocion> obtenerActivas() throws SQLException {
        String sql = "SELECT * FROM promotions WHERE active=TRUE AND end_date >= CURRENT_DATE ORDER BY start_date";
        try (Connection conn = GestorBD.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            return mapearLista(ps.executeQuery());
        }
    }

    public static void guardar(Promocion p) throws SQLException {
        String sql = "INSERT INTO promotions (name, code, discount_type, discount_value, start_date, end_date, active, notes) VALUES (?,?,?,?,?,?,?,?)";
        try (Connection conn = GestorBD.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, p.getNombre());
            ps.setString(2, p.getCodigo());
            ps.setString(3, p.getTipoDescuento());
            ps.setBigDecimal(4, p.getValorDescuento());
            ps.setDate(5, Date.valueOf(p.getFechaInicio()));
            ps.setDate(6, Date.valueOf(p.getFechaFin()));
            ps.setBoolean(7, p.isActivo());
            ps.setString(8, p.getNotas());
            ps.executeUpdate();
            ResultSet keys = ps.getGeneratedKeys();
            if (keys.next()) p.setId(keys.getInt(1));
        }
    }

    public static void actualizar(Promocion p) throws SQLException {
        String sql = "UPDATE promotions SET name=?, code=?, discount_type=?, discount_value=?, start_date=?, end_date=?, active=?, notes=? WHERE id=?";
        try (Connection conn = GestorBD.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, p.getNombre());
            ps.setString(2, p.getCodigo());
            ps.setString(3, p.getTipoDescuento());
            ps.setBigDecimal(4, p.getValorDescuento());
            ps.setDate(5, Date.valueOf(p.getFechaInicio()));
            ps.setDate(6, Date.valueOf(p.getFechaFin()));
            ps.setBoolean(7, p.isActivo());
            ps.setString(8, p.getNotas());
            ps.setInt(9, p.getId());
            ps.executeUpdate();
        }
    }

    public static void eliminar(int id) throws SQLException {
        String sql = "DELETE FROM promotions WHERE id=?";
        try (Connection conn = GestorBD.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        }
    }

    private static List<Promocion> mapearLista(ResultSet rs) throws SQLException {
        List<Promocion> list = new ArrayList<>();
        while (rs.next()) list.add(mapear(rs));
        return list;
    }

    private static Promocion mapear(ResultSet rs) throws SQLException {
        Promocion p = new Promocion();
        p.setId(rs.getInt("id"));
        p.setNombre(rs.getString("name"));
        p.setCodigo(rs.getString("code"));
        p.setTipoDescuento(rs.getString("discount_type"));
        p.setValorDescuento(rs.getBigDecimal("discount_value"));
        p.setFechaInicio(rs.getDate("start_date").toLocalDate());
        p.setFechaFin(rs.getDate("end_date").toLocalDate());
        p.setActivo(rs.getBoolean("active"));
        p.setNotas(rs.getString("notes"));
        return p;
    }
}

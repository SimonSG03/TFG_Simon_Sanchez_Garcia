package es.simonsg.pmsuite.dao;

import es.simonsg.pmsuite.db.GestorBD;
import es.simonsg.pmsuite.model.MesaRestaurante;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class MesaRestauranteDAO {

    private static final String BASE_SQL =
            "SELECT id, zone, name, capacity, sort_order, pos_x, pos_y, active FROM restaurant_tables ";

    public static List<MesaRestaurante> obtenerTodas() throws SQLException {
        String sql = BASE_SQL + "WHERE active = TRUE ORDER BY zone, sort_order, name";
        try (Connection conn = GestorBD.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            return mapearLista(ps.executeQuery());
        }
    }

    public static List<MesaRestaurante> obtenerPorZona(String zona) throws SQLException {
        String sql = BASE_SQL + "WHERE zone = ? AND active = TRUE ORDER BY sort_order, name";
        try (Connection conn = GestorBD.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, zona);
            return mapearLista(ps.executeQuery());
        }
    }

    public static int crear(MesaRestaurante t) throws SQLException {
        String sql = "INSERT INTO restaurant_tables (zone, name, capacity, sort_order, pos_x, pos_y, active) VALUES (?,?,?,?,?,?,?) RETURNING id";
        try (Connection conn = GestorBD.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, t.getZona());
            ps.setString(2, t.getNombre());
            ps.setInt(3, t.getCapacidad());
            ps.setInt(4, t.getOrden());
            ps.setDouble(5, t.getPosX());
            ps.setDouble(6, t.getPosY());
            ps.setBoolean(7, t.isActivo());
            ResultSet rs = ps.executeQuery();
            rs.next();
            return rs.getInt(1);
        }
    }

    public static void actualizar(MesaRestaurante t) throws SQLException {
        String sql = "UPDATE restaurant_tables SET zone=?, name=?, capacity=?, sort_order=?, active=? WHERE id=?";
        try (Connection conn = GestorBD.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, t.getZona());
            ps.setString(2, t.getNombre());
            ps.setInt(3, t.getCapacidad());
            ps.setInt(4, t.getOrden());
            ps.setBoolean(5, t.isActivo());
            ps.setInt(6, t.getId());
            ps.executeUpdate();
        }
    }

    public static void actualizarPosicion(int id, double x, double y) throws SQLException {
        String sql = "UPDATE restaurant_tables SET pos_x=?, pos_y=? WHERE id=?";
        try (Connection conn = GestorBD.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setDouble(1, x);
            ps.setDouble(2, y);
            ps.setInt(3, id);
            ps.executeUpdate();
        }
    }

    public static void eliminar(int id) throws SQLException {
        String sql = "UPDATE restaurant_tables SET active=FALSE WHERE id=?";
        try (Connection conn = GestorBD.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        }
    }

    private static List<MesaRestaurante> mapearLista(ResultSet rs) throws SQLException {
        List<MesaRestaurante> list = new ArrayList<>();
        while (rs.next()) list.add(mapear(rs));
        return list;
    }

    private static MesaRestaurante mapear(ResultSet rs) throws SQLException {
        MesaRestaurante t = new MesaRestaurante();
        t.setId(rs.getInt("id"));
        t.setZona(rs.getString("zone"));
        t.setNombre(rs.getString("name"));
        t.setCapacidad(rs.getInt("capacity"));
        t.setOrden(rs.getInt("sort_order"));
        try { t.setPosX(rs.getDouble("pos_x")); } catch (SQLException ignored) {}
        try { t.setPosY(rs.getDouble("pos_y")); } catch (SQLException ignored) {}
        t.setActivo(rs.getBoolean("active"));
        return t;
    }
}

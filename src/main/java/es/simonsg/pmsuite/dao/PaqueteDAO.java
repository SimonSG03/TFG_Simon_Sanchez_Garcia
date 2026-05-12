package es.simonsg.pmsuite.dao;

import es.simonsg.pmsuite.db.GestorBD;
import es.simonsg.pmsuite.model.Paquete;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class PaqueteDAO {

    public static List<Paquete> obtenerTodos() throws SQLException {
        String sql = "SELECT * FROM packages ORDER BY name";
        try (Connection conn = GestorBD.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            return mapearLista(ps.executeQuery());
        }
    }

    public static List<Paquete> obtenerActivos() throws SQLException {
        String sql = "SELECT * FROM packages WHERE active=TRUE ORDER BY name";
        try (Connection conn = GestorBD.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            return mapearLista(ps.executeQuery());
        }
    }

    public static void guardar(Paquete p) throws SQLException {
        String sql = "INSERT INTO packages (name, description, base_price, contents, active) VALUES (?,?,?,?,?)";
        try (Connection conn = GestorBD.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, p.getNombre());
            ps.setString(2, p.getDescripcion());
            ps.setBigDecimal(3, p.getPrecioBase());
            ps.setString(4, p.getContenido());
            ps.setBoolean(5, p.isActivo());
            ps.executeUpdate();
            ResultSet keys = ps.getGeneratedKeys();
            if (keys.next()) p.setId(keys.getInt(1));
        }
    }

    public static void actualizar(Paquete p) throws SQLException {
        String sql = "UPDATE packages SET name=?, description=?, base_price=?, contents=?, active=? WHERE id=?";
        try (Connection conn = GestorBD.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, p.getNombre());
            ps.setString(2, p.getDescripcion());
            ps.setBigDecimal(3, p.getPrecioBase());
            ps.setString(4, p.getContenido());
            ps.setBoolean(5, p.isActivo());
            ps.setInt(6, p.getId());
            ps.executeUpdate();
        }
    }

    public static void eliminar(int id) throws SQLException {
        String sql = "DELETE FROM packages WHERE id=?";
        try (Connection conn = GestorBD.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        }
    }

    private static List<Paquete> mapearLista(ResultSet rs) throws SQLException {
        List<Paquete> list = new ArrayList<>();
        while (rs.next()) list.add(mapear(rs));
        return list;
    }

    private static Paquete mapear(ResultSet rs) throws SQLException {
        Paquete p = new Paquete();
        p.setId(rs.getInt("id"));
        p.setNombre(rs.getString("name"));
        p.setDescripcion(rs.getString("description"));
        p.setPrecioBase(rs.getBigDecimal("base_price"));
        p.setContenido(rs.getString("contents"));
        p.setActivo(rs.getBoolean("active"));
        return p;
    }
}

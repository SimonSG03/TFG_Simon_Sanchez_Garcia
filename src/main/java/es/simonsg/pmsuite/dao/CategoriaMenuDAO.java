package es.simonsg.pmsuite.dao;

import es.simonsg.pmsuite.db.GestorBD;
import es.simonsg.pmsuite.model.CategoriaMenu;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class CategoriaMenuDAO {

    private static final String BASE_SQL =
            "SELECT id, name, description, color, active FROM menu_categories ";

    public static List<CategoriaMenu> obtenerTodas() throws SQLException {
        String sql = BASE_SQL + "ORDER BY name";
        try (Connection conn = GestorBD.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            return mapearLista(ps.executeQuery());
        }
    }

    public static List<CategoriaMenu> obtenerActivas() throws SQLException {
        String sql = BASE_SQL + "WHERE active = TRUE ORDER BY name";
        try (Connection conn = GestorBD.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            return mapearLista(ps.executeQuery());
        }
    }

    public static CategoriaMenu buscarPorId(int id) throws SQLException {
        String sql = BASE_SQL + "WHERE id = ?";
        try (Connection conn = GestorBD.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
            return rs.next() ? mapear(rs) : null;
        }
    }

    public static int crear(CategoriaMenu cat) throws SQLException {
        String sql = "INSERT INTO menu_categories (name, description, color, active) VALUES (?,?,?,?) RETURNING id";
        try (Connection conn = GestorBD.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, cat.getNombre());
            ps.setString(2, cat.getDescripcion());
            ps.setString(3, cat.getColor() != null ? cat.getColor() : "#607D8B");
            ps.setBoolean(4, cat.isActivo());
            ResultSet rs = ps.executeQuery();
            rs.next();
            return rs.getInt(1);
        }
    }

    public static void actualizar(CategoriaMenu cat) throws SQLException {
        String sql = "UPDATE menu_categories SET name=?, description=?, color=?, active=? WHERE id=?";
        try (Connection conn = GestorBD.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, cat.getNombre());
            ps.setString(2, cat.getDescripcion());
            ps.setString(3, cat.getColor() != null ? cat.getColor() : "#607D8B");
            ps.setBoolean(4, cat.isActivo());
            ps.setInt(5, cat.getId());
            ps.executeUpdate();
        }
    }

    public static void eliminar(int id) throws SQLException {
        String sql = "DELETE FROM menu_categories WHERE id=?";
        try (Connection conn = GestorBD.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        }
    }

    private static List<CategoriaMenu> mapearLista(ResultSet rs) throws SQLException {
        List<CategoriaMenu> list = new ArrayList<>();
        while (rs.next()) list.add(mapear(rs));
        return list;
    }

    public static CategoriaMenu mapear(ResultSet rs) throws SQLException {
        CategoriaMenu c = new CategoriaMenu();
        c.setId(rs.getInt("id"));
        c.setNombre(rs.getString("name"));
        c.setDescripcion(rs.getString("description"));
        try { c.setColor(rs.getString("color")); } catch (SQLException ignored) {}
        c.setActivo(rs.getBoolean("active"));
        return c;
    }
}

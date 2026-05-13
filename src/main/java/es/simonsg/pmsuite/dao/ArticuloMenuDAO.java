package es.simonsg.pmsuite.dao;

import es.simonsg.pmsuite.db.GestorBD;
import es.simonsg.pmsuite.model.CategoriaMenu;
import es.simonsg.pmsuite.model.ArticuloMenu;

import java.math.BigDecimal;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ArticuloMenuDAO {

    private static final String BASE_SQL = """
            SELECT mi.id, mi.name, mi.description, mi.price, mi.available, mi.allergens,
                   mc.id as cat_id, mc.name as cat_name, mc.color as cat_color
            FROM menu_items mi
            LEFT JOIN menu_categories mc ON mc.id = mi.category_id
            """;

    public static List<ArticuloMenu> obtenerTodos() throws SQLException {
        String sql = BASE_SQL + "ORDER BY mc.name, mi.name";
        try (Connection conn = GestorBD.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            return mapearLista(ps.executeQuery());
        }
    }

    public static List<ArticuloMenu> obtenerPorCategoria(int categoriaId) throws SQLException {
        String sql = BASE_SQL + "WHERE mi.category_id = ? AND mi.available = TRUE ORDER BY mi.name";
        try (Connection conn = GestorBD.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, categoriaId);
            return mapearLista(ps.executeQuery());
        }
    }

    public static List<ArticuloMenu> obtenerDisponibles() throws SQLException {
        String sql = BASE_SQL + "WHERE mi.available = TRUE ORDER BY mc.name, mi.name";
        try (Connection conn = GestorBD.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            return mapearLista(ps.executeQuery());
        }
    }

    public static ArticuloMenu buscarPorId(int id) throws SQLException {
        String sql = BASE_SQL + "WHERE mi.id = ?";
        try (Connection conn = GestorBD.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
            return rs.next() ? mapear(rs) : null;
        }
    }

    public static int crear(ArticuloMenu item) throws SQLException {
        String sql = """
                INSERT INTO menu_items (category_id, name, description, price, available, allergens)
                VALUES (?,?,?,?,?,?) RETURNING id
                """;
        try (Connection conn = GestorBD.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            if (item.getCategoria() != null) ps.setInt(1, item.getCategoria().getId());
            else ps.setNull(1, Types.INTEGER);
            ps.setString(2, item.getNombre());
            ps.setString(3, item.getDescripcion());
            ps.setBigDecimal(4, item.getPrecio() != null ? item.getPrecio() : BigDecimal.ZERO);
            ps.setBoolean(5, item.isDisponible());
            ps.setString(6, item.getAlergenos());
            ResultSet rs = ps.executeQuery();
            rs.next();
            return rs.getInt(1);
        }
    }

    public static void actualizar(ArticuloMenu item) throws SQLException {
        String sql = """
                UPDATE menu_items SET category_id=?, name=?, description=?, price=?, available=?, allergens=?
                WHERE id=?
                """;
        try (Connection conn = GestorBD.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            if (item.getCategoria() != null) ps.setInt(1, item.getCategoria().getId());
            else ps.setNull(1, Types.INTEGER);
            ps.setString(2, item.getNombre());
            ps.setString(3, item.getDescripcion());
            ps.setBigDecimal(4, item.getPrecio() != null ? item.getPrecio() : BigDecimal.ZERO);
            ps.setBoolean(5, item.isDisponible());
            ps.setString(6, item.getAlergenos());
            ps.setInt(7, item.getId());
            ps.executeUpdate();
        }
    }

    public static void eliminar(int id) throws SQLException {
        String sql = "DELETE FROM menu_items WHERE id=?";
        try (Connection conn = GestorBD.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        }
    }

    private static List<ArticuloMenu> mapearLista(ResultSet rs) throws SQLException {
        List<ArticuloMenu> list = new ArrayList<>();
        while (rs.next()) list.add(mapear(rs));
        return list;
    }

    public static ArticuloMenu mapear(ResultSet rs) throws SQLException {
        ArticuloMenu m = new ArticuloMenu();
        m.setId(rs.getInt("id"));
        m.setNombre(rs.getString("name"));
        m.setDescripcion(rs.getString("description"));
        m.setPrecio(rs.getBigDecimal("price"));
        m.setDisponible(rs.getBoolean("available"));
        m.setAlergenos(rs.getString("allergens"));
        int catId = rs.getInt("cat_id");
        if (!rs.wasNull()) {
            CategoriaMenu cat = new CategoriaMenu();
            cat.setId(catId);
            cat.setNombre(rs.getString("cat_name"));
            try { cat.setColor(rs.getString("cat_color")); } catch (SQLException ignored) {}
            m.setCategoria(cat);
        }
        return m;
    }
}

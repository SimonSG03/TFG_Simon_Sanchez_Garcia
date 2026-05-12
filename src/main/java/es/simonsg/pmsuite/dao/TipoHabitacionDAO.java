package es.simonsg.pmsuite.dao;

import es.simonsg.pmsuite.db.GestorBD;
import es.simonsg.pmsuite.model.TipoHabitacion;

import java.math.BigDecimal;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class TipoHabitacionDAO {

    private static final String BASE_SQL =
            "SELECT id, code, name, description, max_occupancy, base_price, amenities " +
                    "FROM room_types ";

    public static List<TipoHabitacion> obtenerTodos() throws SQLException {
        String sql = BASE_SQL + "ORDER BY name";
        try (Connection conn = GestorBD.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            return mapearLista(ps.executeQuery());
        }
    }

    public static TipoHabitacion buscarPorId(int id) throws SQLException {
        String sql = BASE_SQL + "WHERE id = ?";
        try (Connection conn = GestorBD.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
            return rs.next() ? mapear(rs) : null;
        }
    }

    public static int crear(TipoHabitacion rt) throws SQLException {
        String sql = """
                INSERT INTO room_types (code, name, description, max_occupancy, base_price, amenities)
                VALUES (?, ?, ?, ?, ?, ?) RETURNING id
                """;
        try (Connection conn = GestorBD.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, rt.getCodigo());
            ps.setString(2, rt.getNombre());
            ps.setString(3, rt.getDescripcion());
            ps.setInt(4, rt.getCapacidadMaxima() > 0 ? rt.getCapacidadMaxima() : 2);
            ps.setBigDecimal(5, rt.getPrecioBase() != null ? rt.getPrecioBase() : BigDecimal.ZERO);
            ps.setString(6, rt.getAmenidades());
            ResultSet rs = ps.executeQuery();
            rs.next();
            return rs.getInt(1);
        }
    }

    public static void actualizar(TipoHabitacion rt) throws SQLException {
        String sql = """
                UPDATE room_types SET code=?, name=?, description=?,
                  max_occupancy=?, base_price=?, amenities=?
                WHERE id=?
                """;
        try (Connection conn = GestorBD.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, rt.getCodigo());
            ps.setString(2, rt.getNombre());
            ps.setString(3, rt.getDescripcion());
            ps.setInt(4, rt.getCapacidadMaxima() > 0 ? rt.getCapacidadMaxima() : 2);
            ps.setBigDecimal(5, rt.getPrecioBase() != null ? rt.getPrecioBase() : BigDecimal.ZERO);
            ps.setString(6, rt.getAmenidades());
            ps.setInt(7, rt.getId());
            ps.executeUpdate();
        }
    }

    public static void eliminar(int id) throws SQLException {
        String sql = "DELETE FROM room_types WHERE id=?";
        try (Connection conn = GestorBD.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        }
    }

    private static List<TipoHabitacion> mapearLista(ResultSet rs) throws SQLException {
        List<TipoHabitacion> list = new ArrayList<>();
        while (rs.next()) list.add(mapear(rs));
        return list;
    }

    public static TipoHabitacion mapear(ResultSet rs) throws SQLException {
        TipoHabitacion rt = new TipoHabitacion();
        rt.setId(rs.getInt("id"));
        rt.setCodigo(rs.getString("code"));
        rt.setNombre(rs.getString("name"));
        rt.setDescripcion(rs.getString("description"));
        rt.setCapacidadMaxima(rs.getInt("max_occupancy"));
        rt.setPrecioBase(rs.getBigDecimal("base_price"));
        rt.setAmenidades(rs.getString("amenities"));
        return rt;
    }
}


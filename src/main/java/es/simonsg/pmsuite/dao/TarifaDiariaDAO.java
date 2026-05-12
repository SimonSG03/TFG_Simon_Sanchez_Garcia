package es.simonsg.pmsuite.dao;

import es.simonsg.pmsuite.db.GestorBD;
import es.simonsg.pmsuite.model.TarifaDiaria;
import es.simonsg.pmsuite.model.TipoHabitacion;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class TarifaDiariaDAO {

    private static final String BASE_SQL = """
            SELECT dr.*, rt.id as rt_id, rt.code, rt.name as rt_name, rt.max_occupancy, rt.base_price
            FROM daily_rates dr
            LEFT JOIN room_types rt ON rt.id = dr.room_type_id
            """;

    public static List<TarifaDiaria> obtenerPorRango(LocalDate desde, LocalDate hasta) throws SQLException {
        String sql = BASE_SQL + "WHERE dr.rate_date BETWEEN ? AND ? ORDER BY dr.rate_date, rt.code";
        try (Connection conn = GestorBD.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setDate(1, Date.valueOf(desde));
            ps.setDate(2, Date.valueOf(hasta));
            return mapearLista(ps.executeQuery());
        }
    }

    public static List<TarifaDiaria> obtenerTodas() throws SQLException {
        String sql = BASE_SQL + "ORDER BY dr.rate_date, rt.code";
        try (Connection conn = GestorBD.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            return mapearLista(ps.executeQuery());
        }
    }

    public static void guardar(TarifaDiaria dr) throws SQLException {
        String sql = "INSERT INTO daily_rates (room_type_id, rate_date, price, notes) VALUES (?, ?, ?, ?) " +
                "ON CONFLICT (room_type_id, rate_date) DO UPDATE SET price=EXCLUDED.price, notes=EXCLUDED.notes";
        try (Connection conn = GestorBD.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, dr.getTipoHabitacion().getId());
            ps.setDate(2, Date.valueOf(dr.getFecha()));
            ps.setBigDecimal(3, dr.getPrecio());
            ps.setString(4, dr.getNotas());
            ps.executeUpdate();
            ResultSet keys = ps.getGeneratedKeys();
            if (keys.next()) dr.setId(keys.getInt(1));
        }
    }

    public static void eliminar(int id) throws SQLException {
        String sql = "DELETE FROM daily_rates WHERE id=?";
        try (Connection conn = GestorBD.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        }
    }

    private static List<TarifaDiaria> mapearLista(ResultSet rs) throws SQLException {
        List<TarifaDiaria> list = new ArrayList<>();
        while (rs.next()) list.add(mapear(rs));
        return list;
    }

    private static TarifaDiaria mapear(ResultSet rs) throws SQLException {
        TarifaDiaria dr = new TarifaDiaria();
        dr.setId(rs.getInt("id"));
        dr.setFecha(rs.getDate("rate_date").toLocalDate());
        dr.setPrecio(rs.getBigDecimal("price"));
        dr.setNotas(rs.getString("notes"));

        TipoHabitacion rt = new TipoHabitacion();
        rt.setId(rs.getInt("rt_id"));
        rt.setCodigo(rs.getString("code"));
        rt.setNombre(rs.getString("rt_name"));
        rt.setCapacidadMaxima(rs.getInt("max_occupancy"));
        rt.setPrecioBase(rs.getBigDecimal("base_price"));
        dr.setTipoHabitacion(rt);
        return dr;
    }
}

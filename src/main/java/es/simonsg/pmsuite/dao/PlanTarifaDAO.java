package es.simonsg.pmsuite.dao;

import es.simonsg.pmsuite.db.GestorBD;
import es.simonsg.pmsuite.model.PlanTarifa;
import es.simonsg.pmsuite.model.TipoHabitacion;
import es.simonsg.pmsuite.model.Temporada;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class PlanTarifaDAO {

    private static final String BASE_SQL = """
            SELECT rp.*,
                   rt.id as rt_id, rt.code, rt.name as rt_name, rt.max_occupancy, rt.base_price,
                   s.id as s_id, s.name as s_name, s.start_date, s.end_date, s.multiplier
            FROM rate_plans rp
            LEFT JOIN room_types rt ON rt.id = rp.room_type_id
            LEFT JOIN seasons s ON s.id = rp.season_id
            """;

    public static List<PlanTarifa> obtenerTodos() throws SQLException {
        String sql = BASE_SQL + "ORDER BY rt.code, s.start_date";
        try (Connection conn = GestorBD.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            return mapearLista(ps.executeQuery());
        }
    }

    public static List<PlanTarifa> obtenerPorTipoHabitacion(int tipoHabitacionId) throws SQLException {
        String sql = BASE_SQL + "WHERE rp.room_type_id=? ORDER BY s.start_date";
        try (Connection conn = GestorBD.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, tipoHabitacionId);
            return mapearLista(ps.executeQuery());
        }
    }

    public static void guardar(PlanTarifa rp) throws SQLException {
        String sql = """
                INSERT INTO rate_plans (room_type_id, season_id, name, price_per_night, min_nights, max_nights, includes_breakfast)
                VALUES (?, ?, ?, ?, ?, ?, ?)
                """;
        try (Connection conn = GestorBD.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, rp.getTipoHabitacion().getId());
            if (rp.getTemporada() != null) ps.setInt(2, rp.getTemporada().getId());
            else ps.setNull(2, Types.INTEGER);
            ps.setString(3, rp.getNombre());
            ps.setBigDecimal(4, rp.getPrecioPorNoche());
            ps.setInt(5, rp.getNochesMinimas());
            if (rp.getNochesMaximas() != null) ps.setInt(6, rp.getNochesMaximas());
            else ps.setNull(6, Types.INTEGER);
            ps.setBoolean(7, rp.isIncluyeDesayuno());
            ps.executeUpdate();
            ResultSet keys = ps.getGeneratedKeys();
            if (keys.next()) rp.setId(keys.getInt(1));
        }
    }

    public static void actualizar(PlanTarifa rp) throws SQLException {
        String sql = """
                UPDATE rate_plans SET room_type_id=?, season_id=?, name=?,
                price_per_night=?, min_nights=?, max_nights=?, includes_breakfast=?
                WHERE id=?
                """;
        try (Connection conn = GestorBD.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, rp.getTipoHabitacion().getId());
            if (rp.getTemporada() != null) ps.setInt(2, rp.getTemporada().getId());
            else ps.setNull(2, Types.INTEGER);
            ps.setString(3, rp.getNombre());
            ps.setBigDecimal(4, rp.getPrecioPorNoche());
            ps.setInt(5, rp.getNochesMinimas());
            if (rp.getNochesMaximas() != null) ps.setInt(6, rp.getNochesMaximas());
            else ps.setNull(6, Types.INTEGER);
            ps.setBoolean(7, rp.isIncluyeDesayuno());
            ps.setInt(8, rp.getId());
            ps.executeUpdate();
        }
    }

    public static void eliminar(int id) throws SQLException {
        String sql = "DELETE FROM rate_plans WHERE id=?";
        try (Connection conn = GestorBD.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        }
    }

    private static List<PlanTarifa> mapearLista(ResultSet rs) throws SQLException {
        List<PlanTarifa> list = new ArrayList<>();
        while (rs.next()) list.add(mapear(rs));
        return list;
    }

    private static PlanTarifa mapear(ResultSet rs) throws SQLException {
        PlanTarifa rp = new PlanTarifa();
        rp.setId(rs.getInt("id"));
        rp.setNombre(rs.getString("name"));
        rp.setPrecioPorNoche(rs.getBigDecimal("price_per_night"));
        rp.setNochesMinimas(rs.getInt("min_nights"));
        int maxN = rs.getInt("max_nights");
        rp.setNochesMaximas(rs.wasNull() ? null : maxN);
        rp.setIncluyeDesayuno(rs.getBoolean("includes_breakfast"));

        TipoHabitacion rt = new TipoHabitacion();
        rt.setId(rs.getInt("rt_id"));
        rt.setCodigo(rs.getString("code"));
        rt.setNombre(rs.getString("rt_name"));
        rt.setCapacidadMaxima(rs.getInt("max_occupancy"));
        rt.setPrecioBase(rs.getBigDecimal("base_price"));
        rp.setTipoHabitacion(rt);

        int sId = rs.getInt("s_id");
        if (!rs.wasNull()) {
            Temporada s = new Temporada();
            s.setId(sId);
            s.setNombre(rs.getString("s_name"));
            s.setFechaInicio(rs.getDate("start_date").toLocalDate());
            s.setFechaFin(rs.getDate("end_date").toLocalDate());
            s.setMultiplicador(rs.getBigDecimal("multiplier"));
            rp.setTemporada(s);
        }
        return rp;
    }
}

package es.simonsg.pmsuite.dao;

import es.simonsg.pmsuite.db.GestorBD;
import es.simonsg.pmsuite.model.SolicitudMantenimiento;
import es.simonsg.pmsuite.model.Habitacion;
import es.simonsg.pmsuite.model.TipoHabitacion;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class MantenimientoDAO {

    private static final String BASE_SQL = """
            SELECT mr.*,
                   r.number AS room_number, r.floor, r.status AS room_status,
                   rt.id AS rt_id, rt.code AS rt_code, rt.name AS rt_name,
                   rt.max_occupancy, rt.base_price
            FROM maintenance_requests mr
            LEFT JOIN rooms r  ON r.id  = mr.room_id
            LEFT JOIN room_types rt ON rt.id = r.room_type_id
            """;

    public static List<SolicitudMantenimiento> obtenerTodas() throws SQLException {
        String sql = BASE_SQL + "ORDER BY mr.reported_at DESC";
        try (Connection conn = GestorBD.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            return mapearLista(ps.executeQuery());
        }
    }

    public static List<SolicitudMantenimiento> obtenerActivas() throws SQLException {
        String sql = BASE_SQL + "WHERE mr.status IN ('PENDING','IN_PROGRESS') ORDER BY mr.reported_at DESC";
        try (Connection conn = GestorBD.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            return mapearLista(ps.executeQuery());
        }
    }

    public static List<SolicitudMantenimiento> getHistorial() throws SQLException {
        String sql = BASE_SQL + "WHERE mr.status IN ('COMPLETED','CANCELLED') ORDER BY mr.reported_at DESC";
        try (Connection conn = GestorBD.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            return mapearLista(ps.executeQuery());
        }
    }

    public static List<SolicitudMantenimiento> getPorHabitacion(int habitacionId) throws SQLException {
        String sql = BASE_SQL + "WHERE mr.room_id = ? AND mr.status IN ('PENDING','IN_PROGRESS') ORDER BY mr.reported_at DESC";
        try (Connection conn = GestorBD.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, habitacionId);
            return mapearLista(ps.executeQuery());
        }
    }

    public static int crear(SolicitudMantenimiento mr) throws SQLException {
        String sql = """
                INSERT INTO maintenance_requests (room_id, title, description, priority, status, notes)
                VALUES (?, ?, ?, ?::maintenance_priority, ?::maintenance_status, ?)
                RETURNING id
                """;
        try (Connection conn = GestorBD.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            if (mr.getHabitacion() != null) ps.setInt(1, mr.getHabitacion().getId());
            else                            ps.setNull(1, Types.INTEGER);
            ps.setString(2, mr.getTitulo());
            ps.setString(3, mr.getDescripcion());
            ps.setString(4, mr.getPrioridad().valorBD);
            ps.setString(5, mr.getEstado().valorBD);
            ps.setString(6, mr.getNotas());
            ResultSet rs = ps.executeQuery();
            return rs.next() ? rs.getInt(1) : -1;
        }
    }

    public static void actualizar(SolicitudMantenimiento mr) throws SQLException {
        String sql = """
                UPDATE maintenance_requests
                SET room_id = ?, title = ?, description = ?,
                    priority = ?::maintenance_priority, status = ?::maintenance_status, notes = ?
                WHERE id = ?
                """;
        try (Connection conn = GestorBD.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            if (mr.getHabitacion() != null) ps.setInt(1, mr.getHabitacion().getId());
            else                            ps.setNull(1, Types.INTEGER);
            ps.setString(2, mr.getTitulo());
            ps.setString(3, mr.getDescripcion());
            ps.setString(4, mr.getPrioridad().valorBD);
            ps.setString(5, mr.getEstado().valorBD);
            ps.setString(6, mr.getNotas());
            ps.setInt(7, mr.getId());
            ps.executeUpdate();
        }
    }

    public static void actualizarEstado(int id, SolicitudMantenimiento.Estado estado) throws SQLException {
        String sql = "UPDATE maintenance_requests SET status = ?::maintenance_status WHERE id = ?";
        try (Connection conn = GestorBD.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, estado.valorBD);
            ps.setInt(2, id);
            ps.executeUpdate();
        }
    }

    // ── Mapper ──────────────────────────────────────────────────────────────

    private static List<SolicitudMantenimiento> mapearLista(ResultSet rs) throws SQLException {
        List<SolicitudMantenimiento> list = new ArrayList<>();
        while (rs.next()) list.add(mapear(rs));
        return list;
    }

    private static SolicitudMantenimiento mapear(ResultSet rs) throws SQLException {
        SolicitudMantenimiento mr = new SolicitudMantenimiento();
        mr.setId(rs.getInt("id"));
        mr.setTitulo(rs.getString("title"));
        mr.setDescripcion(rs.getString("description"));
        mr.setPrioridad(SolicitudMantenimiento.Prioridad.desdeBD(rs.getString("priority")));
        mr.setEstado(SolicitudMantenimiento.Estado.desdeBD(rs.getString("status")));
        mr.setNotas(rs.getString("notes"));

        Timestamp reportedAt = rs.getTimestamp("reported_at");
        if (reportedAt != null) mr.setFechaReporte(reportedAt.toLocalDateTime());
        Timestamp startedAt = rs.getTimestamp("started_at");
        if (startedAt != null) mr.setFechaInicio(startedAt.toLocalDateTime());
        Timestamp completedAt = rs.getTimestamp("completed_at");
        if (completedAt != null) mr.setFechaCompletado(completedAt.toLocalDateTime());

        int roomId = rs.getInt("room_id");
        if (!rs.wasNull()) {
            Habitacion r = new Habitacion();
            r.setId(roomId);
            r.setNumero(rs.getString("room_number"));
            r.setPlanta(rs.getInt("floor"));
            r.setEstado(Habitacion.Estado.desdeBD(rs.getString("room_status")));

            int rtId = rs.getInt("rt_id");
            if (!rs.wasNull()) {
                TipoHabitacion rt = new TipoHabitacion();
                rt.setId(rtId);
                rt.setCodigo(rs.getString("rt_code"));
                rt.setNombre(rs.getString("rt_name"));
                rt.setCapacidadMaxima(rs.getInt("max_occupancy"));
                rt.setPrecioBase(rs.getBigDecimal("base_price"));
                r.setTipoHabitacion(rt);
            }
            mr.setHabitacion(r);
        }
        return mr;
    }
}

package es.simonsg.pmsuite.dao;

import es.simonsg.pmsuite.db.GestorBD;
import es.simonsg.pmsuite.model.Habitacion;
import es.simonsg.pmsuite.model.TipoHabitacion;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.sql.Types;

public class HabitacionDAO {

    private static final String BASE_SQL = """
            SELECT r.*, rt.id as rt_id, rt.code, rt.name as rt_name,
                   rt.max_occupancy, rt.base_price
            FROM rooms r
            LEFT JOIN room_types rt ON rt.id = r.room_type_id
            """;

    public static List<Habitacion> getDisponibles() throws SQLException {
        String sql = BASE_SQL + "WHERE r.status = 'AVAILABLE' ORDER BY r.number";
        try (Connection conn = GestorBD.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            return mapearLista(ps.executeQuery());
        }
    }

    public static List<Habitacion> obtenerTodas() throws SQLException {
        String sql = BASE_SQL + "ORDER BY r.number";
        try (Connection conn = GestorBD.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            return mapearLista(ps.executeQuery());
        }
    }

    public static Habitacion buscarPorId(int id) throws SQLException {
        String sql = BASE_SQL + "WHERE r.id = ?";
        try (Connection conn = GestorBD.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
            return rs.next() ? mapear(rs) : null;
        }
    }

    public static Habitacion buscarPorNumero(String numero) throws SQLException {
        String sql = BASE_SQL + "WHERE r.number = ?";
        try (Connection conn = GestorBD.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, numero);
            ResultSet rs = ps.executeQuery();
            return rs.next() ? mapear(rs) : null;
        }
    }

    public static void actualizarEstado(int habitacionId, Habitacion.Estado estado) throws SQLException {
        String sql = "UPDATE rooms SET status = ?::room_status WHERE id = ?";
        try (Connection conn = GestorBD.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, estado.valorBD);
            ps.setInt(2, habitacionId);
            ps.executeUpdate();
        }
    }

    public static int crear(Habitacion hab) throws SQLException {
        String sql = """
                INSERT INTO rooms (number, floor, room_type_id, status, notes)
                VALUES (?, ?, ?, ?::room_status, ?) RETURNING id
                """;
        try (Connection conn = GestorBD.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, hab.getNumero());
            ps.setInt(2, hab.getPlanta());
            if (hab.getTipoHabitacion() != null) ps.setInt(3, hab.getTipoHabitacion().getId());
            else ps.setNull(3, Types.INTEGER);
            ps.setString(4, hab.getEstado() != null ? hab.getEstado().valorBD : Habitacion.Estado.DISPONIBLE.valorBD);
            ps.setString(5, hab.getNotas());
            ResultSet rs = ps.executeQuery();
            rs.next();
            return rs.getInt(1);
        }
    }

    public static void actualizar(Habitacion hab) throws SQLException {
        String sql = """
                UPDATE rooms SET number=?, floor=?, room_type_id=?, status=?::room_status, notes=?
                WHERE id=?
                """;
        try (Connection conn = GestorBD.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, hab.getNumero());
            ps.setInt(2, hab.getPlanta());
            if (hab.getTipoHabitacion() != null) ps.setInt(3, hab.getTipoHabitacion().getId());
            else ps.setNull(3, Types.INTEGER);
            ps.setString(4, hab.getEstado() != null ? hab.getEstado().valorBD : Habitacion.Estado.DISPONIBLE.valorBD);
            ps.setString(5, hab.getNotas());
            ps.setInt(6, hab.getId());
            ps.executeUpdate();
        }
    }

    public static void eliminar(int id) throws SQLException {
        String sql = "DELETE FROM rooms WHERE id=?";
        try (Connection conn = GestorBD.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        }
    }

    private static List<Habitacion> mapearLista(ResultSet rs) throws SQLException {
        List<Habitacion> list = new ArrayList<>();
        while (rs.next()) list.add(mapear(rs));
        return list;
    }

    public static Habitacion mapear(ResultSet rs) throws SQLException {
        Habitacion r = new Habitacion();
        r.setId(rs.getInt("id"));
        r.setNumero(rs.getString("number"));
        r.setPlanta(rs.getInt("floor"));
        r.setEstado(Habitacion.Estado.desdeBD(rs.getString("status")));
        r.setNotas(rs.getString("notes"));

        int rtId = rs.getInt("rt_id");
        if (!rs.wasNull()) {
            TipoHabitacion rt = new TipoHabitacion();
            rt.setId(rtId);
            rt.setCodigo(rs.getString("code"));
            rt.setNombre(rs.getString("rt_name"));
            rt.setCapacidadMaxima(rs.getInt("max_occupancy"));
            rt.setPrecioBase(rs.getBigDecimal("base_price"));
            r.setTipoHabitacion(rt);
        }
        return r;
    }
}

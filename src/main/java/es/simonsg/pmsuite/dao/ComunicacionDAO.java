package es.simonsg.pmsuite.dao;

import es.simonsg.pmsuite.db.GestorBD;
import es.simonsg.pmsuite.model.Comunicacion;
import es.simonsg.pmsuite.model.Comunicacion.Canal;
import es.simonsg.pmsuite.model.Comunicacion.Sentido;
import es.simonsg.pmsuite.model.Huesped;
import es.simonsg.pmsuite.model.Reserva;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ComunicacionDAO {

    private static final String BASE_SQL = """
            SELECT c.id, c.subject, c.body, c.channel, c.direction,
                   c.sent_at, c.read,
                   g.id as g_id, g.first_name, g.last_name, g.email,
                   r.id as r_id, r.reservation_number
            FROM communications c
            LEFT JOIN guests g ON g.id = c.guest_id
            LEFT JOIN reservations r ON r.id = c.reservation_id
            """;

    public static List<Comunicacion> obtenerTodas() throws SQLException {
        String sql = BASE_SQL + "ORDER BY c.sent_at DESC LIMIT 200";
        try (Connection conn = GestorBD.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            return mapearLista(ps.executeQuery());
        }
    }

    public static List<Comunicacion> getByDirection(Sentido sentido) throws SQLException {
        String sql = BASE_SQL + "WHERE c.direction = ? ORDER BY c.sent_at DESC LIMIT 200";
        try (Connection conn = GestorBD.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, sentido.valorBD);
            return mapearLista(ps.executeQuery());
        }
    }

    public static List<Comunicacion> getByChannel(Canal canal) throws SQLException {
        String sql = BASE_SQL + "WHERE c.channel = ? ORDER BY c.sent_at DESC LIMIT 200";
        try (Connection conn = GestorBD.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, canal.valorBD);
            return mapearLista(ps.executeQuery());
        }
    }

    public static List<Comunicacion> getInternal() throws SQLException {
        return getByChannel(Canal.INTERNO);
    }

    public static void marcarLeida(int id) throws SQLException {
        String sql = "UPDATE communications SET read = TRUE WHERE id = ?";
        try (Connection conn = GestorBD.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        }
    }

    public static int crear(Comunicacion c) throws SQLException {
        String sql = """
                INSERT INTO communications (guest_id, reservation_id, subject, body, channel, direction, read)
                VALUES (?, ?, ?, ?, ?, ?, FALSE)
                RETURNING id
                """;
        try (Connection conn = GestorBD.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            if (c.getHuesped() != null) ps.setInt(1, c.getHuesped().getId());
            else ps.setNull(1, Types.INTEGER);
            if (c.getReserva() != null) ps.setInt(2, c.getReserva().getId());
            else ps.setNull(2, Types.INTEGER);
            ps.setString(3, c.getAsunto());
            ps.setString(4, c.getCuerpo());
            ps.setString(5, c.getCanal().valorBD);
            ps.setString(6, c.getSentido().valorBD);
            ResultSet rs = ps.executeQuery();
            rs.next();
            return rs.getInt(1);
        }
    }

    private static List<Comunicacion> mapearLista(ResultSet rs) throws SQLException {
        List<Comunicacion> list = new ArrayList<>();
        while (rs.next()) list.add(mapear(rs));
        return list;
    }

    private static Comunicacion mapear(ResultSet rs) throws SQLException {
        Comunicacion c = new Comunicacion();
        c.setId(rs.getInt("id"));
        c.setAsunto(rs.getString("subject"));
        c.setCuerpo(rs.getString("body"));
        String ch = rs.getString("channel");
        if (ch != null) {
            try { c.setCanal(Canal.desdeBD(ch)); }
            catch (IllegalArgumentException ignored) {}
        }
        String dir = rs.getString("direction");
        if (dir != null) {
            try { c.setSentido(Sentido.desdeBD(dir)); }
            catch (IllegalArgumentException ignored) {}
        }
        Timestamp sent = rs.getTimestamp("sent_at");
        if (sent != null) c.setEnviadoEn(sent.toLocalDateTime());
        c.setLeido(rs.getBoolean("read"));

        int gId = rs.getInt("g_id");
        if (!rs.wasNull()) {
            Huesped g = new Huesped();
            g.setId(gId);
            g.setNombre(rs.getString("first_name"));
            g.setApellidos(rs.getString("last_name"));
            g.setEmail(rs.getString("email"));
            c.setHuesped(g);
        }

        int rId = rs.getInt("r_id");
        if (!rs.wasNull()) {
            Reserva r = new Reserva();
            r.setId(rId);
            r.setNumeroReserva(rs.getString("reservation_number"));
            c.setReserva(r);
        }

        return c;
    }
}

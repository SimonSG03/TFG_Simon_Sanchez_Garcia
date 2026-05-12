package es.simonsg.pmsuite.dao;

import es.simonsg.pmsuite.db.GestorBD;
import es.simonsg.pmsuite.model.Huesped;
import es.simonsg.pmsuite.model.Habitacion;
import es.simonsg.pmsuite.model.Reserva;

import java.math.BigDecimal;
import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class ReservaDAO {

    private static final String BASE_SQL = """
            SELECT r.id, r.reservation_number, r.check_in_date, r.check_out_date,
                   r.adults, r.children, r.status, r.total_price, r.deposit_paid,
                   r.special_requests, r.notes, r.created_at, r.updated_at,
                   g.id as g_id, g.nif, g.first_name, g.last_name, g.email, g.phone,
                   rm.id as rm_id, rm.number as rm_number, rm.floor as rm_floor,
                   rm.status as rm_status
            FROM reservations r
            JOIN guests g ON g.id = r.guest_id
            LEFT JOIN rooms rm ON rm.id = r.room_id
            """;

    public static List<Reserva> getLlegadasHoy() throws SQLException {
        String sql = BASE_SQL + """
                WHERE r.check_in_date = CURRENT_DATE
                  AND r.status IN ('CONFIRMED', 'PENDING')
                ORDER BY r.reservation_number
                """;
        try (Connection conn = GestorBD.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            return mapearLista(ps.executeQuery());
        }
    }

    public static List<Reserva> getSalidasHoy() throws SQLException {
        String sql = BASE_SQL + """
                WHERE r.check_out_date <= CURRENT_DATE
                  AND r.status IN ('CHECKED_IN', 'CONFIRMED', 'PENDING')
                ORDER BY r.check_out_date, r.reservation_number
                """;
        try (Connection conn = GestorBD.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            return mapearLista(ps.executeQuery());
        }
    }

    /** Busca reservas pendientes de checkout (CHECKED_IN o con salida hoy/pasada). */
    public static List<Reserva> buscarRegistrados(String query) throws SQLException {
        String sql = BASE_SQL + """
                WHERE r.status IN ('CHECKED_IN', 'CONFIRMED', 'PENDING')
                  AND r.check_out_date <= CURRENT_DATE
                  AND (LOWER(r.reservation_number) LIKE LOWER(?)
                   OR  LOWER(g.first_name || ' ' || g.last_name) LIKE LOWER(?)
                   OR  LOWER(COALESCE(g.nif,'')) LIKE LOWER(?))
                ORDER BY r.check_out_date, r.reservation_number
                LIMIT 50
                """;
        try (Connection conn = GestorBD.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            String param = "%" + query + "%";
            ps.setString(1, param);
            ps.setString(2, param);
            ps.setString(3, param);
            return mapearLista(ps.executeQuery());
        }
    }

    public static List<Reserva> buscar(String query) throws SQLException {
        String sql = BASE_SQL + """
                WHERE LOWER(r.reservation_number) LIKE LOWER(?)
                   OR LOWER(g.first_name || ' ' || g.last_name) LIKE LOWER(?)
                   OR LOWER(COALESCE(g.nif,'')) LIKE LOWER(?)
                   OR rm.number LIKE ?
                ORDER BY r.created_at DESC
                LIMIT 10
                """;
        try (Connection conn = GestorBD.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            String param = "%" + query + "%";
            ps.setString(1, param);
            ps.setString(2, param);
            ps.setString(3, param);
            ps.setString(4, param);
            return mapearLista(ps.executeQuery());
        }
    }

    public static List<Reserva> getSinHabitacion() throws SQLException {
        String sql = BASE_SQL + """
                WHERE r.room_id IS NULL
                  AND r.status IN ('PENDING', 'CONFIRMED')
                ORDER BY r.check_in_date, r.reservation_number
                """;
        try (Connection conn = GestorBD.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            return mapearLista(ps.executeQuery());
        }
    }

    public static List<Reserva> obtenerActivas() throws SQLException {
        String sql = BASE_SQL + """
                WHERE r.status IN ('PENDING', 'CONFIRMED', 'CHECKED_IN')
                ORDER BY r.check_in_date DESC
                LIMIT 100
                """;
        try (Connection conn = GestorBD.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            return mapearLista(ps.executeQuery());
        }
    }

    public static Reserva getRegistradaPorHabitacion(int habitacionId) throws SQLException {
        String sql = BASE_SQL + """
                WHERE r.room_id = ? AND r.status IN ('CHECKED_IN', 'CONFIRMED', 'PENDING')
                ORDER BY r.check_in_date DESC LIMIT 1
                """;
        try (Connection conn = GestorBD.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, habitacionId);
            ResultSet rs = ps.executeQuery();
            return rs.next() ? mapear(rs) : null;
        }
    }

    public static List<Reserva> obtenerPorHuesped(int huespedId) throws SQLException {
        String sql = BASE_SQL + "WHERE r.guest_id = ? ORDER BY r.created_at DESC";
        try (Connection conn = GestorBD.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, huespedId);
            return mapearLista(ps.executeQuery());
        }
    }

    public static Reserva buscarPorId(int id) throws SQLException {
        String sql = BASE_SQL + "WHERE r.id = ?";
        try (Connection conn = GestorBD.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
            return rs.next() ? mapear(rs) : null;
        }
    }

    public static Reserva buscarPorNumero(String numero) throws SQLException {
        String sql = BASE_SQL + "WHERE r.reservation_number = ?";
        try (Connection conn = GestorBD.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, numero);
            ResultSet rs = ps.executeQuery();
            return rs.next() ? mapear(rs) : null;
        }
    }

    public static int crear(Reserva res) throws SQLException {
        String sql = """
                INSERT INTO reservations
                  (room_id, guest_id, check_in_date, check_out_date, adults, children,
                   status, total_price, deposit_paid, special_requests, notes,
                   reservation_number)
                VALUES (?,?,?,?,?,?,?::reservation_status,?,?,?,?,'')
                RETURNING id
                """;
        try (Connection conn = GestorBD.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            if (res.getHabitacion() != null) ps.setInt(1, res.getHabitacion().getId());
            else ps.setNull(1, Types.INTEGER);
            ps.setInt(2, res.getHuesped().getId());
            ps.setDate(3, Date.valueOf(res.getFechaEntrada()));
            ps.setDate(4, Date.valueOf(res.getFechaSalida()));
            ps.setInt(5, res.getAdultos());
            ps.setInt(6, res.getNinos());
            ps.setString(7, res.getEstado() != null ? res.getEstado().valorBD : Reserva.Estado.PENDIENTE.valorBD);
            ps.setBigDecimal(8, res.getPrecioTotal());
            ps.setBigDecimal(9, res.getDepositoPagado() != null ? res.getDepositoPagado() : BigDecimal.ZERO);
            ps.setString(10, res.getPeticionesEspeciales());
            ps.setString(11, res.getNotas());
            ResultSet rs = ps.executeQuery();
            rs.next();
            return rs.getInt(1);
        }
    }

    public static void actualizar(Reserva res) throws SQLException {
        String sql = """
                UPDATE reservations SET
                  room_id=?, check_in_date=?, check_out_date=?, adults=?, children=?,
                  status=?::reservation_status, total_price=?, special_requests=?, notes=?
                WHERE id=?
                """;
        try (Connection conn = GestorBD.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            if (res.getHabitacion() != null) ps.setInt(1, res.getHabitacion().getId());
            else ps.setNull(1, Types.INTEGER);
            ps.setDate(2, Date.valueOf(res.getFechaEntrada()));
            ps.setDate(3, Date.valueOf(res.getFechaSalida()));
            ps.setInt(4, res.getAdultos());
            ps.setInt(5, res.getNinos());
            ps.setString(6, res.getEstado().valorBD);
            ps.setBigDecimal(7, res.getPrecioTotal());
            ps.setString(8, res.getPeticionesEspeciales());
            ps.setString(9, res.getNotas());
            ps.setInt(10, res.getId());
            ps.executeUpdate();
        }
    }

    public static void moverReserva(String numeroReserva, int nuevoNumeroHabitacion,
                                    LocalDate nuevaEntrada, LocalDate nuevaSalida) throws SQLException {
        String sql = """
                UPDATE reservations
                SET room_id        = (SELECT id FROM rooms WHERE number = ?),
                    check_in_date  = ?,
                    check_out_date = ?
                WHERE reservation_number = ?
                """;
        try (Connection conn = GestorBD.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, String.valueOf(nuevoNumeroHabitacion));
            ps.setDate(2, Date.valueOf(nuevaEntrada));
            ps.setDate(3, Date.valueOf(nuevaSalida));
            ps.setString(4, numeroReserva);
            ps.executeUpdate();
        }
    }

    public static void realizarCheckin(int reservaId) throws SQLException {
        try (Connection conn = GestorBD.getInstance().getConnection()) {
            conn.setAutoCommit(false);
            try {
                try (PreparedStatement ps = conn.prepareStatement(
                        "UPDATE reservations SET status='CHECKED_IN'::reservation_status WHERE id=?")) {
                    ps.setInt(1, reservaId);
                    ps.executeUpdate();
                }
                try (PreparedStatement ps = conn.prepareStatement(
                        "INSERT INTO checkins (reservation_id) VALUES (?)")) {
                    ps.setInt(1, reservaId);
                    ps.executeUpdate();
                }
                try (PreparedStatement ps = conn.prepareStatement(
                        "UPDATE rooms SET status='OCCUPIED'::room_status WHERE id = " +
                                "(SELECT room_id FROM reservations WHERE id=?)")) {
                    ps.setInt(1, reservaId);
                    ps.executeUpdate();
                }
                conn.commit();
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            } finally {
                conn.setAutoCommit(true);
            }
        }
    }

    public static void realizarCheckout(int reservaId, BigDecimal importeFinal) throws SQLException {
        try (Connection conn = GestorBD.getInstance().getConnection()) {
            conn.setAutoCommit(false);
            try {
                try (PreparedStatement ps = conn.prepareStatement(
                        "UPDATE reservations SET status='CHECKED_OUT'::reservation_status WHERE id=?")) {
                    ps.setInt(1, reservaId);
                    ps.executeUpdate();
                }
                try (PreparedStatement ps = conn.prepareStatement(
                        "INSERT INTO checkouts (reservation_id, final_amount) VALUES (?,?)")) {
                    ps.setInt(1, reservaId);
                    ps.setBigDecimal(2, importeFinal);
                    ps.executeUpdate();
                }
                try (PreparedStatement ps = conn.prepareStatement(
                        "UPDATE rooms SET status='AVAILABLE'::room_status WHERE id = " +
                                "(SELECT room_id FROM reservations WHERE id=?)")) {
                    ps.setInt(1, reservaId);
                    ps.executeUpdate();
                }
                conn.commit();
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            } finally {
                conn.setAutoCommit(true);
            }
        }
    }

    /** Reservas cuya estancia se solapa con [desde, hasta] inclusive, excluye canceladas/no-show. */
    public static List<Reserva> obtenerPorRango(LocalDate desde, LocalDate hasta) throws SQLException {
        String sql = BASE_SQL +
                "WHERE r.check_in_date < ? AND r.check_out_date > ? " +
                "AND r.status NOT IN ('CANCELLED', 'NO_SHOW') " +
                "ORDER BY r.check_in_date";
        try (Connection conn = GestorBD.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setDate(1, Date.valueOf(hasta.plusDays(1)));
            ps.setDate(2, Date.valueOf(desde));
            return mapearLista(ps.executeQuery());
        }
    }

    public static void asignarHabitacion(int reservaId, int habitacionId) throws SQLException {
        String sql = "UPDATE reservations SET room_id=? WHERE id=?";
        try (Connection conn = GestorBD.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, habitacionId);
            ps.setInt(2, reservaId);
            ps.executeUpdate();
        }
    }

    private static List<Reserva> mapearLista(ResultSet rs) throws SQLException {
        List<Reserva> list = new ArrayList<>();
        while (rs.next()) list.add(mapear(rs));
        return list;
    }

    private static Reserva mapear(ResultSet rs) throws SQLException {
        Reserva r = new Reserva();
        r.setId(rs.getInt("id"));
        r.setNumeroReserva(rs.getString("reservation_number"));
        r.setFechaEntrada(rs.getDate("check_in_date").toLocalDate());
        r.setFechaSalida(rs.getDate("check_out_date").toLocalDate());
        r.setAdultos(rs.getInt("adults"));
        r.setNinos(rs.getInt("children"));
        r.setEstado(Reserva.Estado.desdeBD(rs.getString("status")));
        r.setPrecioTotal(rs.getBigDecimal("total_price"));
        r.setDepositoPagado(rs.getBigDecimal("deposit_paid"));
        r.setPeticionesEspeciales(rs.getString("special_requests"));
        r.setNotas(rs.getString("notes"));
        Timestamp cat = rs.getTimestamp("created_at");
        if (cat != null) r.setFechaCreacion(cat.toLocalDateTime());
        Timestamp uat = rs.getTimestamp("updated_at");
        if (uat != null) r.setFechaActualizacion(uat.toLocalDateTime());

        Huesped g = new Huesped();
        g.setId(rs.getInt("g_id"));
        g.setNif(rs.getString("nif"));
        g.setNombre(rs.getString("first_name"));
        g.setApellidos(rs.getString("last_name"));
        g.setEmail(rs.getString("email"));
        g.setTelefono(rs.getString("phone"));
        r.setHuesped(g);

        int rmId = rs.getInt("rm_id");
        if (!rs.wasNull()) {
            Habitacion rm = new Habitacion();
            rm.setId(rmId);
            rm.setNumero(rs.getString("rm_number"));
            rm.setPlanta(rs.getInt("rm_floor"));
            String rmStatus = rs.getString("rm_status");
            if (rmStatus != null) rm.setEstado(Habitacion.Estado.desdeBD(rmStatus));
            r.setHabitacion(rm);
        }
        return r;
    }
}


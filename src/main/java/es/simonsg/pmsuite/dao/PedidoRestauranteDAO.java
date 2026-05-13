package es.simonsg.pmsuite.dao;

import es.simonsg.pmsuite.db.GestorBD;
import es.simonsg.pmsuite.model.*;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class PedidoRestauranteDAO {

    private static final String BASE_SQL = """
            SELECT ro.id, ro.table_number, ro.status, ro.notes, ro.created_at, ro.delivered_at,
                   ro.reservation_id, ro.room_id, ro.payment_method,
                   u.id as uid, u.full_name as uname,
                   COALESCE((SELECT SUM(ol.quantity * ol.unit_price)
                              FROM order_lines ol WHERE ol.order_id = ro.id), 0) AS computed_total
            FROM restaurant_orders ro
            LEFT JOIN users u ON u.id = ro.created_by
            """;

    public static List<PedidoRestaurante> getAbiertos() throws SQLException {
        String sql = BASE_SQL + "WHERE ro.status IN ('PENDING','PREPARING') ORDER BY ro.created_at";
        try (Connection conn = GestorBD.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            return mapearLista(ps.executeQuery());
        }
    }

    public static List<PedidoRestaurante> getPorFecha(LocalDate fecha) throws SQLException {
        String sql = BASE_SQL + "WHERE ro.created_at::date = ? ORDER BY ro.created_at DESC";
        try (Connection conn = GestorBD.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setDate(1, Date.valueOf(fecha));
            return mapearLista(ps.executeQuery());
        }
    }

    public static PedidoRestaurante getPorMesa(String numeroMesa) throws SQLException {
        String sql = BASE_SQL + "WHERE ro.table_number = ? AND ro.status IN ('PENDING','PREPARING') ORDER BY ro.created_at DESC LIMIT 1";
        try (Connection conn = GestorBD.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, numeroMesa);
            ResultSet rs = ps.executeQuery();
            return rs.next() ? mapear(rs) : null;
        }
    }

    public static PedidoRestaurante buscarPorId(int id) throws SQLException {
        String sql = BASE_SQL + "WHERE ro.id = ?";
        try (Connection conn = GestorBD.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
            if (!rs.next()) return null;
            PedidoRestaurante pedido = mapear(rs);
            pedido.setLineas(getLineas(id));
            return pedido;
        }
    }

    public static int crear(PedidoRestaurante pedido) throws SQLException {
        String sql = """
                INSERT INTO restaurant_orders (reservation_id, room_id, table_number, status, notes, created_by)
                VALUES (?,?,?,?::order_status,?,?) RETURNING id
                """;
        try (Connection conn = GestorBD.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            if (pedido.getReserva() != null) ps.setInt(1, pedido.getReserva().getId());
            else ps.setNull(1, Types.INTEGER);
            if (pedido.getHabitacion() != null) ps.setInt(2, pedido.getHabitacion().getId());
            else ps.setNull(2, Types.INTEGER);
            ps.setString(3, pedido.getNumeroMesa());
            ps.setString(4, pedido.getEstado() != null ? pedido.getEstado().valorBD : PedidoRestaurante.Estado.PENDIENTE.valorBD);
            ps.setString(5, pedido.getNotas());
            if (pedido.getCreadoPor() != null) ps.setInt(6, pedido.getCreadoPor().getId());
            else ps.setNull(6, Types.INTEGER);
            ResultSet rs = ps.executeQuery();
            rs.next();
            return rs.getInt(1);
        }
    }

    public static void actualizarEstado(int id, PedidoRestaurante.Estado estado) throws SQLException {
        String sql = "UPDATE restaurant_orders SET status=?::order_status WHERE id=?";
        try (Connection conn = GestorBD.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, estado.valorBD);
            ps.setInt(2, id);
            ps.executeUpdate();
        }
    }

    public static void entregar(int id, String metodoPago) throws SQLException {
        String sql = "UPDATE restaurant_orders SET status='DELIVERED'::order_status, delivered_at=NOW(), payment_method=? WHERE id=?";
        try (Connection conn = GestorBD.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, metodoPago);
            ps.setInt(2, id);
            ps.executeUpdate();
        }
    }

    public static void entregar(int id) throws SQLException {
        entregar(id, null);
    }

    public static void vincularReserva(int pedidoId, int reservaId) throws SQLException {
        String sql = "UPDATE restaurant_orders SET reservation_id=? WHERE id=?";
        try (Connection conn = GestorBD.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, reservaId);
            ps.setInt(2, pedidoId);
            ps.executeUpdate();
        }
    }

    public static void vincularHabitacion(int pedidoId, int habitacionId) throws SQLException {
        String sql = "UPDATE restaurant_orders SET room_id=? WHERE id=?";
        try (Connection conn = GestorBD.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, habitacionId);
            ps.setInt(2, pedidoId);
            ps.executeUpdate();
        }
    }

    public static int añadirLinea(LineaPedido linea) throws SQLException {
        String sql = """
                INSERT INTO order_lines (order_id, menu_item_id, quantity, unit_price, notes)
                VALUES (?,?,?,?,?) RETURNING id
                """;
        try (Connection conn = GestorBD.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, linea.getIdPedido());
            if (linea.getArticulo() != null) ps.setInt(2, linea.getArticulo().getId());
            else ps.setNull(2, Types.INTEGER);
            ps.setInt(3, linea.getCantidad());
            ps.setBigDecimal(4, linea.getPrecioUnitario());
            ps.setString(5, linea.getNotas());
            ResultSet rs = ps.executeQuery();
            rs.next();
            return rs.getInt(1);
        }
    }

    public static void eliminarLinea(int lineaId) throws SQLException {
        String sql = "DELETE FROM order_lines WHERE id=?";
        try (Connection conn = GestorBD.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, lineaId);
            ps.executeUpdate();
        }
    }

    public static List<LineaPedido> getLineas(int pedidoId) throws SQLException {
        String sql = """
                SELECT ol.id, ol.order_id, ol.quantity, ol.unit_price, ol.notes,
                       mi.id as mi_id, mi.name as mi_name, mi.price as mi_price
                FROM order_lines ol
                LEFT JOIN menu_items mi ON mi.id = ol.menu_item_id
                WHERE ol.order_id = ?
                ORDER BY ol.id
                """;
        try (Connection conn = GestorBD.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, pedidoId);
            List<LineaPedido> lineas = new ArrayList<>();
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                LineaPedido l = new LineaPedido();
                l.setId(rs.getInt("id"));
                l.setIdPedido(rs.getInt("order_id"));
                l.setCantidad(rs.getInt("quantity"));
                l.setPrecioUnitario(rs.getBigDecimal("unit_price"));
                l.setNotas(rs.getString("notes"));
                int miId = rs.getInt("mi_id");
                if (!rs.wasNull()) {
                    ArticuloMenu mi = new ArticuloMenu();
                    mi.setId(miId);
                    mi.setNombre(rs.getString("mi_name"));
                    mi.setPrecio(rs.getBigDecimal("mi_price"));
                    l.setArticulo(mi);
                }
                lineas.add(l);
            }
            return lineas;
        }
    }

    private static List<PedidoRestaurante> mapearLista(ResultSet rs) throws SQLException {
        List<PedidoRestaurante> list = new ArrayList<>();
        while (rs.next()) list.add(mapear(rs));
        return list;
    }

    private static PedidoRestaurante mapear(ResultSet rs) throws SQLException {
        PedidoRestaurante o = new PedidoRestaurante();
        o.setId(rs.getInt("id"));
        o.setNumeroMesa(rs.getString("table_number"));
        o.setEstado(PedidoRestaurante.Estado.desdeBD(rs.getString("status")));
        o.setNotas(rs.getString("notes"));
        try { o.setMetodoPago(rs.getString("payment_method")); } catch (SQLException ignored) {}
        try { o.setTotalCalculado(rs.getBigDecimal("computed_total")); } catch (SQLException ignored) {}
        Timestamp ca = rs.getTimestamp("created_at");
        if (ca != null) o.setFechaCreacion(ca.toLocalDateTime());
        Timestamp da = rs.getTimestamp("delivered_at");
        if (da != null) o.setFechaEntrega(da.toLocalDateTime());
        int uid = rs.getInt("uid");
        if (!rs.wasNull()) {
            Usuario u = new Usuario();
            u.setId(uid);
            u.setNombreCompleto(rs.getString("uname"));
            o.setCreadoPor(u);
        }
        return o;
    }
}

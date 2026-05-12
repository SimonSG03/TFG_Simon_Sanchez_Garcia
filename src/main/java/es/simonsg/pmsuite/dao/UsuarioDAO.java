package es.simonsg.pmsuite.dao;

import es.simonsg.pmsuite.db.GestorBD;
import es.simonsg.pmsuite.model.Usuario;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class UsuarioDAO {

    private static final String BASE_SQL =
            "SELECT id, username, email, password_hash, full_name, role, active, " +
                    "last_login, created_at FROM users ";

    public static List<Usuario> obtenerTodos() throws SQLException {
        String sql = BASE_SQL + "ORDER BY full_name";
        try (Connection conn = GestorBD.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            return mapearLista(ps.executeQuery());
        }
    }

    public static Usuario buscarPorId(int id) throws SQLException {
        String sql = BASE_SQL + "WHERE id = ?";
        try (Connection conn = GestorBD.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
            return rs.next() ? mapear(rs) : null;
        }
    }

    public static Usuario buscarPorNombreUsuario(String nombreUsuario) throws SQLException {
        String sql = BASE_SQL + "WHERE username = ?";
        try (Connection conn = GestorBD.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, nombreUsuario);
            ResultSet rs = ps.executeQuery();
            return rs.next() ? mapear(rs) : null;
        }
    }

    /** Crea un usuario. La contraseña se almacena tal cual (sin hash en esta versión). */
    public static int crear(Usuario user) throws SQLException {
        String sql = """
                INSERT INTO users (username, email, password_hash, full_name, role, active)
                VALUES (?, ?, ?, ?, ?::user_role, ?) RETURNING id
                """;
        try (Connection conn = GestorBD.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, user.getNombreUsuario());
            ps.setString(2, user.getEmail());
            ps.setString(3, user.getHashContrasena() != null ? user.getHashContrasena() : "");
            ps.setString(4, user.getNombreCompleto());
            ps.setString(5, user.getRol() != null ? user.getRol().valorBD : Usuario.Rol.RECEPCIONISTA.valorBD);
            ps.setBoolean(6, user.isActivo());
            ResultSet rs = ps.executeQuery();
            rs.next();
            return rs.getInt(1);
        }
    }

    /** Actualiza todos los campos excepto la contraseña. */
    public static void actualizar(Usuario user) throws SQLException {
        String sql = """
                UPDATE users SET username=?, email=?, full_name=?, role=?::user_role, active=?
                WHERE id=?
                """;
        try (Connection conn = GestorBD.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, user.getNombreUsuario());
            ps.setString(2, user.getEmail());
            ps.setString(3, user.getNombreCompleto());
            ps.setString(4, user.getRol() != null ? user.getRol().valorBD : Usuario.Rol.RECEPCIONISTA.valorBD);
            ps.setBoolean(5, user.isActivo());
            ps.setInt(6, user.getId());
            ps.executeUpdate();
        }
    }

    /** Cambia la contraseña de un usuario. */
    public static void actualizarContrasena(int userId, String nuevoHashContrasena) throws SQLException {
        String sql = "UPDATE users SET password_hash=? WHERE id=?";
        try (Connection conn = GestorBD.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, nuevoHashContrasena);
            ps.setInt(2, userId);
            ps.executeUpdate();
        }
    }

    public static void eliminar(int id) throws SQLException {
        String sql = "DELETE FROM users WHERE id=?";
        try (Connection conn = GestorBD.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        }
    }

    private static List<Usuario> mapearLista(ResultSet rs) throws SQLException {
        List<Usuario> list = new ArrayList<>();
        while (rs.next()) list.add(mapear(rs));
        return list;
    }

    public static Usuario mapear(ResultSet rs) throws SQLException {
        Usuario u = new Usuario();
        u.setId(rs.getInt("id"));
        u.setNombreUsuario(rs.getString("username"));
        u.setEmail(rs.getString("email"));
        u.setHashContrasena(rs.getString("password_hash"));
        u.setNombreCompleto(rs.getString("full_name"));
        u.setRol(Usuario.Rol.desdeBD(rs.getString("role")));
        u.setActivo(rs.getBoolean("active"));
        Timestamp lastLogin = rs.getTimestamp("last_login");
        if (lastLogin != null) u.setUltimoAcceso(lastLogin.toLocalDateTime());
        Timestamp createdAt = rs.getTimestamp("created_at");
        if (createdAt != null) u.setFechaCreacion(createdAt.toLocalDateTime());
        return u;
    }
}

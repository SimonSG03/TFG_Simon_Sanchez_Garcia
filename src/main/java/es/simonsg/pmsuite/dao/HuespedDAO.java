package es.simonsg.pmsuite.dao;

import es.simonsg.pmsuite.db.GestorBD;
import es.simonsg.pmsuite.model.Huesped;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class HuespedDAO {

    public static List<Huesped> buscar(String query) throws SQLException {
        String sql = """
                SELECT * FROM guests
                WHERE LOWER(first_name || ' ' || last_name) LIKE LOWER(?)
                   OR LOWER(COALESCE(nif,'')) LIKE LOWER(?)
                   OR LOWER(COALESCE(email,'')) LIKE LOWER(?)
                ORDER BY last_name, first_name
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

    public static Huesped buscarPorId(int id) throws SQLException {
        String sql = "SELECT * FROM guests WHERE id = ?";
        try (Connection conn = GestorBD.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
            return rs.next() ? mapear(rs) : null;
        }
    }

    public static int guardar(Huesped g) throws SQLException {
        if (g.getId() > 0) {
            actualizar(g);
            return g.getId();
        }
        String sql = """
                INSERT INTO guests
                  (nif, first_name, last_name, email, phone, nationality, country,
                   address, city, postal_code, birth_date, notes)
                VALUES (?,?,?,?,?,?,?,?,?,?,?,?)
                RETURNING id
                """;
        try (Connection conn = GestorBD.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, g.getNif());
            ps.setString(2, g.getNombre());
            ps.setString(3, g.getApellidos());
            ps.setString(4, g.getEmail());
            ps.setString(5, g.getTelefono());
            ps.setString(6, g.getNacionalidad());
            ps.setString(7, g.getPais());
            ps.setString(8, g.getDomicilio());
            ps.setString(9, g.getCiudad());
            ps.setString(10, g.getCodigoPostal());
            ps.setObject(11, g.getFechaNacimiento());
            ps.setString(12, g.getNotas());
            ResultSet rs = ps.executeQuery();
            rs.next();
            return rs.getInt(1);
        }
    }

    public static void actualizar(Huesped g) throws SQLException {
        String sql = """
                UPDATE guests SET
                  nif=?, first_name=?, last_name=?, email=?, phone=?,
                  nationality=?, country=?, address=?, city=?, postal_code=?,
                  birth_date=?, notes=?
                WHERE id=?
                """;
        try (Connection conn = GestorBD.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, g.getNif());
            ps.setString(2, g.getNombre());
            ps.setString(3, g.getApellidos());
            ps.setString(4, g.getEmail());
            ps.setString(5, g.getTelefono());
            ps.setString(6, g.getNacionalidad());
            ps.setString(7, g.getPais());
            ps.setString(8, g.getDomicilio());
            ps.setString(9, g.getCiudad());
            ps.setString(10, g.getCodigoPostal());
            ps.setObject(11, g.getFechaNacimiento());
            ps.setString(12, g.getNotas());
            ps.setInt(13, g.getId());
            ps.executeUpdate();
        }
    }

    private static List<Huesped> mapearLista(ResultSet rs) throws SQLException {
        List<Huesped> list = new ArrayList<>();
        while (rs.next()) list.add(mapear(rs));
        return list;
    }

    public static Huesped mapear(ResultSet rs) throws SQLException {
        Huesped g = new Huesped();
        g.setId(rs.getInt("id"));
        g.setNif(rs.getString("nif"));
        g.setNombre(rs.getString("first_name"));
        g.setApellidos(rs.getString("last_name"));
        g.setEmail(rs.getString("email"));
        g.setTelefono(rs.getString("phone"));
        g.setNacionalidad(rs.getString("nationality"));
        g.setPais(rs.getString("country"));
        g.setDomicilio(rs.getString("address"));
        g.setCiudad(rs.getString("city"));
        g.setCodigoPostal(rs.getString("postal_code"));
        Date bd = rs.getDate("birth_date");
        if (bd != null) g.setFechaNacimiento(bd.toLocalDate());
        g.setNotas(rs.getString("notes"));
        Timestamp cat = rs.getTimestamp("created_at");
        if (cat != null) g.setFechaCreacion(cat.toLocalDateTime());
        return g;
    }
}

package es.simonsg.pmsuite.dao;

import es.simonsg.pmsuite.db.GestorBD;
import es.simonsg.pmsuite.model.Temporada;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class TemporadaDAO {

    public static List<Temporada> obtenerTodas() throws SQLException {
        String sql = "SELECT * FROM seasons ORDER BY start_date";
        try (Connection conn = GestorBD.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            return mapearLista(ps.executeQuery());
        }
    }

    public static void guardar(Temporada s) throws SQLException {
        String sql = "INSERT INTO seasons (name, start_date, end_date, multiplier) VALUES (?, ?, ?, ?)";
        try (Connection conn = GestorBD.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, s.getNombre());
            ps.setDate(2, Date.valueOf(s.getFechaInicio()));
            ps.setDate(3, Date.valueOf(s.getFechaFin()));
            ps.setBigDecimal(4, s.getMultiplicador());
            ps.executeUpdate();
            ResultSet keys = ps.getGeneratedKeys();
            if (keys.next()) s.setId(keys.getInt(1));
        }
    }

    public static void actualizar(Temporada s) throws SQLException {
        String sql = "UPDATE seasons SET name=?, start_date=?, end_date=?, multiplier=? WHERE id=?";
        try (Connection conn = GestorBD.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, s.getNombre());
            ps.setDate(2, Date.valueOf(s.getFechaInicio()));
            ps.setDate(3, Date.valueOf(s.getFechaFin()));
            ps.setBigDecimal(4, s.getMultiplicador());
            ps.setInt(5, s.getId());
            ps.executeUpdate();
        }
    }

    public static void eliminar(int id) throws SQLException {
        String sql = "DELETE FROM seasons WHERE id=?";
        try (Connection conn = GestorBD.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        }
    }

    private static List<Temporada> mapearLista(ResultSet rs) throws SQLException {
        List<Temporada> list = new ArrayList<>();
        while (rs.next()) list.add(mapear(rs));
        return list;
    }

    private static Temporada mapear(ResultSet rs) throws SQLException {
        Temporada s = new Temporada();
        s.setId(rs.getInt("id"));
        s.setNombre(rs.getString("name"));
        s.setFechaInicio(rs.getDate("start_date").toLocalDate());
        s.setFechaFin(rs.getDate("end_date").toLocalDate());
        s.setMultiplicador(rs.getBigDecimal("multiplier"));
        return s;
    }
}

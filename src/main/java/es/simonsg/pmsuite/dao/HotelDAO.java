package es.simonsg.pmsuite.dao;

import es.simonsg.pmsuite.db.GestorBD;
import es.simonsg.pmsuite.model.Hotel;

import java.sql.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * DAO para la configuración del establecimiento hotelero.
 * Los campos extra (logo_path, postal_code, iban) se añaden automáticamente
 * mediante ALTER TABLE IF NOT EXISTS la primera vez que se accede.
 */
public class HotelDAO {

    private static final Logger LOG = Logger.getLogger(HotelDAO.class.getName());

    /** Asegura que las columnas de configuración existen en la tabla hotel. */
    static {
        try (Connection conn = GestorBD.getInstance().getConnection();
             Statement st = conn.createStatement()) {
            st.execute("ALTER TABLE hotel ADD COLUMN IF NOT EXISTS postal_code VARCHAR(10)");
            st.execute("ALTER TABLE hotel ADD COLUMN IF NOT EXISTS iban        VARCHAR(50)");
            st.execute("ALTER TABLE hotel ADD COLUMN IF NOT EXISTS logo_path   VARCHAR(500)");
        } catch (Exception e) {
            LOG.log(Level.WARNING, "No se pudieron añadir columnas extra a hotel: " + e.getMessage());
        }
    }

    /** Devuelve la configuración del hotel (primera fila de la tabla). */
    public static Hotel get() throws SQLException {
        String sql = """
                SELECT id, name, address, city, postal_code, country,
                       phone, email, website, nif, iban, logo_path
                FROM hotel ORDER BY id LIMIT 1
                """;
        try (Connection conn = GestorBD.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) return mapear(rs);
        }
        // Si no existe fila, devolver un objeto vacío con valores por defecto
        Hotel h = new Hotel();
        h.setNombre("Mi Hotel");
        h.setPais("España");
        return h;
    }

    /** Guarda los datos del hotel (UPDATE si existe, INSERT si no). */
    public static void guardar(Hotel h) throws SQLException {
        if (h.getId() > 0) {
            String sql = """
                    UPDATE hotel SET
                        name        = ?,
                        address     = ?,
                        city        = ?,
                        postal_code = ?,
                        country     = ?,
                        phone       = ?,
                        email       = ?,
                        website     = ?,
                        nif         = ?,
                        iban        = ?,
                        logo_path   = ?
                    WHERE id = ?
                    """;
            try (Connection conn = GestorBD.getInstance().getConnection();
                 PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1,  h.getNombre());
                ps.setString(2,  h.getDomicilio());
                ps.setString(3,  h.getCiudad());
                ps.setString(4,  h.getCodigoPostal());
                ps.setString(5,  h.getPais());
                ps.setString(6,  h.getTelefono());
                ps.setString(7,  h.getEmail());
                ps.setString(8,  h.getSitioWeb());
                ps.setString(9,  h.getNif());
                ps.setString(10, h.getIban());
                ps.setString(11, h.getRutaLogo());
                ps.setInt(12,    h.getId());
                ps.executeUpdate();
            }
        } else {
            String sql = """
                    INSERT INTO hotel (name, address, city, postal_code, country,
                                       phone, email, website, nif, iban, logo_path)
                    VALUES (?,?,?,?,?,?,?,?,?,?,?)
                    """;
            try (Connection conn = GestorBD.getInstance().getConnection();
                 PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1,  h.getNombre());
                ps.setString(2,  h.getDomicilio());
                ps.setString(3,  h.getCiudad());
                ps.setString(4,  h.getCodigoPostal());
                ps.setString(5,  h.getPais());
                ps.setString(6,  h.getTelefono());
                ps.setString(7,  h.getEmail());
                ps.setString(8,  h.getSitioWeb());
                ps.setString(9,  h.getNif());
                ps.setString(10, h.getIban());
                ps.setString(11, h.getRutaLogo());
                ps.executeUpdate();
            }
        }
    }

    private static Hotel mapear(ResultSet rs) throws SQLException {
        Hotel h = new Hotel();
        h.setId(rs.getInt("id"));
        h.setNombre(rs.getString("name"));
        h.setDomicilio(rs.getString("address"));
        h.setCiudad(rs.getString("city"));
        h.setCodigoPostal(rs.getString("postal_code"));
        h.setPais(rs.getString("country"));
        h.setTelefono(rs.getString("phone"));
        h.setEmail(rs.getString("email"));
        h.setSitioWeb(rs.getString("website"));
        h.setNif(rs.getString("nif"));
        h.setIban(rs.getString("iban"));
        h.setRutaLogo(rs.getString("logo_path"));
        return h;
    }
}

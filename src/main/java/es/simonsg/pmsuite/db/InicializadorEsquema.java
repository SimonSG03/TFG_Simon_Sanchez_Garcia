package es.simonsg.pmsuite.db;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 * Inicializa el esquema de la base de datos al arrancar la aplicación.
 * Lee el fichero schema.sql y lo ejecuta contra la BD configurada.
 */
public class InicializadorEsquema {

    private static final Logger LOGGER = Logger.getLogger(InicializadorEsquema.class.getName());

    /**
     * Ejecuta el schema.sql si las tablas no existen todavía.
     * Es idempotente gracias al uso de CREATE TABLE IF NOT EXISTS.
     */
    public static void initialize() {
        LOGGER.info("Iniciando verificación del esquema de base de datos...");
        String sql = loadSchema();
        if (sql == null || sql.isBlank()) {
            LOGGER.severe("No se pudo cargar el fichero schema.sql");
            return;
        }

        try (Connection conn = GestorBD.getInstance().getConnection();
             Statement stmt = conn.createStatement()) {

            // Ejecutamos el schema completo (CREATE TABLE IF NOT EXISTS es idempotente)
            stmt.execute(sql);
            LOGGER.info("Esquema de base de datos verificado correctamente.");

        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error al inicializar el esquema de la base de datos", e);
            throw new RuntimeException("Error al inicializar la base de datos", e);
        }
    }

    private static String loadSchema() {
        try (InputStream is = InicializadorEsquema.class.getModule().getResourceAsStream("db/schema.sql")) {
            if (is == null) {
                LOGGER.severe("No se encontró db/schema.sql en el classpath");
                return null;
            }
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))) {
                return reader.lines().collect(Collectors.joining("\n"));
            }
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Error leyendo db/schema.sql", e);
            return null;
        }
    }
}

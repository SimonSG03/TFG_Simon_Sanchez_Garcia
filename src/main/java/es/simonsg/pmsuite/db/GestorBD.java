package es.simonsg.pmsuite.db;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

public class GestorBD {

    private static final Logger LOGGER = Logger.getLogger(GestorBD.class.getName());
    private static GestorBD instance;
    private HikariDataSource dataSource;

    private GestorBD() {
        initDataSource();
    }

    public static synchronized GestorBD getInstance() {
        if (instance == null) {
            instance = new GestorBD();
        }
        return instance;
    }

    private void initDataSource() {
        try {
            Class.forName("org.postgresql.Driver");
        } catch (ClassNotFoundException e) {
            LOGGER.log(Level.WARNING, "Driver PostgreSQL no encontrado en classpath", e);
        }

        try (InputStream input = getClass().getClassLoader().getResourceAsStream("database.properties")) {
            if (input == null) {
                LOGGER.severe("No se encontró database.properties — BD no disponible");
                return;
            }

            Properties props = new Properties();
            props.load(input);

            String host     = props.getProperty("db.host", "").trim();
            String port     = props.getProperty("db.port", "").trim();
            String name     = props.getProperty("db.name", "").trim();
            String user     = props.getProperty("db.user", "").trim();
            String password = props.getProperty("db.password", "").trim();

            if (host.isEmpty() || port.isEmpty() || name.isEmpty()) {
                LOGGER.severe("database.properties incompleto (host/port/name vacíos) — BD no disponible");
                return;
            }

            HikariConfig config = new HikariConfig();
            config.setJdbcUrl(String.format("jdbc:postgresql://%s:%s/%s", host, port, name));
            config.setUsername(user);
            config.setPassword(password);
            config.setMaximumPoolSize(Integer.parseInt(props.getProperty("db.pool.maximumPoolSize", "10")));
            config.setMinimumIdle(Integer.parseInt(props.getProperty("db.pool.minimumIdle", "2")));
            config.setConnectionTimeout(Long.parseLong(props.getProperty("db.pool.connectionTimeout", "30000")));
            config.setIdleTimeout(Long.parseLong(props.getProperty("db.pool.idleTimeout", "600000")));
            config.setMaxLifetime(Long.parseLong(props.getProperty("db.pool.maxLifetime", "1800000")));
            config.setPoolName("PMSuite-Pool");
            config.addDataSourceProperty("cachePrepStmts", "true");
            config.addDataSourceProperty("prepStmtCacheSize", "250");
            config.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");

            dataSource = new HikariDataSource(config);
            LOGGER.info("Pool de conexiones inicializado correctamente.");

        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Error leyendo database.properties — BD no disponible", e);
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error iniciando pool de conexiones — BD no disponible", e);
        }
    }

    public boolean isAvailable() {
        return dataSource != null && !dataSource.isClosed();
    }

    public Connection getConnection() throws SQLException {
        if (dataSource == null || dataSource.isClosed()) {
            throw new SQLException("Base de datos no disponible. Comprueba database.properties.");
        }
        return dataSource.getConnection();
    }

    public void close() {
        if (dataSource != null && !dataSource.isClosed()) {
            dataSource.close();
            LOGGER.info("Pool de conexiones cerrado.");
        }
    }

    public boolean isConnected() {
        try (Connection conn = getConnection()) {
            return conn != null && !conn.isClosed();
        } catch (SQLException e) {
            LOGGER.log(Level.WARNING, "No se pudo conectar a la base de datos", e);
            return false;
        }
    }
}


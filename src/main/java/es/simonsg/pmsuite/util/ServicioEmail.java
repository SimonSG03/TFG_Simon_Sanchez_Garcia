package es.simonsg.pmsuite.util;

import es.simonsg.pmsuite.db.GestorBD;

import jakarta.mail.*;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeBodyPart;
import jakarta.mail.internet.MimeMessage;
import jakarta.mail.internet.MimeMultipart;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

public class ServicioEmail {

    public record ConfigSmtp(
            String host,
            int port,
            String user,
            String password,
            String from,
            String fromName,
            boolean tls
    ) {
        public boolean isConfigured() {
            return host != null && !host.isBlank()
                    && user != null && !user.isBlank()
                    && password != null && !password.isBlank();
        }
    }

    /** Loads SMTP configuration from app_config table. Returns null on DB error. */
    public static ConfigSmtp loadConfig() {
        try (Connection conn = GestorBD.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(
                     "SELECT key, value FROM app_config WHERE key LIKE 'smtp.%'")) {
            ResultSet rs = ps.executeQuery();
            Map<String, String> cfg = new HashMap<>();
            while (rs.next()) cfg.put(rs.getString("key"), rs.getString("value"));
            return new ConfigSmtp(
                    cfg.getOrDefault("smtp.host", ""),
                    parsePort(cfg.getOrDefault("smtp.port", "587")),
                    cfg.getOrDefault("smtp.user", ""),
                    cfg.getOrDefault("smtp.password", ""),
                    cfg.getOrDefault("smtp.from", ""),
                    cfg.getOrDefault("smtp.from_name", "Hotel"),
                    "true".equalsIgnoreCase(cfg.getOrDefault("smtp.tls", "true"))
            );
        } catch (SQLException e) {
            return new ConfigSmtp("", 587, "", "", "", "Hotel", true);
        }
    }

    /** Saves SMTP configuration to app_config table. */
    public static void saveConfig(ConfigSmtp cfg) throws SQLException {
        String sql = "INSERT INTO app_config (key, value) VALUES (?, ?) " +
                "ON CONFLICT (key) DO UPDATE SET value = EXCLUDED.value";
        try (Connection conn = GestorBD.getInstance().getConnection()) {
            saveKey(conn, sql, "smtp.host",      cfg.host());
            saveKey(conn, sql, "smtp.port",      String.valueOf(cfg.port()));
            saveKey(conn, sql, "smtp.user",      cfg.user());
            saveKey(conn, sql, "smtp.password",  cfg.password());
            saveKey(conn, sql, "smtp.from",      cfg.from());
            saveKey(conn, sql, "smtp.from_name", cfg.fromName());
            saveKey(conn, sql, "smtp.tls",       String.valueOf(cfg.tls()));
        }
    }

    /** Sends an email using the provided SMTP configuration. */
    public static void send(ConfigSmtp cfg, String to, String subject, String body)
            throws MessagingException, java.io.UnsupportedEncodingException {
        Session session = buildSession(cfg);

        Message message = new MimeMessage(session);
        message.setFrom(new InternetAddress(
                cfg.from().isBlank() ? cfg.user() : cfg.from(), cfg.fromName()));
        message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(to));
        message.setSubject(subject);
        message.setText(body);

        Transport.send(message);
    }

    private static Session buildSession(ConfigSmtp cfg) {
        Properties props = new Properties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.host", cfg.host());
        props.put("mail.smtp.port", String.valueOf(cfg.port()));
        if (cfg.tls()) {
            props.put("mail.smtp.starttls.enable", "true");
        } else {
            props.put("mail.smtp.ssl.enable", "true");
        }
        props.put("mail.smtp.connectiontimeout", "10000");
        props.put("mail.smtp.timeout", "10000");
        return Session.getInstance(props, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(cfg.user(), cfg.password());
            }
        });
    }

    /** Sends an email with a file attachment. */
    public static void sendWithAttachment(ConfigSmtp cfg, String to, String subject,
                                          String body, File attachment)
            throws MessagingException, java.io.UnsupportedEncodingException, IOException {
        Session session = buildSession(cfg);

        MimeMessage message = new MimeMessage(session);
        message.setFrom(new InternetAddress(
                cfg.from().isBlank() ? cfg.user() : cfg.from(), cfg.fromName()));
        message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(to));
        message.setSubject(subject);

        MimeBodyPart textPart = new MimeBodyPart();
        textPart.setText(body);

        MimeBodyPart filePart = new MimeBodyPart();
        filePart.attachFile(attachment);

        MimeMultipart multipart = new MimeMultipart();
        multipart.addBodyPart(textPart);
        multipart.addBodyPart(filePart);
        message.setContent(multipart);

        Transport.send(message);
    }

    /** Convenience: loads config and sends with attachment. */
    public static void sendWithAttachmentStoredConfig(String to, String subject,
                                                      String body, File attachment)
            throws MessagingException, java.io.UnsupportedEncodingException,
            IOException, IllegalStateException {
        ConfigSmtp cfg = loadConfig();
        if (!cfg.isConfigured()) {
            throw new IllegalStateException("SMTP no configurado. Configure el servidor en Comunicación > Config SMTP.");
        }
        sendWithAttachment(cfg, to, subject, body, attachment);
    }

    /** Convenience: loads config and sends. Throws if not configured or on SMTP error. */
    public static void sendWithStoredConfig(String to, String subject, String body)
            throws MessagingException, java.io.UnsupportedEncodingException, IllegalStateException {
        ConfigSmtp cfg = loadConfig();
        if (!cfg.isConfigured()) {
            throw new IllegalStateException("SMTP no configurado. Configure el servidor en Comunicación > Config SMTP.");
        }
        send(cfg, to, subject, body);
    }

    private static void saveKey(Connection conn, String sql, String key, String value) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, key);
            ps.setString(2, value);
            ps.executeUpdate();
        }
    }

    private static int parsePort(String s) {
        try { return Integer.parseInt(s.trim()); }
        catch (NumberFormatException e) { return 587; }
    }
}

package es.simonsg.pmsuite.model;

import java.time.LocalDateTime;

public class Comunicacion {

    public enum Canal {
        EMAIL("EMAIL"), SMS("SMS"), WHATSAPP("WHATSAPP"), INTERNO("INTERNAL");

        public final String valorBD;
        Canal(String valorBD) { this.valorBD = valorBD; }

        public static Canal desdeBD(String v) {
            for (Canal e : values()) if (e.valorBD.equals(v)) return e;
            throw new IllegalArgumentException("Canal desconocido: " + v);
        }

        public String toSpanish() {
            return switch (this) {
                case EMAIL    -> "Email";
                case SMS      -> "SMS";
                case WHATSAPP -> "WhatsApp";
                case INTERNO  -> "Interno";
            };
        }
    }

    public enum Sentido {
        ENTRANTE("INBOUND"), SALIENTE("OUTBOUND");

        public final String valorBD;
        Sentido(String valorBD) { this.valorBD = valorBD; }

        public static Sentido desdeBD(String v) {
            for (Sentido e : values()) if (e.valorBD.equals(v)) return e;
            throw new IllegalArgumentException("Sentido desconocido: " + v);
        }

        public String toSpanish() {
            return switch (this) {
                case ENTRANTE -> "Recibido";
                case SALIENTE -> "Enviado";
            };
        }
    }

    private int id;
    private Huesped huesped;
    private Reserva reserva;
    private String asunto;
    private String cuerpo;
    private Canal canal;
    private Sentido sentido;
    private LocalDateTime enviadoEn;
    private Usuario enviadoPor;
    private boolean leido;

    public Comunicacion() {}

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public Huesped getHuesped() { return huesped; }
    public void setHuesped(Huesped huesped) { this.huesped = huesped; }

    public Reserva getReserva() { return reserva; }
    public void setReserva(Reserva reserva) { this.reserva = reserva; }

    public String getAsunto() { return asunto; }
    public void setAsunto(String asunto) { this.asunto = asunto; }

    public String getCuerpo() { return cuerpo; }
    public void setCuerpo(String cuerpo) { this.cuerpo = cuerpo; }

    public Canal getCanal() { return canal; }
    public void setCanal(Canal canal) { this.canal = canal; }

    public Sentido getSentido() { return sentido; }
    public void setSentido(Sentido sentido) { this.sentido = sentido; }

    public LocalDateTime getEnviadoEn() { return enviadoEn; }
    public void setEnviadoEn(LocalDateTime enviadoEn) { this.enviadoEn = enviadoEn; }

    public Usuario getEnviadoPor() { return enviadoPor; }
    public void setEnviadoPor(Usuario enviadoPor) { this.enviadoPor = enviadoPor; }

    public boolean isLeido() { return leido; }
    public void setLeido(boolean leido) { this.leido = leido; }
}

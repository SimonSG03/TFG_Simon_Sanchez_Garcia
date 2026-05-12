package es.simonsg.pmsuite.model;

import java.time.LocalDateTime;

public class Alerta {

    public enum Tipo {
        OVERBOOKING("OVERBOOKING"), MANTENIMIENTO("MAINTENANCE"), FACTURA("INVOICE"),
        LIMPIEZA("CLEANING"), CHECKIN("CHECKIN"), CHECKOUT("CHECKOUT"), GENERAL("GENERAL");

        public final String valorBD;
        Tipo(String valorBD) { this.valorBD = valorBD; }

        public static Tipo desdeBD(String v) {
            for (Tipo e : values()) if (e.valorBD.equals(v)) return e;
            throw new IllegalArgumentException("Tipo desconocido: " + v);
        }

        public String toSpanish() {
            return switch (this) {
                case OVERBOOKING -> "Overbooking";
                case MANTENIMIENTO -> "Mantenimiento";
                case FACTURA     -> "Facturación";
                case LIMPIEZA    -> "Limpieza";
                case CHECKIN     -> "Check-in";
                case CHECKOUT    -> "Check-out";
                case GENERAL     -> "General";
            };
        }
    }

    public enum Gravedad {
        INFO("INFO"), AVISO("WARNING"), ERROR("ERROR");

        public final String valorBD;
        Gravedad(String valorBD) { this.valorBD = valorBD; }

        public static Gravedad desdeBD(String v) {
            for (Gravedad e : values()) if (e.valorBD.equals(v)) return e;
            throw new IllegalArgumentException("Gravedad desconocida: " + v);
        }
    }

    private int id;
    private Tipo tipo;
    private Gravedad gravedad;
    private String titulo;
    private String descripcion;
    private Habitacion habitacionRelacionada;
    private Reserva reservaRelacionada;
    private boolean resuelta;
    private LocalDateTime fechaCreacion;
    private LocalDateTime fechaResolucion;

    public Alerta() {}

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public Tipo getTipo() { return tipo; }
    public void setTipo(Tipo tipo) { this.tipo = tipo; }

    public Gravedad getGravedad() { return gravedad; }
    public void setGravedad(Gravedad gravedad) { this.gravedad = gravedad; }

    public String getTitulo() { return titulo; }
    public void setTitulo(String titulo) { this.titulo = titulo; }

    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }

    public Habitacion getHabitacionRelacionada() { return habitacionRelacionada; }
    public void setHabitacionRelacionada(Habitacion habitacionRelacionada) { this.habitacionRelacionada = habitacionRelacionada; }

    public Reserva getReservaRelacionada() { return reservaRelacionada; }
    public void setReservaRelacionada(Reserva reservaRelacionada) { this.reservaRelacionada = reservaRelacionada; }

    public boolean isResuelta() { return resuelta; }
    public void setResuelta(boolean resuelta) { this.resuelta = resuelta; }

    public LocalDateTime getFechaCreacion() { return fechaCreacion; }
    public void setFechaCreacion(LocalDateTime fechaCreacion) { this.fechaCreacion = fechaCreacion; }

    public LocalDateTime getFechaResolucion() { return fechaResolucion; }
    public void setFechaResolucion(LocalDateTime fechaResolucion) { this.fechaResolucion = fechaResolucion; }
}

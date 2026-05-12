package es.simonsg.pmsuite.model;

import java.time.LocalDateTime;

public class SolicitudMantenimiento {

    public enum Prioridad {
        BAJA("LOW"), MEDIA("MEDIUM"), ALTA("HIGH"), URGENTE("URGENT");

        public final String valorBD;
        Prioridad(String valorBD) { this.valorBD = valorBD; }

        public static Prioridad desdeBD(String v) {
            for (Prioridad e : values()) if (e.valorBD.equals(v)) return e;
            throw new IllegalArgumentException("Prioridad desconocida: " + v);
        }

        public String toSpanish() {
            return switch (this) {
                case BAJA    -> "Baja";
                case MEDIA   -> "Media";
                case ALTA    -> "Alta";
                case URGENTE -> "Urgente";
            };
        }
    }

    public enum Estado {
        PENDIENTE("PENDING"), EN_PROGRESO("IN_PROGRESS"), COMPLETADA("COMPLETED"), CANCELADA("CANCELLED");

        public final String valorBD;
        Estado(String valorBD) { this.valorBD = valorBD; }

        public static Estado desdeBD(String v) {
            for (Estado e : values()) if (e.valorBD.equals(v)) return e;
            throw new IllegalArgumentException("Estado desconocido: " + v);
        }

        public String toSpanish() {
            return switch (this) {
                case PENDIENTE   -> "Pendiente";
                case EN_PROGRESO -> "En curso";
                case COMPLETADA  -> "Completada";
                case CANCELADA   -> "Cancelada";
            };
        }
    }

    private int id;
    private Habitacion habitacion;
    private String titulo;
    private String descripcion;
    private Prioridad prioridad;
    private Estado estado;
    private Usuario reportadoPor;
    private Usuario asignadoA;
    private LocalDateTime fechaReporte;
    private LocalDateTime fechaInicio;
    private LocalDateTime fechaCompletado;
    private String notas;

    public SolicitudMantenimiento() {}

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public Habitacion getHabitacion() { return habitacion; }
    public void setHabitacion(Habitacion habitacion) { this.habitacion = habitacion; }

    public String getTitulo() { return titulo; }
    public void setTitulo(String titulo) { this.titulo = titulo; }

    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }

    public Prioridad getPrioridad() { return prioridad; }
    public void setPrioridad(Prioridad prioridad) { this.prioridad = prioridad; }

    public Estado getEstado() { return estado; }
    public void setEstado(Estado estado) { this.estado = estado; }

    public Usuario getReportadoPor() { return reportadoPor; }
    public void setReportadoPor(Usuario reportadoPor) { this.reportadoPor = reportadoPor; }

    public Usuario getAsignadoA() { return asignadoA; }
    public void setAsignadoA(Usuario asignadoA) { this.asignadoA = asignadoA; }

    public LocalDateTime getFechaReporte() { return fechaReporte; }
    public void setFechaReporte(LocalDateTime fechaReporte) { this.fechaReporte = fechaReporte; }

    public LocalDateTime getFechaInicio() { return fechaInicio; }
    public void setFechaInicio(LocalDateTime fechaInicio) { this.fechaInicio = fechaInicio; }

    public LocalDateTime getFechaCompletado() { return fechaCompletado; }
    public void setFechaCompletado(LocalDateTime fechaCompletado) { this.fechaCompletado = fechaCompletado; }

    public String getNotas() { return notas; }
    public void setNotas(String notas) { this.notas = notas; }
}

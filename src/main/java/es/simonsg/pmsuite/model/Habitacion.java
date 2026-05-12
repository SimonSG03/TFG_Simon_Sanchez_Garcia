package es.simonsg.pmsuite.model;

import java.time.LocalDateTime;

public class Habitacion {

    public enum Estado {
        DISPONIBLE("AVAILABLE"), OCUPADA("OCCUPIED"), FUERA_SERVICIO("OUT_OF_SERVICE"),
        BLOQUEADA("BLOCKED"), LIMPIEZA("CLEANING");

        public final String valorBD;
        Estado(String valorBD) { this.valorBD = valorBD; }

        public static Estado desdeBD(String v) {
            for (Estado e : values()) if (e.valorBD.equals(v)) return e;
            throw new IllegalArgumentException("Estado desconocido: " + v);
        }

        public String toSpanish() {
            return switch (this) {
                case DISPONIBLE    -> "Disponible";
                case OCUPADA       -> "Ocupada";
                case FUERA_SERVICIO -> "Fuera de servicio";
                case BLOQUEADA     -> "Bloqueada";
                case LIMPIEZA      -> "Limpieza";
            };
        }
    }

    private int id;
    private String numero;
    private int planta;
    private TipoHabitacion tipoHabitacion;
    private Estado estado;
    private String notas;
    private LocalDateTime ultimaLimpieza;
    private LocalDateTime fechaCreacion;

    public Habitacion() {}

    public Habitacion(int id, String numero, int planta, TipoHabitacion tipoHabitacion, Estado estado) {
        this.id = id;
        this.numero = numero;
        this.planta = planta;
        this.tipoHabitacion = tipoHabitacion;
        this.estado = estado;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getNumero() { return numero; }
    public void setNumero(String numero) { this.numero = numero; }

    public int getPlanta() { return planta; }
    public void setPlanta(int planta) { this.planta = planta; }

    public TipoHabitacion getTipoHabitacion() { return tipoHabitacion; }
    public void setTipoHabitacion(TipoHabitacion tipoHabitacion) { this.tipoHabitacion = tipoHabitacion; }

    public Estado getEstado() { return estado; }
    public void setEstado(Estado estado) { this.estado = estado; }

    public String getNotas() { return notas; }
    public void setNotas(String notas) { this.notas = notas; }

    public LocalDateTime getUltimaLimpieza() { return ultimaLimpieza; }
    public void setUltimaLimpieza(LocalDateTime ultimaLimpieza) { this.ultimaLimpieza = ultimaLimpieza; }

    public LocalDateTime getFechaCreacion() { return fechaCreacion; }
    public void setFechaCreacion(LocalDateTime fechaCreacion) { this.fechaCreacion = fechaCreacion; }

    @Override
    public String toString() {
        return "Hab. " + numero + (tipoHabitacion != null ? " - " + tipoHabitacion.getNombre() : "");
    }
}

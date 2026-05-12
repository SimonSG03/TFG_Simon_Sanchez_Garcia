package es.simonsg.pmsuite.model;

import java.time.LocalDateTime;
import java.util.Map;

public class PlantillaEmail {

    public enum Tipo {
        CONFIRMACION("CONFIRMATION"), BIENVENIDA("WELCOME"), FACTURA("INVOICE"),
        LIMPIEZA("HOUSEKEEPING"), PERSONALIZADO("CUSTOM");

        public final String valorBD;
        Tipo(String valorBD) { this.valorBD = valorBD; }

        public static Tipo desdeBD(String v) {
            for (Tipo e : values()) if (e.valorBD.equals(v)) return e;
            throw new IllegalArgumentException("Tipo desconocido: " + v);
        }

        public String toSpanish() {
            return switch (this) {
                case CONFIRMACION -> "Confirmación de reserva";
                case BIENVENIDA   -> "Bienvenida";
                case FACTURA      -> "Factura";
                case LIMPIEZA     -> "Housekeeping";
                case PERSONALIZADO -> "Personalizada";
            };
        }
    }

    private int id;
    private String nombre;
    private Tipo tipo;
    private String asunto;
    private String cuerpo;
    private boolean activo;
    private LocalDateTime fechaCreacion;
    private LocalDateTime fechaActualizacion;

    public PlantillaEmail() {}

    /** Sustituye {{variable}} en el asunto con los valores del mapa. */
    public String renderAsunto(Map<String, String> vars) {
        return render(asunto, vars);
    }

    /** Sustituye {{variable}} en el cuerpo con los valores del mapa. */
    public String renderCuerpo(Map<String, String> vars) {
        return render(cuerpo, vars);
    }

    private static String render(String template, Map<String, String> vars) {
        if (template == null) return "";
        String result = template;
        for (Map.Entry<String, String> e : vars.entrySet()) {
            result = result.replace("{{" + e.getKey() + "}}", e.getValue() == null ? "" : e.getValue());
        }
        return result;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public Tipo getTipo() { return tipo; }
    public void setTipo(Tipo tipo) { this.tipo = tipo; }

    public String getAsunto() { return asunto; }
    public void setAsunto(String asunto) { this.asunto = asunto; }

    public String getCuerpo() { return cuerpo; }
    public void setCuerpo(String cuerpo) { this.cuerpo = cuerpo; }

    public boolean isActivo() { return activo; }
    public void setActivo(boolean activo) { this.activo = activo; }

    public LocalDateTime getFechaCreacion() { return fechaCreacion; }
    public void setFechaCreacion(LocalDateTime fechaCreacion) { this.fechaCreacion = fechaCreacion; }

    public LocalDateTime getFechaActualizacion() { return fechaActualizacion; }
    public void setFechaActualizacion(LocalDateTime fechaActualizacion) { this.fechaActualizacion = fechaActualizacion; }

    @Override
    public String toString() { return nombre != null ? nombre : ""; }
}

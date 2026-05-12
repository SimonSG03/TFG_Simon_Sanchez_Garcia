package es.simonsg.pmsuite.model;

import java.time.LocalDateTime;

public class Usuario {

    public enum Rol {
        ADMIN("ADMIN"), GERENTE("MANAGER"), RECEPCIONISTA("RECEPTIONIST"),
        LIMPIEZA("HOUSEKEEPER"), MANTENIMIENTO("MAINTENANCE"), RESTAURACION("RESTAURATION");

        public final String valorBD;
        Rol(String valorBD) { this.valorBD = valorBD; }

        public static Rol desdeBD(String v) {
            for (Rol e : values()) if (e.valorBD.equals(v)) return e;
            throw new IllegalArgumentException("Rol desconocido: " + v);
        }

        public String toSpanish() {
            return switch (this) {
                case ADMIN          -> "Administrador";
                case GERENTE        -> "Director";
                case RECEPCIONISTA  -> "Recepcionista";
                case LIMPIEZA       -> "Limpieza";
                case MANTENIMIENTO  -> "Mantenimiento";
                case RESTAURACION   -> "Restauración";
            };
        }
    }

    private int id;
    private String nombreUsuario;
    private String email;
    private String hashContrasena;
    private String nombreCompleto;
    private Rol rol;
    private boolean activo;
    private LocalDateTime ultimoAcceso;
    private LocalDateTime fechaCreacion;

    public Usuario() {}

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getNombreUsuario() { return nombreUsuario; }
    public void setNombreUsuario(String nombreUsuario) { this.nombreUsuario = nombreUsuario; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getHashContrasena() { return hashContrasena; }
    public void setHashContrasena(String hashContrasena) { this.hashContrasena = hashContrasena; }

    public String getNombreCompleto() { return nombreCompleto; }
    public void setNombreCompleto(String nombreCompleto) { this.nombreCompleto = nombreCompleto; }

    public Rol getRol() { return rol; }
    public void setRol(Rol rol) { this.rol = rol; }

    public boolean isActivo() { return activo; }
    public void setActivo(boolean activo) { this.activo = activo; }

    public LocalDateTime getUltimoAcceso() { return ultimoAcceso; }
    public void setUltimoAcceso(LocalDateTime ultimoAcceso) { this.ultimoAcceso = ultimoAcceso; }

    public LocalDateTime getFechaCreacion() { return fechaCreacion; }
    public void setFechaCreacion(LocalDateTime fechaCreacion) { this.fechaCreacion = fechaCreacion; }

    @Override
    public String toString() {
        return nombreCompleto + " (" + (rol != null ? rol.toSpanish() : "") + ")";
    }
}

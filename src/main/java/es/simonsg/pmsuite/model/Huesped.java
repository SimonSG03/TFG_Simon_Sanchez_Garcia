package es.simonsg.pmsuite.model;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class Huesped {

    private int id;
    private String nif;
    private String nombre;
    private String apellidos;
    private String email;
    private String telefono;
    private String nacionalidad;
    private String pais;
    private String domicilio;
    private String ciudad;
    private String codigoPostal;
    private LocalDate fechaNacimiento;
    private String notas;
    private LocalDateTime fechaCreacion;

    public Huesped() {}

    public Huesped(int id, String nif, String nombre, String apellidos, String email, String telefono) {
        this.id = id;
        this.nif = nif;
        this.nombre = nombre;
        this.apellidos = apellidos;
        this.email = email;
        this.telefono = telefono;
    }

    public String getNombreCompleto() {
        return nombre + " " + apellidos;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getNif() { return nif; }
    public void setNif(String nif) { this.nif = nif; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getApellidos() { return apellidos; }
    public void setApellidos(String apellidos) { this.apellidos = apellidos; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getTelefono() { return telefono; }
    public void setTelefono(String telefono) { this.telefono = telefono; }

    public String getNacionalidad() { return nacionalidad; }
    public void setNacionalidad(String nacionalidad) { this.nacionalidad = nacionalidad; }

    public String getPais() { return pais; }
    public void setPais(String pais) { this.pais = pais; }

    public String getDomicilio() { return domicilio; }
    public void setDomicilio(String domicilio) { this.domicilio = domicilio; }

    public String getCiudad() { return ciudad; }
    public void setCiudad(String ciudad) { this.ciudad = ciudad; }

    public String getCodigoPostal() { return codigoPostal; }
    public void setCodigoPostal(String codigoPostal) { this.codigoPostal = codigoPostal; }

    public LocalDate getFechaNacimiento() { return fechaNacimiento; }
    public void setFechaNacimiento(LocalDate fechaNacimiento) { this.fechaNacimiento = fechaNacimiento; }

    public String getNotas() { return notas; }
    public void setNotas(String notas) { this.notas = notas; }

    public LocalDateTime getFechaCreacion() { return fechaCreacion; }
    public void setFechaCreacion(LocalDateTime fechaCreacion) { this.fechaCreacion = fechaCreacion; }

    @Override
    public String toString() {
        return getNombreCompleto();
    }
}

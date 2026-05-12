package es.simonsg.pmsuite.model;

import java.math.BigDecimal;

public class TipoHabitacion {

    private int id;
    private String codigo;
    private String nombre;
    private String descripcion;
    private int capacidadMaxima;
    private BigDecimal precioBase;
    private String amenidades;

    public TipoHabitacion() {}

    public TipoHabitacion(int id, String codigo, String nombre, String descripcion,
                          int capacidadMaxima, BigDecimal precioBase) {
        this.id = id;
        this.codigo = codigo;
        this.nombre = nombre;
        this.descripcion = descripcion;
        this.capacidadMaxima = capacidadMaxima;
        this.precioBase = precioBase;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getCodigo() { return codigo; }
    public void setCodigo(String codigo) { this.codigo = codigo; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }

    public int getCapacidadMaxima() { return capacidadMaxima; }
    public void setCapacidadMaxima(int capacidadMaxima) { this.capacidadMaxima = capacidadMaxima; }

    public BigDecimal getPrecioBase() { return precioBase; }
    public void setPrecioBase(BigDecimal precioBase) { this.precioBase = precioBase; }

    public String getAmenidades() { return amenidades; }
    public void setAmenidades(String amenidades) { this.amenidades = amenidades; }

    @Override
    public String toString() {
        return nombre;
    }
}


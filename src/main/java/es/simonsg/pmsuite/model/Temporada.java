package es.simonsg.pmsuite.model;

import java.math.BigDecimal;
import java.time.LocalDate;

public class Temporada {
    private int id;
    private String nombre;
    private LocalDate fechaInicio;
    private LocalDate fechaFin;
    private BigDecimal multiplicador;

    public Temporada() {}

    public Temporada(int id, String nombre, LocalDate fechaInicio, LocalDate fechaFin, BigDecimal multiplicador) {
        this.id = id;
        this.nombre = nombre;
        this.fechaInicio = fechaInicio;
        this.fechaFin = fechaFin;
        this.multiplicador = multiplicador;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public LocalDate getFechaInicio() { return fechaInicio; }
    public void setFechaInicio(LocalDate fechaInicio) { this.fechaInicio = fechaInicio; }

    public LocalDate getFechaFin() { return fechaFin; }
    public void setFechaFin(LocalDate fechaFin) { this.fechaFin = fechaFin; }

    public BigDecimal getMultiplicador() { return multiplicador; }
    public void setMultiplicador(BigDecimal multiplicador) { this.multiplicador = multiplicador; }

    @Override
    public String toString() { return nombre; }
}

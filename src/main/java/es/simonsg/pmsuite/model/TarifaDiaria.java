package es.simonsg.pmsuite.model;

import java.math.BigDecimal;
import java.time.LocalDate;

public class TarifaDiaria {
    private int id;
    private TipoHabitacion tipoHabitacion;
    private LocalDate fecha;
    private BigDecimal precio;
    private String notas;

    public TarifaDiaria() {}

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public TipoHabitacion getTipoHabitacion() { return tipoHabitacion; }
    public void setTipoHabitacion(TipoHabitacion tipoHabitacion) { this.tipoHabitacion = tipoHabitacion; }

    public LocalDate getFecha() { return fecha; }
    public void setFecha(LocalDate fecha) { this.fecha = fecha; }

    public BigDecimal getPrecio() { return precio; }
    public void setPrecio(BigDecimal precio) { this.precio = precio; }

    public String getNotas() { return notas; }
    public void setNotas(String notas) { this.notas = notas; }
}

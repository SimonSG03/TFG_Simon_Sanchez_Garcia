package es.simonsg.pmsuite.model;

import java.math.BigDecimal;

public class PlanTarifa {
    private int id;
    private TipoHabitacion tipoHabitacion;
    private Temporada temporada;
    private String nombre;
    private BigDecimal precioPorNoche;
    private int nochesMinimas;
    private Integer nochesMaximas;
    private boolean incluyeDesayuno;

    public PlanTarifa() {}

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public TipoHabitacion getTipoHabitacion() { return tipoHabitacion; }
    public void setTipoHabitacion(TipoHabitacion tipoHabitacion) { this.tipoHabitacion = tipoHabitacion; }

    public Temporada getTemporada() { return temporada; }
    public void setTemporada(Temporada temporada) { this.temporada = temporada; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public BigDecimal getPrecioPorNoche() { return precioPorNoche; }
    public void setPrecioPorNoche(BigDecimal precioPorNoche) { this.precioPorNoche = precioPorNoche; }

    public int getNochesMinimas() { return nochesMinimas; }
    public void setNochesMinimas(int nochesMinimas) { this.nochesMinimas = nochesMinimas; }

    public Integer getNochesMaximas() { return nochesMaximas; }
    public void setNochesMaximas(Integer nochesMaximas) { this.nochesMaximas = nochesMaximas; }

    public boolean isIncluyeDesayuno() { return incluyeDesayuno; }
    public void setIncluyeDesayuno(boolean incluyeDesayuno) { this.incluyeDesayuno = incluyeDesayuno; }
}

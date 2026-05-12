package es.simonsg.pmsuite.model;

import java.math.BigDecimal;
import java.time.LocalDate;

public class Promocion {
    private int id;
    private String nombre;
    private String codigo;
    private String tipoDescuento; // "PERCENT" or "FIXED"
    private BigDecimal valorDescuento;
    private LocalDate fechaInicio;
    private LocalDate fechaFin;
    private boolean activo;
    private String notas;

    public Promocion() {}

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getCodigo() { return codigo; }
    public void setCodigo(String codigo) { this.codigo = codigo; }

    public String getTipoDescuento() { return tipoDescuento; }
    public void setTipoDescuento(String tipoDescuento) { this.tipoDescuento = tipoDescuento; }

    public BigDecimal getValorDescuento() { return valorDescuento; }
    public void setValorDescuento(BigDecimal valorDescuento) { this.valorDescuento = valorDescuento; }

    public LocalDate getFechaInicio() { return fechaInicio; }
    public void setFechaInicio(LocalDate fechaInicio) { this.fechaInicio = fechaInicio; }

    public LocalDate getFechaFin() { return fechaFin; }
    public void setFechaFin(LocalDate fechaFin) { this.fechaFin = fechaFin; }

    public boolean isActivo() { return activo; }
    public void setActivo(boolean activo) { this.activo = activo; }

    public String getNotas() { return notas; }
    public void setNotas(String notas) { this.notas = notas; }
}

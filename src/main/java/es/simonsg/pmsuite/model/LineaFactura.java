package es.simonsg.pmsuite.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class LineaFactura {

    private int id;
    private int idFactura;
    private String descripcion;
    private BigDecimal cantidad;
    private BigDecimal precioUnitario;
    private BigDecimal precioTotal;
    private LocalDateTime fechaCreacion;

    public LineaFactura() {}

    public LineaFactura(int idFactura, String descripcion, BigDecimal cantidad, BigDecimal precioUnitario) {
        this.idFactura = idFactura;
        this.descripcion = descripcion;
        this.cantidad = cantidad;
        this.precioUnitario = precioUnitario;
        this.precioTotal = cantidad.multiply(precioUnitario);
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getIdFactura() { return idFactura; }
    public void setIdFactura(int idFactura) { this.idFactura = idFactura; }

    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }

    public BigDecimal getCantidad() { return cantidad; }
    public void setCantidad(BigDecimal cantidad) { this.cantidad = cantidad; }

    public BigDecimal getPrecioUnitario() { return precioUnitario; }
    public void setPrecioUnitario(BigDecimal precioUnitario) { this.precioUnitario = precioUnitario; }

    public BigDecimal getPrecioTotal() { return precioTotal; }
    public void setPrecioTotal(BigDecimal precioTotal) { this.precioTotal = precioTotal; }

    public LocalDateTime getFechaCreacion() { return fechaCreacion; }
    public void setFechaCreacion(LocalDateTime fechaCreacion) { this.fechaCreacion = fechaCreacion; }
}


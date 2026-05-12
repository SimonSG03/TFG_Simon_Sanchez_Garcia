package es.simonsg.pmsuite.model;

import java.math.BigDecimal;

public class LineaPedido {
    private int id;
    private int idPedido;
    private ArticuloMenu articulo;
    private int cantidad;
    private BigDecimal precioUnitario;
    private String notas;

    public LineaPedido() {}

    public LineaPedido(int idPedido, ArticuloMenu articulo, int cantidad, BigDecimal precioUnitario) {
        this.idPedido = idPedido;
        this.articulo = articulo;
        this.cantidad = cantidad;
        this.precioUnitario = precioUnitario;
    }

    public BigDecimal getTotal() {
        if (precioUnitario == null) return BigDecimal.ZERO;
        return precioUnitario.multiply(BigDecimal.valueOf(cantidad));
    }

    public int getId()                  { return id; }
    public void setId(int id)           { this.id = id; }
    public int getIdPedido()            { return idPedido; }
    public void setIdPedido(int o)      { this.idPedido = o; }
    public ArticuloMenu getArticulo()   { return articulo; }
    public void setArticulo(ArticuloMenu m) { this.articulo = m; }
    public int getCantidad()            { return cantidad; }
    public void setCantidad(int q)      { this.cantidad = q; }
    public BigDecimal getPrecioUnitario() { return precioUnitario; }
    public void setPrecioUnitario(BigDecimal p) { this.precioUnitario = p; }
    public String getNotas()            { return notas; }
    public void setNotas(String n)      { this.notas = n; }
}


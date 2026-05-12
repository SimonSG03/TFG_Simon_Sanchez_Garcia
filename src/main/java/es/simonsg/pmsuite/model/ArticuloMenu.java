package es.simonsg.pmsuite.model;

import java.math.BigDecimal;

public class ArticuloMenu {
    private int id;
    private CategoriaMenu categoria;
    private String nombre;
    private String descripcion;
    private BigDecimal precio;
    private boolean disponible;
    private String alergenos;

    public ArticuloMenu() { this.disponible = true; }

    public int getId()                  { return id; }
    public void setId(int id)           { this.id = id; }
    public CategoriaMenu getCategoria() { return categoria; }
    public void setCategoria(CategoriaMenu c) { this.categoria = c; }
    public String getNombre()           { return nombre; }
    public void setNombre(String n)     { this.nombre = n; }
    public String getDescripcion()      { return descripcion; }
    public void setDescripcion(String d){ this.descripcion = d; }
    public BigDecimal getPrecio()       { return precio; }
    public void setPrecio(BigDecimal p) { this.precio = p; }
    public boolean isDisponible()       { return disponible; }
    public void setDisponible(boolean a){ this.disponible = a; }
    public String getAlergenos()        { return alergenos; }
    public void setAlergenos(String a)  { this.alergenos = a; }

    @Override public String toString()  { return nombre != null ? nombre : ""; }
}

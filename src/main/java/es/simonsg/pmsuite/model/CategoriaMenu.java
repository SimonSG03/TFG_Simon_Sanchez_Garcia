package es.simonsg.pmsuite.model;

public class CategoriaMenu {
    private int id;
    private String nombre;
    private String descripcion;
    private String color;
    private boolean activo;

    public CategoriaMenu() { this.activo = true; this.color = "#607D8B"; }

    public int getId()              { return id; }
    public void setId(int id)       { this.id = id; }
    public String getNombre()       { return nombre; }
    public void setNombre(String n) { this.nombre = n; }
    public String getDescripcion()  { return descripcion; }
    public void setDescripcion(String d) { this.descripcion = d; }
    public String getColor()        { return color; }
    public void setColor(String c)  { this.color = c; }
    public boolean isActivo()       { return activo; }
    public void setActivo(boolean a){ this.activo = a; }

    @Override public String toString() { return nombre != null ? nombre : ""; }
}

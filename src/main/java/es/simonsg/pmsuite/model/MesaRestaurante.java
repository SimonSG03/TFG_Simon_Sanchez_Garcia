package es.simonsg.pmsuite.model;

public class MesaRestaurante {
    private int id;
    private String zona;
    private String nombre;
    private int capacidad;
    private int orden;
    private double posX;
    private double posY;
    private boolean activo;

    public MesaRestaurante() { this.capacidad = 4; this.activo = true; }

    public int getId()              { return id; }
    public void setId(int id)       { this.id = id; }
    public String getZona()         { return zona; }
    public void setZona(String z)   { this.zona = z; }
    public String getNombre()       { return nombre; }
    public void setNombre(String n) { this.nombre = n; }
    public int getCapacidad()       { return capacidad; }
    public void setCapacidad(int c) { this.capacidad = c; }
    public int getOrden()           { return orden; }
    public void setOrden(int s)     { this.orden = s; }
    public double getPosX()         { return posX; }
    public void setPosX(double x)   { this.posX = x; }
    public double getPosY()         { return posY; }
    public void setPosY(double y)   { this.posY = y; }
    public boolean isActivo()       { return activo; }
    public void setActivo(boolean a){ this.activo = a; }

    @Override public String toString() { return nombre != null ? nombre : ""; }
}

package es.simonsg.pmsuite.model;

/**
 * Datos del establecimiento hotelero.
 * Estos campos se configurarán desde el módulo Maestros y se almacenan
 * en la tabla {@code hotel} de la base de datos.
 */
public class Hotel {

    private int    id;
    private String nombre;
    private String domicilio;
    private String ciudad;
    private String codigoPostal;
    private String pais;
    private String telefono;
    private String email;
    private String sitioWeb;
    private String nif;
    private String iban;
    private String rutaLogo;

    public Hotel() {}

    // ── Getters / Setters ────────────────────────────────────────

    public int    getId()         { return id; }
    public void   setId(int id)   { this.id = id; }

    public String getNombre()                  { return nombre; }
    public void   setNombre(String nombre)     { this.nombre = nombre; }

    public String getDomicilio()               { return domicilio; }
    public void   setDomicilio(String domicilio) { this.domicilio = domicilio; }

    public String getCiudad()                  { return ciudad; }
    public void   setCiudad(String ciudad)     { this.ciudad = ciudad; }

    public String getCodigoPostal()                    { return codigoPostal; }
    public void   setCodigoPostal(String codigoPostal) { this.codigoPostal = codigoPostal; }

    public String getPais()               { return pais; }
    public void   setPais(String pais)    { this.pais = pais; }

    public String getTelefono()                  { return telefono; }
    public void   setTelefono(String telefono)   { this.telefono = telefono; }

    public String getEmail()                 { return email; }
    public void   setEmail(String email)     { this.email = email; }

    public String getSitioWeb()               { return sitioWeb; }
    public void   setSitioWeb(String sitioWeb) { this.sitioWeb = sitioWeb; }

    public String getNif()                   { return nif; }
    public void   setNif(String nif)         { this.nif = nif; }

    public String getIban()                  { return iban; }
    public void   setIban(String iban)       { this.iban = iban; }

    public String getRutaLogo()                  { return rutaLogo; }
    public void   setRutaLogo(String rutaLogo)   { this.rutaLogo = rutaLogo; }

    /** Dirección completa formateada para imprimir en facturas. */
    public String getDireccionCompleta() {
        StringBuilder sb = new StringBuilder();
        if (domicilio  != null && !domicilio.isBlank())     sb.append(domicilio);
        if (codigoPostal != null && !codigoPostal.isBlank()) sb.append(", ").append(codigoPostal);
        if (ciudad     != null && !ciudad.isBlank())         sb.append(" ").append(ciudad);
        if (pais       != null && !pais.isBlank())           sb.append(" (").append(pais).append(")");
        return sb.toString();
    }
}

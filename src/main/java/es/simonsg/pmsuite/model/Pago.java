package es.simonsg.pmsuite.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class Pago {

    private int id;
    private int idFactura;
    private BigDecimal importe;
    private Factura.MetodoPago metodo;
    private String referencia;
    private LocalDateTime fechaPago;
    private int procesadoPor;
    private String notas;
    // Display-only fields populated by extended queries
    private String numeroFactura;
    private String nombreHuesped;

    public Pago() {}

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getIdFactura() { return idFactura; }
    public void setIdFactura(int idFactura) { this.idFactura = idFactura; }

    public BigDecimal getImporte() { return importe; }
    public void setImporte(BigDecimal importe) { this.importe = importe; }

    public Factura.MetodoPago getMetodo() { return metodo; }
    public void setMetodo(Factura.MetodoPago metodo) { this.metodo = metodo; }

    public String getReferencia() { return referencia; }
    public void setReferencia(String referencia) { this.referencia = referencia; }

    public LocalDateTime getFechaPago() { return fechaPago; }
    public void setFechaPago(LocalDateTime fechaPago) { this.fechaPago = fechaPago; }

    public int getProcesadoPor() { return procesadoPor; }
    public void setProcesadoPor(int procesadoPor) { this.procesadoPor = procesadoPor; }

    public String getNotas() { return notas; }
    public void setNotas(String notas) { this.notas = notas; }

    public String getNumeroFactura() { return numeroFactura; }
    public void setNumeroFactura(String numeroFactura) { this.numeroFactura = numeroFactura; }

    public String getNombreHuesped() { return nombreHuesped; }
    public void setNombreHuesped(String nombreHuesped) { this.nombreHuesped = nombreHuesped; }
}


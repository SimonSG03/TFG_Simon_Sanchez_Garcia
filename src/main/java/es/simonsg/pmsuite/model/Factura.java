package es.simonsg.pmsuite.model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public class Factura {

    public enum Estado {
        PENDIENTE("PENDING"), PAGADA("PAID"), PARCIALMENTE_PAGADA("PARTIALLY_PAID"), CANCELADA("CANCELLED");

        public final String valorBD;
        Estado(String valorBD) { this.valorBD = valorBD; }

        public static Estado desdeBD(String v) {
            for (Estado e : values()) if (e.valorBD.equals(v)) return e;
            throw new IllegalArgumentException("Estado desconocido: " + v);
        }

        public String toSpanish() {
            return switch (this) {
                case PENDIENTE           -> "Pendiente";
                case PAGADA              -> "Pagada";
                case PARCIALMENTE_PAGADA -> "Pago parcial";
                case CANCELADA           -> "Cancelada";
            };
        }
    }

    public enum MetodoPago {
        EFECTIVO("CASH"), TARJETA_CREDITO("CREDIT_CARD"), TARJETA_DEBITO("DEBIT_CARD"),
        TRANSFERENCIA("BANK_TRANSFER"), OTRO("OTHER");

        public final String valorBD;
        MetodoPago(String valorBD) { this.valorBD = valorBD; }

        public static MetodoPago desdeBD(String v) {
            for (MetodoPago e : values()) if (e.valorBD.equals(v)) return e;
            throw new IllegalArgumentException("MetodoPago desconocido: " + v);
        }

        public String toSpanish() {
            return switch (this) {
                case EFECTIVO        -> "Efectivo";
                case TARJETA_CREDITO -> "Tarjeta crédito";
                case TARJETA_DEBITO  -> "Tarjeta débito";
                case TRANSFERENCIA   -> "Transferencia";
                case OTRO            -> "Otro";
            };
        }
    }

    private int id;
    private String numeroFactura;
    private Reserva reserva;
    private Huesped huesped;
    private LocalDate fechaEmision;
    private LocalDate fechaVencimiento;
    private BigDecimal subtotal;
    private BigDecimal tasaImpuesto;
    private BigDecimal importeImpuesto;
    private BigDecimal importeTotal;
    private BigDecimal importePagado;
    private Estado estado;
    private MetodoPago metodoPago;
    private String notas;
    private LocalDateTime fechaCreacion;

    // Datos de facturación alternativos (empresa, tercero, etc.)
    private String nombreFacturacion;
    private String nifFacturacion;
    private String domicilioFacturacion;
    private String ciudadFacturacion;
    private String cpFacturacion;
    private String paisFacturacion;

    public Factura() {}

    public BigDecimal getImportePendiente() {
        if (importeTotal != null && importePagado != null) {
            return importeTotal.subtract(importePagado);
        }
        return importeTotal;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getNumeroFactura() { return numeroFactura; }
    public void setNumeroFactura(String numeroFactura) { this.numeroFactura = numeroFactura; }

    public Reserva getReserva() { return reserva; }
    public void setReserva(Reserva reserva) { this.reserva = reserva; }

    public Huesped getHuesped() { return huesped; }
    public void setHuesped(Huesped huesped) { this.huesped = huesped; }

    public LocalDate getFechaEmision() { return fechaEmision; }
    public void setFechaEmision(LocalDate fechaEmision) { this.fechaEmision = fechaEmision; }

    public LocalDate getFechaVencimiento() { return fechaVencimiento; }
    public void setFechaVencimiento(LocalDate fechaVencimiento) { this.fechaVencimiento = fechaVencimiento; }

    public BigDecimal getSubtotal() { return subtotal; }
    public void setSubtotal(BigDecimal subtotal) { this.subtotal = subtotal; }

    public BigDecimal getTasaImpuesto() { return tasaImpuesto; }
    public void setTasaImpuesto(BigDecimal tasaImpuesto) { this.tasaImpuesto = tasaImpuesto; }

    public BigDecimal getImporteImpuesto() { return importeImpuesto; }
    public void setImporteImpuesto(BigDecimal importeImpuesto) { this.importeImpuesto = importeImpuesto; }

    public BigDecimal getImporteTotal() { return importeTotal; }
    public void setImporteTotal(BigDecimal importeTotal) { this.importeTotal = importeTotal; }

    public BigDecimal getImportePagado() { return importePagado; }
    public void setImportePagado(BigDecimal importePagado) { this.importePagado = importePagado; }

    public Estado getEstado() { return estado; }
    public void setEstado(Estado estado) { this.estado = estado; }

    public MetodoPago getMetodoPago() { return metodoPago; }
    public void setMetodoPago(MetodoPago metodoPago) { this.metodoPago = metodoPago; }

    public String getNotas() { return notas; }
    public void setNotas(String notas) { this.notas = notas; }

    public LocalDateTime getFechaCreacion() { return fechaCreacion; }
    public void setFechaCreacion(LocalDateTime fechaCreacion) { this.fechaCreacion = fechaCreacion; }

    /** Devuelve true si la factura tiene datos de facturación alternativos (empresa/tercero). */
    public boolean tieneDatosFacturacion() {
        return nombreFacturacion != null && !nombreFacturacion.isBlank();
    }

    public String getNombreFacturacion()       { return nombreFacturacion; }
    public void setNombreFacturacion(String v) { this.nombreFacturacion = v; }

    public String getNifFacturacion()        { return nifFacturacion; }
    public void setNifFacturacion(String v)  { this.nifFacturacion = v; }

    public String getDomicilioFacturacion()        { return domicilioFacturacion; }
    public void setDomicilioFacturacion(String v)  { this.domicilioFacturacion = v; }

    public String getCiudadFacturacion()       { return ciudadFacturacion; }
    public void setCiudadFacturacion(String v) { this.ciudadFacturacion = v; }

    public String getCpFacturacion()        { return cpFacturacion; }
    public void setCpFacturacion(String v)  { this.cpFacturacion = v; }

    public String getPaisFacturacion()        { return paisFacturacion; }
    public void setPaisFacturacion(String v)  { this.paisFacturacion = v; }
}

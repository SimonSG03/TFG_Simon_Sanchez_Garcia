package es.simonsg.pmsuite.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Factura")
class FacturaTest {

    private Factura factura;

    @BeforeEach
    void setUp() {
        factura = new Factura();
    }

    // ── Estado enum

    @Test
    @DisplayName("Estado.desdeBD mapea PENDING a PENDIENTE")
    void estadoDesdeBD_pending() {
        assertEquals(Factura.Estado.PENDIENTE, Factura.Estado.desdeBD("PENDING"));
    }

    @Test
    @DisplayName("Estado.desdeBD mapea PAID a PAGADA")
    void estadoDesdeBD_paid() {
        assertEquals(Factura.Estado.PAGADA, Factura.Estado.desdeBD("PAID"));
    }

    @Test
    @DisplayName("Estado.desdeBD mapea PARTIALLY_PAID a PARCIALMENTE_PAGADA")
    void estadoDesdeBD_partiallyPaid() {
        assertEquals(Factura.Estado.PARCIALMENTE_PAGADA, Factura.Estado.desdeBD("PARTIALLY_PAID"));
    }

    @Test
    @DisplayName("Estado.desdeBD mapea CANCELLED a CANCELADA")
    void estadoDesdeBD_cancelled() {
        assertEquals(Factura.Estado.CANCELADA, Factura.Estado.desdeBD("CANCELLED"));
    }

    @Test
    @DisplayName("Estado.desdeBD lanza excepción con valor desconocido")
    void estadoDesdeBD_valorDesconocido() {
        assertThrows(IllegalArgumentException.class, () -> Factura.Estado.desdeBD("UNKNOWN"));
    }

    @Test
    @DisplayName("Estado.toSpanish de PARCIALMENTE_PAGADA devuelve 'Pago parcial'")
    void estadoToSpanish_parcialmentePagada() {
        assertEquals("Pago parcial", Factura.Estado.PARCIALMENTE_PAGADA.toSpanish());
    }

    // ── MetodoPago enum

    @Test
    @DisplayName("MetodoPago.desdeBD mapea CASH a EFECTIVO")
    void metodoPagoDesdeBD_cash() {
        assertEquals(Factura.MetodoPago.EFECTIVO, Factura.MetodoPago.desdeBD("CASH"));
    }

    @Test
    @DisplayName("MetodoPago.desdeBD mapea CREDIT_CARD a TARJETA_CREDITO")
    void metodoPagoDesdeBD_creditCard() {
        assertEquals(Factura.MetodoPago.TARJETA_CREDITO, Factura.MetodoPago.desdeBD("CREDIT_CARD"));
    }

    @Test
    @DisplayName("MetodoPago.desdeBD mapea DEBIT_CARD a TARJETA_DEBITO")
    void metodoPagoDesdeBD_debitCard() {
        assertEquals(Factura.MetodoPago.TARJETA_DEBITO, Factura.MetodoPago.desdeBD("DEBIT_CARD"));
    }

    @Test
    @DisplayName("MetodoPago.desdeBD mapea BANK_TRANSFER a TRANSFERENCIA")
    void metodoPagoDesdeBD_bankTransfer() {
        assertEquals(Factura.MetodoPago.TRANSFERENCIA, Factura.MetodoPago.desdeBD("BANK_TRANSFER"));
    }

    @Test
    @DisplayName("MetodoPago.desdeBD lanza excepción con valor desconocido")
    void metodoPagoDesdeBD_valorDesconocido() {
        assertThrows(IllegalArgumentException.class, () -> Factura.MetodoPago.desdeBD("BITCOIN"));
    }

    @Test
    @DisplayName("MetodoPago.toSpanish de TRANSFERENCIA devuelve 'Transferencia'")
    void metodoPagoToSpanish_transferencia() {
        assertEquals("Transferencia", Factura.MetodoPago.TRANSFERENCIA.toSpanish());
    }

    // ── getImportePendiente

    @Test
    @DisplayName("getImportePendiente devuelve la diferencia entre total y pagado")
    void getImportePendiente_parcial() {
        factura.setImporteTotal(new BigDecimal("300.00"));
        factura.setImportePagado(new BigDecimal("100.00"));
        assertEquals(0, new BigDecimal("200.00").compareTo(factura.getImportePendiente()));
    }

    @Test
    @DisplayName("getImportePendiente devuelve cero cuando está completamente pagada")
    void getImportePendiente_completamentePagada() {
        factura.setImporteTotal(new BigDecimal("150.00"));
        factura.setImportePagado(new BigDecimal("150.00"));
        assertEquals(0, BigDecimal.ZERO.compareTo(factura.getImportePendiente()));
    }

    @Test
    @DisplayName("getImportePendiente devuelve el total cuando no hay pagos")
    void getImportePendiente_sinPagos() {
        factura.setImporteTotal(new BigDecimal("200.00"));
        factura.setImportePagado(BigDecimal.ZERO);
        assertEquals(0, new BigDecimal("200.00").compareTo(factura.getImportePendiente()));
    }

    @Test
    @DisplayName("getImportePendiente devuelve el total cuando importePagado es null")
    void getImportePendiente_pagadoNull() {
        factura.setImporteTotal(new BigDecimal("99.90"));
        factura.setImportePagado(null);
        assertEquals(0, new BigDecimal("99.90").compareTo(factura.getImportePendiente()));
    }

    // ── tieneDatosFacturacion

    @Test
    @DisplayName("tieneDatosFacturacion devuelve true cuando nombreFacturacion tiene valor")
    void tieneDatosFacturacion_conNombre() {
        factura.setNombreFacturacion("Empresa S.L.");
        assertTrue(factura.tieneDatosFacturacion());
    }

    @Test
    @DisplayName("tieneDatosFacturacion devuelve false cuando nombreFacturacion es null")
    void tieneDatosFacturacion_null() {
        assertFalse(factura.tieneDatosFacturacion());
    }

    @Test
    @DisplayName("tieneDatosFacturacion devuelve false cuando nombreFacturacion está en blanco")
    void tieneDatosFacturacion_blanco() {
        factura.setNombreFacturacion("   ");
        assertFalse(factura.tieneDatosFacturacion());
    }

    // ── setters y getters

    @Test
    @DisplayName("setFechaEmision y getFechaEmision funcionan correctamente")
    void setGetFechaEmision() {
        LocalDate hoy = LocalDate.of(2026, 5, 13);
        factura.setFechaEmision(hoy);
        assertEquals(hoy, factura.getFechaEmision());
    }

    @Test
    @DisplayName("setEstado y getEstado funcionan correctamente")
    void setGetEstado() {
        factura.setEstado(Factura.Estado.PAGADA);
        assertEquals(Factura.Estado.PAGADA, factura.getEstado());
    }
}

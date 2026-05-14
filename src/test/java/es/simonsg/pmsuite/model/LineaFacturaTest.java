package es.simonsg.pmsuite.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("LineaFactura")
class LineaFacturaTest {

    // ── Constructor con cálculo automático

    @Test
    @DisplayName("Constructor calcula precioTotal = cantidad * precioUnitario")
    void constructor_calculaPrecioTotal() {
        LineaFactura linea = new LineaFactura(1, "Noche de alojamiento", new BigDecimal("3"), new BigDecimal("90.00"));
        assertEquals(0, new BigDecimal("270.00").compareTo(linea.getPrecioTotal()));
    }

    @Test
    @DisplayName("Constructor con cantidad 1 devuelve precioTotal igual al precioUnitario")
    void constructor_cantidadUno() {
        LineaFactura linea = new LineaFactura(1, "Desayuno", new BigDecimal("1"), new BigDecimal("12.50"));
        assertEquals(0, new BigDecimal("12.50").compareTo(linea.getPrecioTotal()));
    }

    @Test
    @DisplayName("Constructor almacena descripción correctamente")
    void constructor_descripcion() {
        LineaFactura linea = new LineaFactura(2, "Minibar", new BigDecimal("2"), new BigDecimal("8.00"));
        assertEquals("Minibar", linea.getDescripcion());
    }

    @Test
    @DisplayName("Constructor almacena idFactura correctamente")
    void constructor_idFactura() {
        LineaFactura linea = new LineaFactura(5, "Parking", new BigDecimal("1"), new BigDecimal("15.00"));
        assertEquals(5, linea.getIdFactura());
    }

    @Test
    @DisplayName("Constructor con decimales calcula correctamente")
    void constructor_conDecimales() {
        LineaFactura linea = new LineaFactura(1, "Servicio spa", new BigDecimal("2.5"), new BigDecimal("40.00"));
        assertEquals(0, new BigDecimal("100.00").compareTo(linea.getPrecioTotal()));
    }

    // ── Constructor vacío y setters

    @Test
    @DisplayName("Constructor vacío crea línea sin datos")
    void constructor_vacio() {
        LineaFactura linea = new LineaFactura();
        assertNull(linea.getDescripcion());
        assertNull(linea.getCantidad());
        assertEquals(0, linea.getId());
    }

    @Test
    @DisplayName("setPrecioTotal permite sobreescribir el valor calculado")
    void setPrecioTotal_sobreescribe() {
        LineaFactura linea = new LineaFactura(1, "Test", new BigDecimal("1"), new BigDecimal("10.00"));
        linea.setPrecioTotal(new BigDecimal("999.99"));
        assertEquals(0, new BigDecimal("999.99").compareTo(linea.getPrecioTotal()));
    }

    @Test
    @DisplayName("setDescripcion y getDescripcion funcionan correctamente")
    void setGetDescripcion() {
        LineaFactura linea = new LineaFactura();
        linea.setDescripcion("Lavandería");
        assertEquals("Lavandería", linea.getDescripcion());
    }
}

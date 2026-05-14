package es.simonsg.pmsuite.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("TipoHabitacion")
class TipoHabitacionTest {

    // ── Constructor completo
    @Test
    @DisplayName("Constructor inicializa todos los campos correctamente")
    void constructor_inicializaCampos() {
        TipoHabitacion tipo = new TipoHabitacion(1, "DBL-STD", "Doble Estándar",
                "Habitación doble con cama de matrimonio", 2, new BigDecimal("95.00"));

        assertEquals(1,                        tipo.getId());
        assertEquals("DBL-STD",                tipo.getCodigo());
        assertEquals("Doble Estándar",         tipo.getNombre());
        assertEquals("Habitación doble con cama de matrimonio", tipo.getDescripcion());
        assertEquals(2,                        tipo.getCapacidadMaxima());
        assertEquals(0, new BigDecimal("95.00").compareTo(tipo.getPrecioBase()));
    }

    @Test
    @DisplayName("Constructor vacío crea tipo sin datos")
    void constructor_vacio() {
        TipoHabitacion tipo = new TipoHabitacion();
        assertNull(tipo.getCodigo());
        assertNull(tipo.getNombre());
        assertEquals(0, tipo.getId());
    }

    // ── toString

    @Test
    @DisplayName("toString devuelve el nombre del tipo")
    void toString_devuelveNombre() {
        TipoHabitacion tipo = new TipoHabitacion();
        tipo.setNombre("Suite Presidencial");
        assertEquals("Suite Presidencial", tipo.toString());
    }

    // ── setters y getters

    @Test
    @DisplayName("setPrecioBase y getPrecioBase funcionan correctamente")
    void setGetPrecioBase() {
        TipoHabitacion tipo = new TipoHabitacion();
        tipo.setPrecioBase(new BigDecimal("120.50"));
        assertEquals(0, new BigDecimal("120.50").compareTo(tipo.getPrecioBase()));
    }

    @Test
    @DisplayName("setCapacidadMaxima y getCapacidadMaxima funcionan correctamente")
    void setGetCapacidadMaxima() {
        TipoHabitacion tipo = new TipoHabitacion();
        tipo.setCapacidadMaxima(4);
        assertEquals(4, tipo.getCapacidadMaxima());
    }

    @Test
    @DisplayName("setAmenidades y getAmenidades funcionan correctamente")
    void setGetAmenidades() {
        TipoHabitacion tipo = new TipoHabitacion();
        tipo.setAmenidades("WiFi, TV, Minibar, Jacuzzi");
        assertEquals("WiFi, TV, Minibar, Jacuzzi", tipo.getAmenidades());
    }

    @Test
    @DisplayName("setCodigo y getCodigo funcionan correctamente")
    void setGetCodigo() {
        TipoHabitacion tipo = new TipoHabitacion();
        tipo.setCodigo("STE-PRE");
        assertEquals("STE-PRE", tipo.getCodigo());
    }
}

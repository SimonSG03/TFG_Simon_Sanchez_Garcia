package es.simonsg.pmsuite.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Habitacion")
class HabitacionTest {

    // ── Constructor completo

    @Test
    @DisplayName("Constructor inicializa todos los campos correctamente")
    void constructor_inicializaCampos() {
        TipoHabitacion tipo = new TipoHabitacion(1, "DBL", "Doble Estándar", "", 2, new BigDecimal("90.00"));
        Habitacion h = new Habitacion(10, "201", 2, tipo, Habitacion.Estado.DISPONIBLE);

        assertEquals(10,                        h.getId());
        assertEquals("201",                     h.getNumero());
        assertEquals(2,                         h.getPlanta());
        assertEquals(tipo,                      h.getTipoHabitacion());
        assertEquals(Habitacion.Estado.DISPONIBLE, h.getEstado());
    }

    @Test
    @DisplayName("Constructor vacío crea habitación sin datos")
    void constructor_vacio() {
        Habitacion h = new Habitacion();
        assertNull(h.getNumero());
        assertNull(h.getEstado());
        assertEquals(0, h.getId());
    }

    // ── Estado enum

    @Test
    @DisplayName("Estado.desdeBD mapea AVAILABLE a DISPONIBLE")
    void estado_desdeBD_available() {
        assertEquals(Habitacion.Estado.DISPONIBLE, Habitacion.Estado.desdeBD("AVAILABLE"));
    }

    @Test
    @DisplayName("Estado.desdeBD mapea OCCUPIED a OCUPADA")
    void estado_desdeBD_occupied() {
        assertEquals(Habitacion.Estado.OCUPADA, Habitacion.Estado.desdeBD("OCCUPIED"));
    }

    @Test
    @DisplayName("Estado.desdeBD mapea OUT_OF_SERVICE a FUERA_SERVICIO")
    void estado_desdeBD_outOfService() {
        assertEquals(Habitacion.Estado.FUERA_SERVICIO, Habitacion.Estado.desdeBD("OUT_OF_SERVICE"));
    }

    @Test
    @DisplayName("Estado.desdeBD mapea BLOCKED a BLOQUEADA")
    void estado_desdeBD_blocked() {
        assertEquals(Habitacion.Estado.BLOQUEADA, Habitacion.Estado.desdeBD("BLOCKED"));
    }

    @Test
    @DisplayName("Estado.desdeBD mapea CLEANING a LIMPIEZA")
    void estado_desdeBD_cleaning() {
        assertEquals(Habitacion.Estado.LIMPIEZA, Habitacion.Estado.desdeBD("CLEANING"));
    }

    @Test
    @DisplayName("Estado.desdeBD lanza excepción con valor desconocido")
    void estado_desdeBD_valorDesconocido() {
        assertThrows(IllegalArgumentException.class, () -> Habitacion.Estado.desdeBD("UNKNOWN"));
    }

    @Test
    @DisplayName("Estado.valorBD de OCUPADA es OCCUPIED")
    void estado_valorBD_ocupada() {
        assertEquals("OCCUPIED", Habitacion.Estado.OCUPADA.valorBD);
    }

    @Test
    @DisplayName("Estado.toSpanish de FUERA_SERVICIO devuelve 'Fuera de servicio'")
    void estado_toSpanish_fueraServicio() {
        assertEquals("Fuera de servicio", Habitacion.Estado.FUERA_SERVICIO.toSpanish());
    }

    @Test
    @DisplayName("Estado.toSpanish de LIMPIEZA devuelve 'Limpieza'")
    void estado_toSpanish_limpieza() {
        assertEquals("Limpieza", Habitacion.Estado.LIMPIEZA.toSpanish());
    }

    // ── toString

    @Test
    @DisplayName("toString incluye número y nombre del tipo")
    void toString_conTipo() {
        TipoHabitacion tipo = new TipoHabitacion(1, "SGL", "Individual", "", 1, new BigDecimal("60.00"));
        Habitacion h = new Habitacion(1, "101", 1, tipo, Habitacion.Estado.DISPONIBLE);
        assertEquals("Hab. 101 - Individual", h.toString());
    }

    @Test
    @DisplayName("toString sin tipo solo muestra número")
    void toString_sinTipo() {
        Habitacion h = new Habitacion(2, "102", 1, null, Habitacion.Estado.DISPONIBLE);
        assertEquals("Hab. 102", h.toString());
    }

    // ── setters y getters

    @Test
    @DisplayName("setEstado y getEstado funcionan correctamente")
    void setGetEstado() {
        Habitacion h = new Habitacion();
        h.setEstado(Habitacion.Estado.LIMPIEZA);
        assertEquals(Habitacion.Estado.LIMPIEZA, h.getEstado());
    }

    @Test
    @DisplayName("setNotas y getNotas funcionan correctamente")
    void setGetNotas() {
        Habitacion h = new Habitacion();
        h.setNotas("Vista al mar");
        assertEquals("Vista al mar", h.getNotas());
    }
}

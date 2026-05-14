package es.simonsg.pmsuite.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Reserva")
class ReservaTest {

    private Reserva reserva;

    @BeforeEach
    void setUp() {
        reserva = new Reserva();
    }

    // ── getNoches

    @Test
    @DisplayName("getNoches devuelve 1 para estancia de una noche")
    void getNoches_unaNoches() {
        reserva.setFechaEntrada(LocalDate.of(2026, 5, 10));
        reserva.setFechaSalida(LocalDate.of(2026, 5, 11));
        assertEquals(1, reserva.getNoches());
    }

    @Test
    @DisplayName("getNoches devuelve 7 para estancia de una semana")
    void getNoches_sieteDias() {
        reserva.setFechaEntrada(LocalDate.of(2026, 6, 1));
        reserva.setFechaSalida(LocalDate.of(2026, 6, 8));
        assertEquals(7, reserva.getNoches());
    }

    @Test
    @DisplayName("getNoches devuelve 30 para estancia de un mes")
    void getNoches_treintaDias() {
        reserva.setFechaEntrada(LocalDate.of(2026, 1, 1));
        reserva.setFechaSalida(LocalDate.of(2026, 1, 31));
        assertEquals(30, reserva.getNoches());
    }

    @Test
    @DisplayName("getNoches devuelve 0 cuando fechaEntrada equals fechaSalida")
    void getNoches_fechasIguales() {
        LocalDate hoy = LocalDate.of(2026, 5, 13);
        reserva.setFechaEntrada(hoy);
        reserva.setFechaSalida(hoy);
        assertEquals(0, reserva.getNoches());
    }

    @Test
    @DisplayName("getNoches devuelve 0 cuando las fechas son null")
    void getNoches_fechasNull() {
        assertEquals(0, reserva.getNoches());
    }

    @Test
    @DisplayName("getNoches devuelve 0 cuando solo fechaEntrada es null")
    void getNoches_soloEntradaNull() {
        reserva.setFechaSalida(LocalDate.of(2026, 5, 15));
        assertEquals(0, reserva.getNoches());
    }

    // ── Estado enum
    @Test
    @DisplayName("Estado.desdeBD mapea PENDING a PENDIENTE")
    void estado_desdeBD_pending() {
        assertEquals(Reserva.Estado.PENDIENTE, Reserva.Estado.desdeBD("PENDING"));
    }

    @Test
    @DisplayName("Estado.desdeBD mapea CONFIRMED a CONFIRMADA")
    void estado_desdeBD_confirmed() {
        assertEquals(Reserva.Estado.CONFIRMADA, Reserva.Estado.desdeBD("CONFIRMED"));
    }

    @Test
    @DisplayName("Estado.desdeBD mapea CHECKED_IN a REGISTRADA")
    void estado_desdeBD_checkedIn() {
        assertEquals(Reserva.Estado.REGISTRADA, Reserva.Estado.desdeBD("CHECKED_IN"));
    }

    @Test
    @DisplayName("Estado.desdeBD mapea CHECKED_OUT a CHECKOUT_REALIZADO")
    void estado_desdeBD_checkedOut() {
        assertEquals(Reserva.Estado.CHECKOUT_REALIZADO, Reserva.Estado.desdeBD("CHECKED_OUT"));
    }

    @Test
    @DisplayName("Estado.desdeBD mapea CANCELLED a CANCELADA")
    void estado_desdeBD_cancelled() {
        assertEquals(Reserva.Estado.CANCELADA, Reserva.Estado.desdeBD("CANCELLED"));
    }

    @Test
    @DisplayName("Estado.desdeBD mapea NO_SHOW a NO_PRESENTADO")
    void estado_desdeBD_noShow() {
        assertEquals(Reserva.Estado.NO_PRESENTADO, Reserva.Estado.desdeBD("NO_SHOW"));
    }

    @Test
    @DisplayName("Estado.desdeBD lanza excepción con valor desconocido")
    void estado_desdeBD_valorDesconocido() {
        assertThrows(IllegalArgumentException.class, () -> Reserva.Estado.desdeBD("INVALIDO"));
    }

    @Test
    @DisplayName("Estado.valorBD de REGISTRADA es CHECKED_IN")
    void estado_valorBD_registrada() {
        assertEquals("CHECKED_IN", Reserva.Estado.REGISTRADA.valorBD);
    }

    @Test
    @DisplayName("Estado.toSpanish de REGISTRADA devuelve 'En curso'")
    void estado_toSpanish_registrada() {
        assertEquals("En curso", Reserva.Estado.REGISTRADA.toSpanish());
    }

    @Test
    @DisplayName("Estado.toSpanish de CHECKOUT_REALIZADO devuelve 'Completada'")
    void estado_toSpanish_checkoutRealizado() {
        assertEquals("Completada", Reserva.Estado.CHECKOUT_REALIZADO.toSpanish());
    }

    // ── toString

    @Test
    @DisplayName("toString devuelve número y nombre del huésped")
    void toString_conHuesped() {
        Huesped h = new Huesped(1, "12345678A", "Ana", "García López", "ana@mail.com", "600000001");
        reserva.setNumeroReserva("RES-20260513-0001");
        reserva.setHuesped(h);
        assertEquals("RES-20260513-0001 - Ana García López", reserva.toString());
    }

    @Test
    @DisplayName("toString indica 'Sin huésped' cuando el huésped es null")
    void toString_sinHuesped() {
        reserva.setNumeroReserva("RES-20260513-0002");
        assertEquals("RES-20260513-0002 - Sin huésped", reserva.toString());
    }

    // ── getters / setters

    @Test
    @DisplayName("setAdultos y getAdultos funcionan correctamente")
    void setGetAdultos() {
        reserva.setAdultos(2);
        assertEquals(2, reserva.getAdultos());
    }

    @Test
    @DisplayName("setPrecioTotal y getPrecioTotal funcionan correctamente")
    void setGetPrecioTotal() {
        BigDecimal precio = new BigDecimal("150.00");
        reserva.setPrecioTotal(precio);
        assertEquals(0, precio.compareTo(reserva.getPrecioTotal()));
    }

    @Test
    @DisplayName("setEstado y getEstado funcionan correctamente")
    void setGetEstado() {
        reserva.setEstado(Reserva.Estado.CONFIRMADA);
        assertEquals(Reserva.Estado.CONFIRMADA, reserva.getEstado());
    }
}

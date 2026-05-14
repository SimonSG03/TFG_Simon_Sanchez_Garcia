package es.simonsg.pmsuite.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Huesped")
class HuespedTest {

    // ── Constructor completo
    @Test
    @DisplayName("Constructor inicializa todos los campos correctamente")
    void constructor_inicializaCampos() {
        Huesped h = new Huesped(5, "12345678A", "Pedro", "Martínez Ruiz", "pedro@mail.com", "600100200");
        assertEquals(5,              h.getId());
        assertEquals("12345678A",    h.getNif());
        assertEquals("Pedro",        h.getNombre());
        assertEquals("Martínez Ruiz",h.getApellidos());
        assertEquals("pedro@mail.com", h.getEmail());
        assertEquals("600100200",    h.getTelefono());
    }

    @Test
    @DisplayName("Constructor vacío crea huésped sin datos")
    void constructor_vacio() {
        Huesped h = new Huesped();
        assertNull(h.getNombre());
        assertNull(h.getApellidos());
        assertEquals(0, h.getId());
    }

    // ── getNombreCompleto
    @Test
    @DisplayName("getNombreCompleto concatena nombre y apellidos con espacio")
    void getNombreCompleto_normal() {
        Huesped h = new Huesped();
        h.setNombre("Laura");
        h.setApellidos("Sánchez Pérez");
        assertEquals("Laura Sánchez Pérez", h.getNombreCompleto());
    }

    @Test
    @DisplayName("getNombreCompleto funciona con nombre simple y un apellido")
    void getNombreCompleto_nombreSimple() {
        Huesped h = new Huesped();
        h.setNombre("John");
        h.setApellidos("Smith");
        assertEquals("John Smith", h.getNombreCompleto());
    }

    // ── toString
    @Test
    @DisplayName("toString devuelve el nombre completo")
    void toString_devuelveNombreCompleto() {
        Huesped h = new Huesped();
        h.setNombre("Carlos");
        h.setApellidos("Díaz López");
        assertEquals("Carlos Díaz López", h.toString());
    }

    // ── setters y getters

    @Test
    @DisplayName("setNacionalidad y getNacionalidad funcionan correctamente")
    void setGetNacionalidad() {
        Huesped h = new Huesped();
        h.setNacionalidad("Española");
        assertEquals("Española", h.getNacionalidad());
    }

    @Test
    @DisplayName("setFechaNacimiento y getFechaNacimiento funcionan correctamente")
    void setGetFechaNacimiento() {
        Huesped h = new Huesped();
        LocalDate nacimiento = LocalDate.of(1990, 3, 25);
        h.setFechaNacimiento(nacimiento);
        assertEquals(nacimiento, h.getFechaNacimiento());
    }

    @Test
    @DisplayName("setNotas y getNotas funcionan correctamente")
    void setGetNotas() {
        Huesped h = new Huesped();
        h.setNotas("Cliente VIP");
        assertEquals("Cliente VIP", h.getNotas());
    }

    @Test
    @DisplayName("setEmail y getEmail funcionan correctamente")
    void setGetEmail() {
        Huesped h = new Huesped();
        h.setEmail("test@hotel.com");
        assertEquals("test@hotel.com", h.getEmail());
    }
}

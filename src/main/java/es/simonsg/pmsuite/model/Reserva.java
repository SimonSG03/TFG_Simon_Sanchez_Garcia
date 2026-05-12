package es.simonsg.pmsuite.model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

public class Reserva {

    public enum Estado {
        PENDIENTE("PENDING"), CONFIRMADA("CONFIRMED"), REGISTRADA("CHECKED_IN"),
        CHECKOUT_REALIZADO("CHECKED_OUT"), CANCELADA("CANCELLED"), NO_PRESENTADO("NO_SHOW");

        public final String valorBD;
        Estado(String valorBD) { this.valorBD = valorBD; }

        public static Estado desdeBD(String v) {
            for (Estado e : values()) if (e.valorBD.equals(v)) return e;
            throw new IllegalArgumentException("Estado desconocido: " + v);
        }

        public String toSpanish() {
            return switch (this) {
                case PENDIENTE          -> "Pendiente";
                case CONFIRMADA         -> "Confirmada";
                case REGISTRADA         -> "En curso";
                case CHECKOUT_REALIZADO -> "Completada";
                case CANCELADA          -> "Cancelada";
                case NO_PRESENTADO      -> "No presentado";
            };
        }
    }

    private int id;
    private String numeroReserva;
    private Habitacion habitacion;
    private Huesped huesped;
    private LocalDate fechaEntrada;
    private LocalDate fechaSalida;
    private int adultos;
    private int ninos;
    private Estado estado;
    private BigDecimal precioTotal;
    private BigDecimal depositoPagado;
    private String peticionesEspeciales;
    private String notas;
    private LocalDateTime fechaCreacion;
    private LocalDateTime fechaActualizacion;

    public Reserva() {}

    public long getNoches() {
        if (fechaEntrada != null && fechaSalida != null) {
            return ChronoUnit.DAYS.between(fechaEntrada, fechaSalida);
        }
        return 0;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getNumeroReserva() { return numeroReserva; }
    public void setNumeroReserva(String numeroReserva) { this.numeroReserva = numeroReserva; }

    public Habitacion getHabitacion() { return habitacion; }
    public void setHabitacion(Habitacion habitacion) { this.habitacion = habitacion; }

    public Huesped getHuesped() { return huesped; }
    public void setHuesped(Huesped huesped) { this.huesped = huesped; }

    public LocalDate getFechaEntrada() { return fechaEntrada; }
    public void setFechaEntrada(LocalDate fechaEntrada) { this.fechaEntrada = fechaEntrada; }

    public LocalDate getFechaSalida() { return fechaSalida; }
    public void setFechaSalida(LocalDate fechaSalida) { this.fechaSalida = fechaSalida; }

    public int getAdultos() { return adultos; }
    public void setAdultos(int adultos) { this.adultos = adultos; }

    public int getNinos() { return ninos; }
    public void setNinos(int ninos) { this.ninos = ninos; }

    public Estado getEstado() { return estado; }
    public void setEstado(Estado estado) { this.estado = estado; }

    public BigDecimal getPrecioTotal() { return precioTotal; }
    public void setPrecioTotal(BigDecimal precioTotal) { this.precioTotal = precioTotal; }

    public BigDecimal getDepositoPagado() { return depositoPagado; }
    public void setDepositoPagado(BigDecimal depositoPagado) { this.depositoPagado = depositoPagado; }

    public String getPeticionesEspeciales() { return peticionesEspeciales; }
    public void setPeticionesEspeciales(String peticionesEspeciales) { this.peticionesEspeciales = peticionesEspeciales; }

    public String getNotas() { return notas; }
    public void setNotas(String notas) { this.notas = notas; }

    public LocalDateTime getFechaCreacion() { return fechaCreacion; }
    public void setFechaCreacion(LocalDateTime fechaCreacion) { this.fechaCreacion = fechaCreacion; }

    public LocalDateTime getFechaActualizacion() { return fechaActualizacion; }
    public void setFechaActualizacion(LocalDateTime fechaActualizacion) { this.fechaActualizacion = fechaActualizacion; }

    @Override
    public String toString() {
        return numeroReserva + " - " + (huesped != null ? huesped.getNombreCompleto() : "Sin huésped");
    }
}

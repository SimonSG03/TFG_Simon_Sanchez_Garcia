package es.simonsg.pmsuite.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class PedidoRestaurante {

    public enum Estado {
        PENDIENTE("PENDING"), PREPARANDO("PREPARING"), ENTREGADO("DELIVERED"), CANCELADO("CANCELLED");

        public final String valorBD;
        Estado(String valorBD) { this.valorBD = valorBD; }

        public static Estado desdeBD(String v) {
            for (Estado e : values()) if (e.valorBD.equals(v)) return e;
            throw new IllegalArgumentException("Estado desconocido: " + v);
        }
    }

    private int id;
    private Reserva reserva;
    private Habitacion habitacion;
    private String numeroMesa;
    private Estado estado;
    private String notas;
    private String metodoPago;
    private Usuario creadoPor;
    private LocalDateTime fechaCreacion;
    private LocalDateTime fechaEntrega;
    private List<LineaPedido> lineas = new ArrayList<>();
    private BigDecimal totalCalculado;

    public BigDecimal getTotal() {
        if (!lineas.isEmpty()) {
            return lineas.stream().map(LineaPedido::getTotal).reduce(BigDecimal.ZERO, BigDecimal::add);
        }
        return totalCalculado != null ? totalCalculado : BigDecimal.ZERO;
    }

    public int getId()                      { return id; }
    public void setId(int id)               { this.id = id; }
    public Reserva getReserva()             { return reserva; }
    public void setReserva(Reserva r)       { this.reserva = r; }
    public Habitacion getHabitacion()       { return habitacion; }
    public void setHabitacion(Habitacion r) { this.habitacion = r; }
    public String getNumeroMesa()           { return numeroMesa; }
    public void setNumeroMesa(String t)     { this.numeroMesa = t; }
    public Estado getEstado()               { return estado; }
    public void setEstado(Estado s)         { this.estado = s; }
    public String getNotas()                { return notas; }
    public void setNotas(String n)          { this.notas = n; }
    public String getMetodoPago()           { return metodoPago; }
    public void setMetodoPago(String p)     { this.metodoPago = p; }
    public Usuario getCreadoPor()           { return creadoPor; }
    public void setCreadoPor(Usuario u)     { this.creadoPor = u; }
    public LocalDateTime getFechaCreacion() { return fechaCreacion; }
    public void setFechaCreacion(LocalDateTime t) { this.fechaCreacion = t; }
    public LocalDateTime getFechaEntrega()  { return fechaEntrega; }
    public void setFechaEntrega(LocalDateTime t) { this.fechaEntrega = t; }
    public List<LineaPedido> getLineas()    { return lineas; }
    public void setLineas(List<LineaPedido> l) { this.lineas = l; }
    public BigDecimal getTotalCalculado()   { return totalCalculado; }
    public void setTotalCalculado(BigDecimal t) { this.totalCalculado = t; }

    @Override public String toString() {
        return numeroMesa != null ? numeroMesa : ("Pedido #" + id);
    }
}

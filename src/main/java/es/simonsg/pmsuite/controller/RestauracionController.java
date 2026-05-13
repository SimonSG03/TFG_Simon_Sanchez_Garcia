package es.simonsg.pmsuite.controller;

import es.simonsg.pmsuite.dao.*;
import es.simonsg.pmsuite.model.*;
import es.simonsg.pmsuite.model.ArticuloMenu;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.*;
import javafx.scene.layout.*;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class RestauracionController {

    private IndexController indexController;
    public void setIndexController(IndexController ic) { this.indexController = ic; }

    // ── Raíz y vistas
    @FXML private StackPane rootStack;
    @FXML private BorderPane vistaTMS;
    @FXML private BorderPane vistaAdmin;

    // ── TMS — barra superior
    @FXML private Label lblMesaActual;
    @FXML private ComboBox<Usuario> cmbCamarero;

    // ── TMS — ticket panel (izquierda)
    @FXML private Label lblTicketMesa;
    @FXML private TableView<LineaPedido> tblTicket;
    @FXML private TableColumn<LineaPedido, String> colTkCant;
    @FXML private TableColumn<LineaPedido, String> colTkDesc;
    @FXML private TableColumn<LineaPedido, String> colTkPrecio;
    @FXML private TableColumn<LineaPedido, String> colTkTotal;
    @FXML private Label lblTicketTotal;
    @FXML private Button btnCobrar;
    @FXML private Button btnCargoHab;
    @FXML private Button btnEliminarLinea;
    @FXML private Button btnLimpiarTicket;

    // ── TMS — panel mesas (arriba derecha)
    @FXML private HBox zonaTabBar;
    @FXML private FlowPane panelMesas;

    // ── TMS — panel productos (abajo derecha)
    @FXML private FlowPane panelFamilias;
    @FXML private FlowPane panelProductos;
    @FXML private Label lblFamiliaActual;

    // ── Admin — sidebar
    @FXML private VBox pnlCategorias;
    @FXML private VBox pnlProductosAdmin;
    @FXML private VBox pnlPedidos;
    @FXML private VBox pnlHistorial;

    @FXML private Button btnNavCat;
    @FXML private Button btnNavProd;
    @FXML private Button btnNavPedidos;
    @FXML private Button btnNavHistorial;

    // ── Admin — Categorías
    @FXML private TableView<CategoriaMenu> tblCat;
    @FXML private TableColumn<CategoriaMenu, String> colCatNombre;
    @FXML private TableColumn<CategoriaMenu, String> colCatActiva;
    @FXML private TextField txtCatNombre;
    @FXML private TextField txtCatDesc;
    @FXML private TextField txtCatColor;
    @FXML private CheckBox chkCatActiva;

    // ── Admin — Productos
    @FXML private TableView<ArticuloMenu> tblProd;
    @FXML private TableColumn<ArticuloMenu, String> colProdNombre;
    @FXML private TableColumn<ArticuloMenu, String> colProdCat;
    @FXML private TableColumn<ArticuloMenu, String> colProdPrecio;
    @FXML private TableColumn<ArticuloMenu, String> colProdDisp;
    @FXML private ComboBox<CategoriaMenu> cmbProdCat;
    @FXML private TextField txtProdNombre;
    @FXML private TextField txtProdDesc;
    @FXML private TextField txtProdPrecio;
    @FXML private TextField txtProdAlerg;
    @FXML private CheckBox chkProdDisp;

    // ── Admin — Pedidos activos
    @FXML private TableView<PedidoRestaurante> tblPedidos;
    @FXML private TableColumn<PedidoRestaurante, String> colPedMesa;
    @FXML private TableColumn<PedidoRestaurante, String> colPedEstado;
    @FXML private TableColumn<PedidoRestaurante, String> colPedHora;
    @FXML private TableColumn<PedidoRestaurante, String> colPedTotal;

    // ── Admin — Historial
    @FXML private DatePicker dpHistFecha;
    @FXML private TableView<PedidoRestaurante> tblHistorial;
    @FXML private TableColumn<PedidoRestaurante, String> colHistMesa;
    @FXML private TableColumn<PedidoRestaurante, String> colHistTurno;
    @FXML private TableColumn<PedidoRestaurante, String> colHistEstado;
    @FXML private TableColumn<PedidoRestaurante, String> colHistHora;
    @FXML private TableColumn<PedidoRestaurante, String> colHistTotal;
    @FXML private Label lblArqueoTotalPedidos;
    @FXML private Label lblArqueoTotal;
    @FXML private Label lblArqueoEfectivo;
    @FXML private Label lblArqueoTarjeta;

    // ── Admin — Mesas
    @FXML private VBox pnlMesasAdmin;
    @FXML private Button btnNavMesas;
    @FXML private HBox mapaZonaTabs;
    @FXML private Pane panelMapaMesas;
    @FXML private TextField txtNuevaMesaNombre;
    @FXML private TextField txtNuevaMesaCap;
    @FXML private Label lblMesaSelec;

    // ── Estado TMS
    private String zonaActual = "BARRA";
    private String mesaActual = null;
    private PedidoRestaurante pedidoActual = null;
    private final ObservableList<LineaPedido> lineasTicket = FXCollections.observableArrayList();
    private final Map<String, List<MesaRestaurante>> tablesByZone = new LinkedHashMap<>();

    // ── Estado mapa de mesas
    private String zonaMapaActual = "BARRA";
    private MesaRestaurante mesaSeleccionadaMapa = null;

    private final DateTimeFormatter timeFmt = DateTimeFormatter.ofPattern("HH:mm");
    private final DateTimeFormatter dateFmt = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final List<String> ZONAS = List.of("BARRA", "SALÓN", "TERRAZA");


    // INITIALIZE

    @FXML
    public void initialize() {
        setupTicketTable();
        mostrarVistaTMS();
        cargarTablasDesdeBD();
        cargarCamareros();
        construirZonaTabs();
        cambiarZona("BARRA");
        cargarFamilias();
        setupAdminTables();
    }

    private void cargarTablasDesdeBD() {
        tablesByZone.clear();
        ZONAS.forEach(z -> tablesByZone.put(z, new ArrayList<>()));
        try {
            MesaRestauranteDAO.obtenerTodas().forEach(t -> {
                tablesByZone.computeIfAbsent(t.getZona(), k -> new ArrayList<>()).add(t);
            });
        } catch (SQLException e) {
            e.printStackTrace();
        }
        // Fallback: si no hay tablas en BD, usa valores por defecto
        if (tablesByZone.values().stream().allMatch(List::isEmpty)) {
            tablesByZone.put("BARRA",   fallbackTables("BARRA",   "BARRA ",    10, 2));
            tablesByZone.put("SALÓN",   fallbackTables("SALÓN",   "MESA ",     10, 4));
            tablesByZone.put("TERRAZA", fallbackTables("TERRAZA", "TERRAZA ",  10, 4));
        }
    }

    private List<MesaRestaurante> fallbackTables(String zone, String prefix, int count, int cap) {
        List<MesaRestaurante> list = new ArrayList<>();
        for (int i = 1; i <= count; i++) {
            MesaRestaurante t = new MesaRestaurante();
            t.setZona(zone); t.setNombre(prefix + i);
            t.setCapacidad(cap); t.setOrden(i);
            t.setPosX(10 + ((i-1) % 5) * 110.0);
            t.setPosY(10 + ((i-1) / 5) * 90.0);
            list.add(t);
        }
        return list;
    }


    // NAVEGACIÓN ENTRE VISTAS

    @FXML private void mostrarVistaTMS() {
        vistaTMS.setVisible(true);
        vistaTMS.setManaged(true);
        vistaAdmin.setVisible(false);
        vistaAdmin.setManaged(false);
    }

    @FXML private void mostrarVistaAdmin() {
        vistaAdmin.setVisible(true);
        vistaAdmin.setManaged(true);
        vistaTMS.setVisible(false);
        vistaTMS.setManaged(false);
        cargarCategorias();
        navAdmin(pnlCategorias, btnNavCat);
    }

    @FXML private void volverInicio() {
        if (indexController != null) indexController.mostrarInicio();
    }


    // TMS — SETUP

    private void setupTicketTable() {
        colTkCant.setCellValueFactory(c -> new SimpleStringProperty(String.valueOf(c.getValue().getCantidad())));
        colTkDesc.setCellValueFactory(c -> {
            LineaPedido l = c.getValue();
            String name = l.getArticulo() != null ? l.getArticulo().getNombre() : "?";
            return new SimpleStringProperty(name);
        });
        colTkPrecio.setCellValueFactory(c -> {
            BigDecimal p = c.getValue().getPrecioUnitario();
            return new SimpleStringProperty(p != null ? p.setScale(2, RoundingMode.HALF_UP) + " €" : "");
        });
        colTkTotal.setCellValueFactory(c -> {
            BigDecimal t = c.getValue().getTotal();
            return new SimpleStringProperty(t != null ? t.setScale(2, RoundingMode.HALF_UP) + " €" : "");
        });
        tblTicket.setItems(lineasTicket);
        tblTicket.setPlaceholder(new Label("Sin artículos"));
    }

    private void cargarCamareros() {
        try {
            List<Usuario> usuarios = UsuarioDAO.obtenerTodos().stream()
                    .filter(u -> u.getRol() == Usuario.Rol.RECEPCIONISTA
                            || u.getRol() == Usuario.Rol.RESTAURACION
                            || u.getRol() == Usuario.Rol.ADMIN
                            || u.getRol() == Usuario.Rol.GERENTE)
                    .toList();
            cmbCamarero.setItems(FXCollections.observableArrayList(usuarios));
            if (!usuarios.isEmpty()) cmbCamarero.setValue(usuarios.get(0));
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }


    // TMS — ZONAS Y MESAS

    private void construirZonaTabs() {
        zonaTabBar.getChildren().clear();
        ToggleGroup tg = new ToggleGroup();
        tablesByZone.keySet().forEach(zona -> {
            ToggleButton tb = new ToggleButton(zona);
            tb.getStyleClass().add("zona-tab");
            tb.setToggleGroup(tg);
            tb.setOnAction(e -> cambiarZona(zona));
            if (zona.equals(zonaActual)) tb.setSelected(true);
            zonaTabBar.getChildren().add(tb);
        });
    }

    private void cambiarZona(String zona) {
        zonaActual = zona;
        zonaTabBar.getChildren().forEach(n -> {
            if (n instanceof ToggleButton tb) tb.setSelected(tb.getText().equals(zona));
        });
        renderizarMesas();
    }

    private void renderizarMesas() {
        panelMesas.getChildren().clear();
        List<MesaRestaurante> mesas = tablesByZone.getOrDefault(zonaActual, List.of());

        Set<String> mesasOcupadas = new HashSet<>();
        try {
            PedidoRestauranteDAO.getAbiertos().forEach(o -> {
                if (o.getNumeroMesa() != null) mesasOcupadas.add(o.getNumeroMesa());
            });
        } catch (SQLException e) { e.printStackTrace(); }

        mesas.forEach(t -> {
            boolean ocupada = mesasOcupadas.contains(t.getNombre());
            boolean activa  = t.getNombre().equals(mesaActual);

            Button btn = new Button(t.getNombre() + "\n👥 " + t.getCapacidad());
            btn.setPrefWidth(100);
            btn.setPrefHeight(60);
            btn.setWrapText(true);
            btn.setAlignment(Pos.CENTER);

            if (activa)        btn.getStyleClass().add("mesa-btn-activa");
            else if (ocupada)  btn.getStyleClass().add("mesa-btn-ocupada");
            else               btn.getStyleClass().add("mesa-btn-libre");

            btn.setOnAction(e -> seleccionarMesa(t.getNombre()));
            panelMesas.getChildren().add(btn);
        });
    }

    private void seleccionarMesa(String mesa) {
        mesaActual = mesa;
        lblMesaActual.setText("Mesa: " + mesa);
        lblTicketMesa.setText("🧾  " + mesa);

        try {
            pedidoActual = PedidoRestauranteDAO.getPorMesa(mesa);
            if (pedidoActual != null) {
                List<LineaPedido> lines = PedidoRestauranteDAO.getLineas(pedidoActual.getId());
                lineasTicket.setAll(lines);
            } else {
                lineasTicket.clear();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        actualizarTotalTicket();
        renderizarMesas();
    }


    // TMS — FAMILIAS Y PRODUCTOS

    private void cargarFamilias() {
        panelFamilias.getChildren().clear();
        try {
            List<CategoriaMenu> cats = CategoriaMenuDAO.obtenerActivas();
            cats.forEach(cat -> {
                Button btn = new Button(cat.getNombre());
                btn.setPrefWidth(120);
                btn.setPrefHeight(70);
                btn.setWrapText(true);
                btn.setAlignment(Pos.CENTER);
                btn.getStyleClass().add("familia-btn");
                String color = cat.getColor() != null && !cat.getColor().isBlank()
                        ? cat.getColor() : "#607D8B";
                btn.setStyle("-fx-background-color:" + color + "; -fx-text-fill: white; " +
                        "-fx-font-weight: bold; -fx-font-size: 12px;");
                btn.setOnAction(e -> cargarProductosDeFamilia(cat));
                panelFamilias.getChildren().add(btn);
            });
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void cargarProductosDeFamilia(CategoriaMenu cat) {
        panelProductos.getChildren().clear();
        if (lblFamiliaActual != null) lblFamiliaActual.setText(cat.getNombre());
        try {
            List<ArticuloMenu> items = ArticuloMenuDAO.obtenerPorCategoria(cat.getId());
            items.forEach(item -> {
                Button btn = new Button(item.getNombre() + "\n" + item.getPrecio().setScale(2, RoundingMode.HALF_UP) + " €");
                btn.setPrefWidth(110);
                btn.setPrefHeight(65);
                btn.setWrapText(true);
                btn.setAlignment(Pos.CENTER);
                btn.getStyleClass().add("producto-btn");
                btn.setOnAction(e -> agregarArticulo(item));
                panelProductos.getChildren().add(btn);
            });
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void agregarArticulo(ArticuloMenu item) {
        if (mesaActual == null) {
            warn("Selecciona una mesa primero.");
            return;
        }
        try {
            if (pedidoActual == null) {
                PedidoRestaurante nuevo = new PedidoRestaurante();
                nuevo.setNumeroMesa(mesaActual);
                nuevo.setEstado(PedidoRestaurante.Estado.PENDIENTE);
                nuevo.setCreadoPor(cmbCamarero.getValue());
                int id = PedidoRestauranteDAO.crear(nuevo);
                nuevo.setId(id);
                pedidoActual = nuevo;
            }
            // Agrupa línea existente si mismo producto
            Optional<LineaPedido> existente = lineasTicket.stream()
                    .filter(l -> l.getArticulo() != null && l.getArticulo().getId() == item.getId())
                    .findFirst();
            if (existente.isPresent()) {
                LineaPedido l = existente.get();
                PedidoRestauranteDAO.eliminarLinea(l.getId());
                l.setCantidad(l.getCantidad() + 1);
                int newId = PedidoRestauranteDAO.añadirLinea(l);
                l.setId(newId);
            } else {
                LineaPedido l = new LineaPedido(pedidoActual.getId(), item, 1, item.getPrecio());
                int newId = PedidoRestauranteDAO.añadirLinea(l);
                l.setId(newId);
                lineasTicket.add(l);
            }
            tblTicket.refresh();
            actualizarTotalTicket();
            renderizarMesas();
        } catch (SQLException e) {
            e.printStackTrace();
            error("Error añadiendo artículo", e);
        }
    }

    @FXML private void eliminarLineaTicket() {
        LineaPedido sel = tblTicket.getSelectionModel().getSelectedItem();
        if (sel == null) return;
        try {
            PedidoRestauranteDAO.eliminarLinea(sel.getId());
            lineasTicket.remove(sel);
            actualizarTotalTicket();
            if (lineasTicket.isEmpty() && pedidoActual != null) {
                PedidoRestauranteDAO.actualizarEstado(pedidoActual.getId(), PedidoRestaurante.Estado.CANCELADO);
                pedidoActual = null;
                renderizarMesas();
            }
        } catch (SQLException e) {
            error("Error eliminando línea", e);
        }
    }

    @FXML private void limpiarTicket() {
        if (pedidoActual == null) return;
        if (!confirm("¿Cancelar el pedido completo de " + mesaActual + "?")) return;
        try {
            PedidoRestauranteDAO.actualizarEstado(pedidoActual.getId(), PedidoRestaurante.Estado.CANCELADO);
            lineasTicket.clear();
            pedidoActual = null;
            actualizarTotalTicket();
            renderizarMesas();
        } catch (SQLException e) {
            error("Error cancelando pedido", e);
        }
    }

    private void actualizarTotalTicket() {
        BigDecimal total = lineasTicket.stream()
                .map(LineaPedido::getTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        lblTicketTotal.setText("TOTAL:  " + total.setScale(2, RoundingMode.HALF_UP) + " €");
    }

    @FXML private void preimprimirTicket() {
        if (lineasTicket.isEmpty()) { warn("El ticket está vacío."); return; }
        StringBuilder sb = new StringBuilder();
        sb.append("════════════════════════════════\n");
        sb.append("        TICKET PREVIO\n");
        sb.append("  Mesa: ").append(mesaActual != null ? mesaActual : "-").append("\n");
        sb.append("────────────────────────────────\n");
        for (LineaPedido l : lineasTicket) {
            String nombre = l.getArticulo() != null ? l.getArticulo().getNombre() : "Artículo";
            String lineTxt = String.format("%-2dx %-20s %6.2f €%n",
                    l.getCantidad(), nombre, l.getTotal());
            sb.append(lineTxt);
        }
        sb.append("────────────────────────────────\n");
        BigDecimal total = lineasTicket.stream().map(LineaPedido::getTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        sb.append(String.format("  TOTAL:  %24.2f €%n", total));
        sb.append("════════════════════════════════\n");
        sb.append("         Gracias por su visita");

        TextArea ta = new TextArea(sb.toString());
        ta.setEditable(false);
        ta.setStyle("-fx-font-family: monospace; -fx-font-size: 13px;");
        ta.setPrefSize(340, 280);
        Dialog<Void> dlg = new Dialog<>();
        dlg.setTitle("Preimprimir Ticket");
        dlg.getDialogPane().setContent(ta);
        dlg.getDialogPane().getButtonTypes().add(ButtonType.OK);
        dlg.showAndWait();
    }

    @FXML private void cierreTurno() {
        try {
            List<PedidoRestaurante> abiertos = PedidoRestauranteDAO.getAbiertos();
            if (!abiertos.isEmpty()) {
                StringBuilder sb = new StringBuilder(
                        "⚠️  Hay " + abiertos.size() + " pedido(s) abierto(s):\n\n");
                abiertos.forEach(o -> sb.append("  · ").append(o.getNumeroMesa())
                        .append(" — ").append(estadoLabel(o.getEstado())).append("\n"));
                sb.append("\nCierra los tickets antes de hacer el arqueo de turno.");
                Alert a = new Alert(Alert.AlertType.WARNING, sb.toString(), ButtonType.OK);
                a.setTitle("Cierre de Turno");
                a.setHeaderText("Tickets abiertos con numeración");
                a.showAndWait();
                return;
            }
            // Sin pedidos abiertos — mostrar resumen del día
            List<PedidoRestaurante> hoy = PedidoRestauranteDAO.getPorFecha(LocalDate.now());
            BigDecimal totalEfectivo    = BigDecimal.ZERO;
            BigDecimal totalTarjeta     = BigDecimal.ZERO;
            BigDecimal totalSinMetodo   = BigDecimal.ZERO;
            BigDecimal totalGeneral     = BigDecimal.ZERO;
            int entregados = 0, cancelados = 0;
            for (PedidoRestaurante o : hoy) {
                BigDecimal t = o.getTotal();
                if (o.getEstado() == PedidoRestaurante.Estado.ENTREGADO) {
                    entregados++;
                    totalGeneral = totalGeneral.add(t);
                    String m = o.getMetodoPago();
                    if ("EFECTIVO".equals(m))      totalEfectivo = totalEfectivo.add(t);
                    else if (m != null && m.startsWith("TARJETA")) totalTarjeta = totalTarjeta.add(t);
                    else                           totalSinMetodo = totalSinMetodo.add(t);
                } else if (o.getEstado() == PedidoRestaurante.Estado.CANCELADO) {
                    cancelados++;
                }
            }
            String resumen = String.format(
                    "════════════════════════\n" +
                            "    CIERRE DE TURNO\n" +
                            "    %s\n" +
                            "════════════════════════\n" +
                            "  Pedidos entregados: %d\n" +
                            "  Pedidos cancelados: %d\n" +
                            "────────────────────────\n" +
                            "  Efectivo:    %8.2f €\n" +
                            "  Tarjeta:     %8.2f €\n" +
                            "  Sin método:  %8.2f €\n" +
                            "────────────────────────\n" +
                            "  TOTAL:       %8.2f €\n" +
                            "════════════════════════",
                    LocalDate.now(), entregados, cancelados,
                    totalEfectivo, totalTarjeta, totalSinMetodo, totalGeneral
            );
            TextArea ta = new TextArea(resumen);
            ta.setEditable(false);
            ta.setStyle("-fx-font-family: monospace; -fx-font-size: 13px;");
            ta.setPrefSize(320, 300);
            Dialog<Void> dlg = new Dialog<>();
            dlg.setTitle("Cierre de Turno");
            dlg.getDialogPane().setContent(ta);
            dlg.getDialogPane().getButtonTypes().add(ButtonType.OK);
            dlg.showAndWait();
        } catch (SQLException e) {
            error("Error en cierre de turno", e);
        }
    }

    @FXML private void cobrar() {
        if (pedidoActual == null || lineasTicket.isEmpty()) {
            warn("No hay pedido activo en esta mesa."); return;
        }
        BigDecimal total = lineasTicket.stream().map(LineaPedido::getTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // Preguntar método de pago
        ChoiceDialog<String> dlgPago = new ChoiceDialog<>(
                "Efectivo", List.of("Efectivo", "Tarjeta Crédito", "Tarjeta Débito"));
        dlgPago.setTitle("Método de Pago");
        dlgPago.setHeaderText("Cobro de " + total.setScale(2, RoundingMode.HALF_UP) + " €  —  " + mesaActual);
        dlgPago.setContentText("Forma de pago:");
        Optional<String> metodo = dlgPago.showAndWait();
        if (metodo.isEmpty()) return;

        String metodoCodigo = switch (metodo.get()) {
            case "Tarjeta Crédito" -> "TARJETA_CREDITO";
            case "Tarjeta Débito"  -> "TARJETA_DEBITO";
            default                -> "EFECTIVO";
        };

        try {
            PedidoRestauranteDAO.entregar(pedidoActual.getId(), metodoCodigo);
            info("Cobro realizado",
                    "Pedido cobrado: " + total.setScale(2, RoundingMode.HALF_UP)
                            + " €\nMétodo: " + metodo.get());
            lineasTicket.clear();
            pedidoActual = null;
            mesaActual = null;
            lblMesaActual.setText("Sin mesa");
            lblTicketMesa.setText("🧾  Ticket");
            actualizarTotalTicket();
            renderizarMesas();
        } catch (SQLException e) {
            error("Error al cobrar", e);
        }
    }

    @FXML private void cargoHabitacion() {
        if (pedidoActual == null || lineasTicket.isEmpty()) {
            warn("No hay pedido activo."); return;
        }
        // Mostrar lista de habitaciones ocupadas
        List<Habitacion> ocupadas;
        try {
            ocupadas = HabitacionDAO.obtenerTodas().stream()
                    .filter(r -> r.getEstado() == Habitacion.Estado.OCUPADA)
                    .toList();
        } catch (SQLException e) {
            error("Error cargando habitaciones", e); return;
        }
        if (ocupadas.isEmpty()) {
            warn("No hay habitaciones ocupadas en este momento."); return;
        }

        // Dialog con ListView de habitaciones
        Dialog<Habitacion> dlg = new Dialog<>();
        dlg.setTitle("Cargo a Habitación");
        dlg.setHeaderText("Selecciona la habitación");
        ListView<Habitacion> listView = new ListView<>(FXCollections.observableArrayList(ocupadas));
        listView.setPrefHeight(200);
        listView.setCellFactory(lv -> new ListCell<>() {
            @Override protected void updateItem(Habitacion r, boolean empty) {
                super.updateItem(r, empty);
                if (empty || r == null) { setText(null); return; }
                String tipo = r.getTipoHabitacion() != null ? r.getTipoHabitacion().getNombre() : "";
                setText("Hab. " + r.getNumero() + "  —  " + tipo);
            }
        });
        dlg.getDialogPane().setContent(listView);
        ButtonType btnCargar = new ButtonType("Cargar a Habitación", ButtonBar.ButtonData.OK_DONE);
        dlg.getDialogPane().getButtonTypes().addAll(btnCargar, ButtonType.CANCEL);
        dlg.setResultConverter(bt -> bt == btnCargar ? listView.getSelectionModel().getSelectedItem() : null);

        Optional<Habitacion> selRoom = dlg.showAndWait();
        if (selRoom.isEmpty() || selRoom.get() == null) return;
        Habitacion room = selRoom.get();

        try {
            BigDecimal total = lineasTicket.stream().map(LineaPedido::getTotal)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            Factura inv = FacturaDAO.getAbiertaPorHabitacion(room.getId());
            int invoiceId;
            if (inv == null) {
                // Fetch the checked-in reservation to link the new invoice properly
                Reserva res = ReservaDAO.getRegistradaPorHabitacion(room.getId());
                Factura newInv = new Factura();
                newInv.setFechaEmision(LocalDate.now());
                if (res != null) {
                    newInv.setReserva(res);
                    newInv.setHuesped(res.getHuesped());
                }
                invoiceId = FacturaDAO.crear(newInv);
            } else {
                invoiceId = inv.getId();
            }
            // Detalle por artículo en la descripción
            StringBuilder detalle = new StringBuilder("Restaurante · " + mesaActual + ": ");
            lineasTicket.forEach(l -> {
                String n = l.getArticulo() != null ? l.getArticulo().getNombre() : "?";
                detalle.append(l.getCantidad()).append("x ").append(n).append(", ");
            });
            String desc = detalle.toString().replaceAll(", $", "");
            FacturaDAO.añadirLinea(new LineaFactura(invoiceId, desc, BigDecimal.ONE, total));

            PedidoRestauranteDAO.vincularHabitacion(pedidoActual.getId(), room.getId());
            PedidoRestauranteDAO.entregar(pedidoActual.getId(), "CARGO_HABITACION");

            info("Cargo realizado",
                    total.setScale(2, RoundingMode.HALF_UP) + " € cargados a habitación "
                            + room.getNumero());
            lineasTicket.clear();
            pedidoActual = null;
            mesaActual = null;
            lblMesaActual.setText("Sin mesa");
            lblTicketMesa.setText("🧾  Ticket");
            actualizarTotalTicket();
            renderizarMesas();
        } catch (Exception e) {
            error("Error en cargo a habitación", e);
        }
    }

    @FXML private void actualizarMesas() {
        renderizarMesas();
    }


    // ADMIN — NAVEGACIÓN

    private List<VBox> adminPanels;
    private List<Button> adminNavBtns;

    private void setupAdminTables() {
        adminPanels = List.of(pnlCategorias, pnlProductosAdmin, pnlMesasAdmin, pnlPedidos, pnlHistorial);
        adminNavBtns = List.of(btnNavCat, btnNavProd, btnNavMesas, btnNavPedidos, btnNavHistorial);

        // Categorías
        colCatNombre.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getNombre()));
        colCatActiva.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().isActivo() ? "✓" : "✗"));

        tblCat.getSelectionModel().selectedItemProperty().addListener((obs, o, n) -> {
            if (n != null) rellenarFormCat(n);
        });

        // Productos
        colProdNombre.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getNombre()));
        colProdCat.setCellValueFactory(c -> {
            CategoriaMenu cat = c.getValue().getCategoria();
            return new SimpleStringProperty(cat != null ? cat.getNombre() : "-");
        });
        colProdPrecio.setCellValueFactory(c -> {
            BigDecimal p = c.getValue().getPrecio();
            return new SimpleStringProperty(p != null ? p.setScale(2, RoundingMode.HALF_UP) + " €" : "-");
        });
        colProdDisp.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().isDisponible() ? "✓" : "✗"));

        tblProd.getSelectionModel().selectedItemProperty().addListener((obs, o, n) -> {
            if (n != null) rellenarFormProd(n);
        });

        // Pedidos
        colPedMesa.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getNumeroMesa()));
        colPedEstado.setCellValueFactory(c -> new SimpleStringProperty(estadoLabel(c.getValue().getEstado())));
        colPedHora.setCellValueFactory(c -> {
            var t = c.getValue().getFechaCreacion();
            return new SimpleStringProperty(t != null ? t.format(timeFmt) : "-");
        });
        colPedTotal.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getTotal().setScale(2, RoundingMode.HALF_UP) + " €"));

        // Historial con turno
        colHistMesa.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getNumeroMesa()));
        if (colHistTurno != null) {
            colHistTurno.setCellValueFactory(c -> {
                var t = c.getValue().getFechaCreacion();
                return new SimpleStringProperty(t != null ? turnoLabel(t.getHour()) : "-");
            });
        }
        colHistEstado.setCellValueFactory(c -> new SimpleStringProperty(estadoLabel(c.getValue().getEstado())));
        colHistHora.setCellValueFactory(c -> {
            var t = c.getValue().getFechaCreacion();
            return new SimpleStringProperty(t != null ? t.format(timeFmt) : "-");
        });
        colHistTotal.setCellValueFactory(c -> new SimpleStringProperty(
                c.getValue().getTotal().setScale(2, RoundingMode.HALF_UP) + " €"));

        dpHistFecha.setValue(LocalDate.now());
    }

    private void navAdmin(VBox panel, Button btn) {
        adminPanels.forEach(p -> { p.setVisible(false); p.setManaged(false); });
        adminNavBtns.forEach(b -> b.getStyleClass().remove("nav-btn-active"));
        panel.setVisible(true);
        panel.setManaged(true);
        btn.getStyleClass().add("nav-btn-active");
    }

    @FXML private void navCategorias()  { navAdmin(pnlCategorias, btnNavCat); cargarCategorias(); }
    @FXML private void navProductos()   { navAdmin(pnlProductosAdmin, btnNavProd); cargarProductosAdmin(); }
    @FXML private void navMesas()       { navAdmin(pnlMesasAdmin, btnNavMesas); construirMapaZonaTabs(); mostrarMapaZona(zonaMapaActual); }
    @FXML private void navPedidos()     { navAdmin(pnlPedidos, btnNavPedidos); cargarPedidos(); }
    @FXML private void navHistorial()   { navAdmin(pnlHistorial, btnNavHistorial); cargarHistorial(); }


    // ADMIN — MESAS

    private void construirMapaZonaTabs() {
        mapaZonaTabs.getChildren().clear();
        ToggleGroup tg = new ToggleGroup();
        ZONAS.forEach(zona -> {
            ToggleButton tb = new ToggleButton(zona);
            tb.getStyleClass().add("zona-tab");
            tb.setToggleGroup(tg);
            tb.setOnAction(e -> { zonaMapaActual = zona; mostrarMapaZona(zona); });
            if (zona.equals(zonaMapaActual)) tb.setSelected(true);
            mapaZonaTabs.getChildren().add(tb);
        });
    }

    private void mostrarMapaZona(String zona) {
        zonaMapaActual = zona;
        mesaSeleccionadaMapa = null;
        if (lblMesaSelec != null) lblMesaSelec.setText("Ninguna seleccionada");
        panelMapaMesas.getChildren().clear();

        Set<String> ocupadas = new HashSet<>();
        try { PedidoRestauranteDAO.getAbiertos().forEach(o -> {
            if (o.getNumeroMesa() != null) ocupadas.add(o.getNumeroMesa());
        }); } catch (SQLException e) { e.printStackTrace(); }

        List<MesaRestaurante> tablas = tablesByZone.getOrDefault(zona, List.of());
        tablas.forEach(t -> {
            VBox node = crearNodoMesa(t, ocupadas.contains(t.getNombre()));
            node.setLayoutX(t.getPosX());
            node.setLayoutY(t.getPosY());
            panelMapaMesas.getChildren().add(node);
        });
    }

    private VBox crearNodoMesa(MesaRestaurante t, boolean ocupada) {
        javafx.scene.control.Label lblNombre = new javafx.scene.control.Label(t.getNombre());
        lblNombre.setStyle("-fx-font-weight: bold; -fx-font-size: 11px;");
        javafx.scene.control.Label lblCap = new javafx.scene.control.Label("👥 " + t.getCapacidad());
        lblCap.setStyle("-fx-font-size: 10px;");
        VBox node = new VBox(3, lblNombre, lblCap);
        node.setPrefWidth(90);
        node.setPrefHeight(56);
        node.setAlignment(Pos.CENTER);
        node.getStyleClass().add(ocupada ? "mapa-mesa-ocupada" : "mapa-mesa-libre");
        node.setOnMouseClicked(e -> {
            panelMapaMesas.getChildren().forEach(n -> n.getStyleClass().remove("mapa-mesa-selected"));
            node.getStyleClass().add("mapa-mesa-selected");
            mesaSeleccionadaMapa = t;
            if (lblMesaSelec != null) lblMesaSelec.setText(t.getNombre() + "  (👥 " + t.getCapacidad() + ")");
        });
        return node;
    }

    @FXML private void agregarMesaMapa() {
        String nombre = txtNuevaMesaNombre != null ? txtNuevaMesaNombre.getText().trim() : "";
        if (nombre.isBlank()) { warn("Introduce el nombre de la mesa."); return; }
        int cap = 4;
        try { if (txtNuevaMesaCap != null && !txtNuevaMesaCap.getText().isBlank())
            cap = Integer.parseInt(txtNuevaMesaCap.getText().trim()); }
        catch (NumberFormatException ignored) {}

        MesaRestaurante t = new MesaRestaurante();
        t.setZona(zonaMapaActual);
        t.setNombre(nombre);
        t.setCapacidad(cap);
        t.setOrden(tablesByZone.getOrDefault(zonaMapaActual, List.of()).size() + 1);
        t.setPosX(10); t.setPosY(10);
        try {
            int id = MesaRestauranteDAO.crear(t);
            t.setId(id);
            tablesByZone.computeIfAbsent(zonaMapaActual, k -> new ArrayList<>()).add(t);
            if (txtNuevaMesaNombre != null) txtNuevaMesaNombre.clear();
            if (txtNuevaMesaCap != null) txtNuevaMesaCap.clear();
            mostrarMapaZona(zonaMapaActual);
            construirZonaTabs();  // Actualiza TMS tabs también
        } catch (SQLException e) { error("Error añadiendo mesa", e); }
    }

    @FXML private void eliminarMesaMapa() {
        if (mesaSeleccionadaMapa == null) { warn("Selecciona una mesa en el mapa."); return; }
        if (!confirm("¿Eliminar '" + mesaSeleccionadaMapa.getNombre() + "' de " + zonaMapaActual + "?")) return;
        try {
            if (mesaSeleccionadaMapa.getId() > 0)
                MesaRestauranteDAO.eliminar(mesaSeleccionadaMapa.getId());
            tablesByZone.getOrDefault(zonaMapaActual, new ArrayList<>()).remove(mesaSeleccionadaMapa);
            mesaSeleccionadaMapa = null;
            if (lblMesaSelec != null) lblMesaSelec.setText("Ninguna seleccionada");
            mostrarMapaZona(zonaMapaActual);
            construirZonaTabs();
        } catch (SQLException e) { error("Error eliminando mesa", e); }
    }


    // ADMIN — CATEGORÍAS

    private void cargarCategorias() {
        try {
            tblCat.setItems(FXCollections.observableArrayList(CategoriaMenuDAO.obtenerTodas()));
            limpiarFormCat();
            cargarFamilias();
        } catch (SQLException e) { error("Error cargando categorías", e); }
    }

    private void rellenarFormCat(CategoriaMenu c) {
        txtCatNombre.setText(c.getNombre());
        txtCatDesc.setText(c.getDescripcion() != null ? c.getDescripcion() : "");
        txtCatColor.setText(c.getColor() != null ? c.getColor() : "#607D8B");
        chkCatActiva.setSelected(c.isActivo());
    }

    private void limpiarFormCat() {
        txtCatNombre.clear(); txtCatDesc.clear();
        txtCatColor.setText("#607D8B"); chkCatActiva.setSelected(true);
        tblCat.getSelectionModel().clearSelection();
    }

    @FXML private void nuevaCategoria()  { limpiarFormCat(); }

    @FXML private void guardarCategoria() {
        if (txtCatNombre.getText().isBlank()) { warn("El nombre es obligatorio."); return; }
        CategoriaMenu sel = tblCat.getSelectionModel().getSelectedItem();
        CategoriaMenu cat = sel != null ? sel : new CategoriaMenu();
        cat.setNombre(txtCatNombre.getText().trim());
        cat.setDescripcion(txtCatDesc.getText().trim());
        cat.setColor(txtCatColor.getText().trim());
        cat.setActivo(chkCatActiva.isSelected());
        try {
            if (sel == null) CategoriaMenuDAO.crear(cat);
            else CategoriaMenuDAO.actualizar(cat);
            cargarCategorias();
        } catch (SQLException e) { error("Error guardando categoría", e); }
    }

    @FXML private void eliminarCategoria() {
        CategoriaMenu sel = tblCat.getSelectionModel().getSelectedItem();
        if (sel == null) { warn("Selecciona una categoría."); return; }
        if (!confirm("¿Eliminar categoría '" + sel.getNombre() + "'?")) return;
        try {
            CategoriaMenuDAO.eliminar(sel.getId());
            cargarCategorias();
        } catch (SQLException e) { error("Error eliminando categoría", e); }
    }


    // ADMIN — PRODUCTOS

    private void cargarProductosAdmin() {
        try {
            tblProd.setItems(FXCollections.observableArrayList(ArticuloMenuDAO.obtenerTodos()));
            List<CategoriaMenu> cats = CategoriaMenuDAO.obtenerTodas();
            cmbProdCat.setItems(FXCollections.observableArrayList(cats));
            limpiarFormProd();
        } catch (SQLException e) { error("Error cargando productos", e); }
    }

    private void rellenarFormProd(ArticuloMenu m) {
        txtProdNombre.setText(m.getNombre());
        txtProdDesc.setText(m.getDescripcion() != null ? m.getDescripcion() : "");
        txtProdPrecio.setText(m.getPrecio() != null ? m.getPrecio().toPlainString() : "");
        txtProdAlerg.setText(m.getAlergenos() != null ? m.getAlergenos() : "");
        chkProdDisp.setSelected(m.isDisponible());
        cmbProdCat.setValue(m.getCategoria());
    }

    private void limpiarFormProd() {
        txtProdNombre.clear(); txtProdDesc.clear(); txtProdPrecio.clear();
        txtProdAlerg.clear(); chkProdDisp.setSelected(true); cmbProdCat.setValue(null);
        tblProd.getSelectionModel().clearSelection();
    }

    @FXML private void nuevoProducto() { limpiarFormProd(); }

    @FXML private void guardarProducto() {
        if (txtProdNombre.getText().isBlank()) { warn("El nombre es obligatorio."); return; }
        BigDecimal precio;
        try { precio = new BigDecimal(txtProdPrecio.getText().replace(",", ".")); }
        catch (NumberFormatException e) { warn("Precio inválido."); return; }

        ArticuloMenu sel = tblProd.getSelectionModel().getSelectedItem();
        ArticuloMenu item = sel != null ? sel : new ArticuloMenu();
        item.setNombre(txtProdNombre.getText().trim());
        item.setDescripcion(txtProdDesc.getText().trim());
        item.setPrecio(precio);
        item.setAlergenos(txtProdAlerg.getText().trim());
        item.setDisponible(chkProdDisp.isSelected());
        item.setCategoria(cmbProdCat.getValue());
        try {
            if (sel == null) ArticuloMenuDAO.crear(item);
            else ArticuloMenuDAO.actualizar(item);
            cargarProductosAdmin();
        } catch (SQLException e) { error("Error guardando producto", e); }
    }

    @FXML private void eliminarProducto() {
        ArticuloMenu sel = tblProd.getSelectionModel().getSelectedItem();
        if (sel == null) { warn("Selecciona un producto."); return; }
        if (!confirm("¿Eliminar '" + sel.getNombre() + "'?")) return;
        try {
            ArticuloMenuDAO.eliminar(sel.getId());
            cargarProductosAdmin();
        } catch (SQLException e) { error("Error eliminando producto", e); }
    }


    // ADMIN — PEDIDOS

    @FXML private void cargarPedidos() {
        try {
            tblPedidos.setItems(FXCollections.observableArrayList(PedidoRestauranteDAO.getAbiertos()));
        } catch (SQLException e) { error("Error cargando pedidos", e); }
    }

    @FXML private void marcarEntregado() {
        PedidoRestaurante sel = tblPedidos.getSelectionModel().getSelectedItem();
        if (sel == null) { warn("Selecciona un pedido."); return; }
        try {
            PedidoRestauranteDAO.entregar(sel.getId());
            cargarPedidos();
        } catch (SQLException e) { error("Error actualizando pedido", e); }
    }

    @FXML private void cancelarPedido() {
        PedidoRestaurante sel = tblPedidos.getSelectionModel().getSelectedItem();
        if (sel == null) { warn("Selecciona un pedido."); return; }
        if (!confirm("¿Cancelar pedido de " + sel.getNumeroMesa() + "?")) return;
        try {
            PedidoRestauranteDAO.actualizarEstado(sel.getId(), PedidoRestaurante.Estado.CANCELADO);
            cargarPedidos();
        } catch (SQLException e) { error("Error cancelando pedido", e); }
    }


    // ADMIN — HISTORIAL

    @FXML private void cargarHistorial() {
        LocalDate fecha = dpHistFecha.getValue();
        if (fecha == null) fecha = LocalDate.now();
        try {
            List<PedidoRestaurante> pedidos = PedidoRestauranteDAO.getPorFecha(fecha);
            tblHistorial.setItems(FXCollections.observableArrayList(pedidos));
            actualizarArqueo(pedidos);
        } catch (SQLException e) { error("Error cargando historial", e); }
    }

    private void actualizarArqueo(List<PedidoRestaurante> pedidos) {
        int totalPed = 0;
        BigDecimal total = BigDecimal.ZERO, efectivo = BigDecimal.ZERO, tarjeta = BigDecimal.ZERO;
        for (PedidoRestaurante o : pedidos) {
            if (o.getEstado() == PedidoRestaurante.Estado.ENTREGADO) {
                totalPed++;
                BigDecimal t = o.getTotal();
                total = total.add(t);
                String m = o.getMetodoPago();
                if ("EFECTIVO".equals(m))              efectivo = efectivo.add(t);
                else if (m != null && m.startsWith("TARJETA")) tarjeta = tarjeta.add(t);
            }
        }
        if (lblArqueoTotalPedidos != null) lblArqueoTotalPedidos.setText(String.valueOf(totalPed));
        if (lblArqueoTotal    != null) lblArqueoTotal.setText(total.setScale(2, RoundingMode.HALF_UP) + " €");
        if (lblArqueoEfectivo != null) lblArqueoEfectivo.setText(efectivo.setScale(2, RoundingMode.HALF_UP) + " €");
        if (lblArqueoTarjeta  != null) lblArqueoTarjeta.setText(tarjeta.setScale(2, RoundingMode.HALF_UP) + " €");
    }

    private String turnoLabel(int hour) {
        if (hour >= 6  && hour < 14) return "🌅 Mañana";
        if (hour >= 14 && hour < 22) return "🌇 Tarde";
        return "🌙 Noche";
    }


    // HELPERS

    private String estadoLabel(PedidoRestaurante.Estado s) {
        if (s == null) return "-";
        return switch (s) {
            case PENDIENTE    -> "⏳ Pendiente";
            case PREPARANDO  -> "🍳 Preparando";
            case ENTREGADO   -> "✅ Entregado";
            case CANCELADO   -> "❌ Cancelado";
        };
    }

    private void info(String title, String msg) {
        Alert a = new Alert(Alert.AlertType.INFORMATION, msg, ButtonType.OK);
        a.setTitle(title); a.setHeaderText(title); a.showAndWait();
    }

    private void warn(String msg) {
        Alert a = new Alert(Alert.AlertType.WARNING, msg, ButtonType.OK);
        a.setTitle("Atención"); a.showAndWait();
    }

    private boolean confirm(String msg) {
        Alert a = new Alert(Alert.AlertType.CONFIRMATION, msg, ButtonType.YES, ButtonType.NO);
        a.setTitle("Confirmar"); a.setHeaderText("Confirmar acción");
        return a.showAndWait().orElse(ButtonType.NO) == ButtonType.YES;
    }

    private void error(String title, Exception e) {
        Alert a = new Alert(Alert.AlertType.ERROR, e.getMessage(), ButtonType.OK);
        a.setTitle(title); a.setHeaderText(title); a.showAndWait();
    }
}

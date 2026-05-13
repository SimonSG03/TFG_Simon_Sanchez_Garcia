package es.simonsg.pmsuite.controller;

import es.simonsg.pmsuite.dao.HuespedDAO;
import es.simonsg.pmsuite.dao.HotelDAO;
import es.simonsg.pmsuite.dao.FacturaDAO;
import es.simonsg.pmsuite.dao.PagoDAO;
import es.simonsg.pmsuite.dao.ReservaDAO;
import es.simonsg.pmsuite.model.Huesped;
import es.simonsg.pmsuite.model.Hotel;
import es.simonsg.pmsuite.model.Factura;
import es.simonsg.pmsuite.model.LineaFactura;
import es.simonsg.pmsuite.model.Pago;
import es.simonsg.pmsuite.model.Reserva;
import es.simonsg.pmsuite.util.GeneradorPdfFactura;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;

import com.lowagie.text.DocumentException;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class AdministracionController {

    // ── IndexController ref
    private IndexController indexController;

    public void setIndexController(IndexController ic) {
        this.indexController = ic;
    }

    /**
     * Abre el panel Pagos y preselecciona la factura de la reserva indicada.
     * Llamado desde IndexController tras un checkout en Recepción.
     */
    public void abrirPagosConReserva(int reservationId) {
        navPagos(); // carga pending invoices en cmbFacturaPago y tblFacturasPendientes
        todasLasFacturasPendientes.stream()
                .filter(inv -> inv.getReserva() != null
                        && inv.getReserva().getId() == reservationId)
                .findFirst()
                .ifPresent(inv -> {
                    tblFacturasPendientes.getSelectionModel().select(inv);
                    cmbFacturaPago.setValue(inv);
                });
    }

    // ── Sidebar buttons
    @FXML private Button btnNavFacturas;
    @FXML private Button btnNavPagos;
    @FXML private Button btnNavCaja;
    @FXML private Button btnNavIngresos;
    @FXML private Button btnNavExport;

    // ── Panels
    @FXML private VBox pnlFacturas;
    @FXML private VBox pnlPagos;
    @FXML private VBox pnlCaja;
    @FXML private VBox pnlIngresos;
    @FXML private VBox pnlExport;
    @FXML private VBox pnlNuevaFactura;

    // ── Panel 6: Nueva Factura / Editor
    // — Reserva —
    @FXML private TextField              txtNFBuscarReserva;
    @FXML private ComboBox<Reserva>  cmbNFReserva;
    @FXML private VBox                   vboxNFReservaInfo;
    @FXML private Label                  lblNFReservaNum;
    @FXML private Label                  lblNFReservaFechas;
    @FXML private Label                  lblNFReservaHab;
    // — Cliente —
    @FXML private TextField              txtNFBuscarCliente;
    @FXML private ComboBox<Huesped>        cmbNFCliente;
    // Datos de facturación alternativos
    @FXML private CheckBox               chkNFBillingOverride;
    @FXML private VBox                   vboxNFBillingFields;
    @FXML private TextField              txtNFBillingName;
    @FXML private TextField              txtNFBillingNif;
    @FXML private TextField              txtNFBillingAddress;
    @FXML private TextField              txtNFBillingCity;
    @FXML private TextField              txtNFBillingCp;
    @FXML private TextField              txtNFBillingCountry;
    @FXML private ComboBox<Factura.MetodoPago> cmbNFMetodoPago;
    @FXML private TextField              txtNFNotas;
    @FXML private TextField              txtNFDesc;
    @FXML private TextField              txtNFCantidad;
    @FXML private TextField              txtNFPrecio;
    @FXML private TableView<LineaFactura> tblNFLineas;
    @FXML private TableColumn<LineaFactura, String> colNFDesc;
    @FXML private TableColumn<LineaFactura, String> colNFCant;
    @FXML private TableColumn<LineaFactura, String> colNFPrecio;
    @FXML private TableColumn<LineaFactura, String> colNFTotal;
    @FXML private TableColumn<LineaFactura, String> colNFElim;
    @FXML private Label                  lblNFSubtotal;
    @FXML private Label                  lblNFIva;
    @FXML private Label                  lblNFTotal;
    @FXML private Button                 btnNFCancelar;
    @FXML private Button                 btnNFGuardar;

    // ── Panel 1 – sección «Huéspedes en casa»
    @FXML private TableView<Factura>          tblTicketsActivos;
    @FXML private TableColumn<Factura, String> colActHab;
    @FXML private TableColumn<Factura, String> colActHuesped;
    @FXML private TableColumn<Factura, String> colActCheckIn;
    @FXML private TableColumn<Factura, String> colActCheckOut;
    @FXML private TableColumn<Factura, String> colActTotal;
    @FXML private TableColumn<Factura, String> colActPend;
    @FXML private TableColumn<Factura, String> colActEstado;
    @FXML private TableColumn<Factura, String> colActAcciones;

    // ── Panel 1: Facturas / Tickets
    @FXML private ComboBox<String>       cmbFiltroFactura;
    @FXML private DatePicker             dpFacturaDesde;
    @FXML private DatePicker             dpFacturaHasta;
    @FXML private TableView<Factura>     tblFacturas;
    @FXML private TableColumn<Factura, String> colFacNum;
    @FXML private TableColumn<Factura, String> colFacFecha;
    @FXML private TableColumn<Factura, String> colFacHuesped;
    @FXML private TableColumn<Factura, String> colFacTotal;
    @FXML private TableColumn<Factura, String> colFacPagado;
    @FXML private TableColumn<Factura, String> colFacPendiente;
    @FXML private TableColumn<Factura, String> colFacEstado;
    @FXML private TableColumn<Factura, String> colFacAcciones;

    // ── Panel 2: Pagos
    @FXML private DatePicker             dpPagoDesde;
    @FXML private DatePicker             dpPagoHasta;
    // Tabla facturas pendientes de cobro
    @FXML private TableView<Factura>     tblFacturasPendientes;
    @FXML private TableColumn<Factura, String> colPendNum;
    @FXML private TableColumn<Factura, String> colPendFecha;
    @FXML private TableColumn<Factura, String> colPendHuesped;
    @FXML private TableColumn<Factura, String> colPendTotal;
    @FXML private TableColumn<Factura, String> colPendPagado;
    @FXML private TableColumn<Factura, String> colPendPend;
    @FXML private TableColumn<Factura, String> colPendEstado;
    // Historial de cobros
    @FXML private TableView<Pago>     tblPagos;
    @FXML private TableColumn<Pago, String> colPagFecha;
    @FXML private TableColumn<Pago, String> colPagFactura;
    @FXML private TableColumn<Pago, String> colPagHuesped;
    @FXML private TableColumn<Pago, String> colPagMetodo;
    @FXML private TableColumn<Pago, String> colPagImporte;
    @FXML private TextField              txtBuscarFactura;
    @FXML private ComboBox<Factura>      cmbFacturaPago;
    @FXML private TextField              txtImportePago;
    @FXML private ComboBox<Factura.MetodoPago> cmbMetodoPago;
    @FXML private TextField              txtReferenciaPago;
    @FXML private VBox                   vboxResumenPago;
    @FXML private Label                  lblPagoTotal;
    @FXML private Label                  lblPagoPagado;
    @FXML private Label                  lblPagoPendiente;

    // ── Panel 3: Caja del Día
    @FXML private Label kpiEfectivo;
    @FXML private Label kpiTarjetaCredito;
    @FXML private Label kpiTarjetaDebito;
    @FXML private Label kpiTransferencia;
    @FXML private Label kpiTotalDia;
    @FXML private TableView<Pago>     tblCaja;
    @FXML private TableColumn<Pago, String> colCajaHora;
    @FXML private TableColumn<Pago, String> colCajaFactura;
    @FXML private TableColumn<Pago, String> colCajaHuesped;
    @FXML private TableColumn<Pago, String> colCajaMetodo;
    @FXML private TableColumn<Pago, String> colCajaImporte;

    // ── Panel 4: Control Ingresos
    @FXML private DatePicker             dpIngDesde;
    @FXML private DatePicker             dpIngHasta;
    @FXML private Label kpiTotalFacturado;
    @FXML private Label kpiTotalCobrado;
    @FXML private Label kpiTotalPendiente;
    @FXML private TableView<Factura>     tblIngresos;
    @FXML private TableColumn<Factura, String> colIngNum;
    @FXML private TableColumn<Factura, String> colIngFecha;
    @FXML private TableColumn<Factura, String> colIngHuesped;
    @FXML private TableColumn<Factura, String> colIngTotal;
    @FXML private TableColumn<Factura, String> colIngCobrado;
    @FXML private TableColumn<Factura, String> colIngEstado;

    // ── Panel 5: Exportación
    @FXML private DatePicker             dpExpDesde;
    @FXML private DatePicker             dpExpHasta;
    @FXML private ComboBox<String>       cmbTipoExport;
    @FXML private ComboBox<String>       cmbSeparador;
    @FXML private Label                  lblExportEstado;

    private static final DateTimeFormatter DATE_FMT  = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DateTimeFormatter DT_FMT    = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    private static final DateTimeFormatter TIME_FMT  = DateTimeFormatter.ofPattern("HH:mm");

    /** Cache para no recargar de BD cada vez que cambia el combo de estado. */
    private List<Factura> listaFacturasCacheada = new ArrayList<>();

    /** Cache de facturas pendientes/parciales para el buscador de cobros. */
    private List<Factura> todasLasFacturasPendientes = new ArrayList<>();

    /** Líneas en construcción para la nueva factura. */
    private final List<LineaFactura> lineasNuevaFactura = new ArrayList<>();

    /** ID de la factura que se está editando (0 = nueva factura). */
    private int invoiceEdicionId = 0;

    /** Reserva vinculada al ticket que se está creando/editando (null = sin reserva). */
    private Reserva reservaSeleccionada = null;

    /**
     * IDs de facturas duplicadas de la misma reserva que deben cancelarse al guardar
     * (sus líneas ya han sido consolidadas en la factura principal invoiceEdicionId).
     */
    private final List<Integer> invoicesParaFusionar = new ArrayList<>();

    // ── Inicialización

    @FXML
    public void initialize() {
        configurarTablaTicketsActivos();
        configurarTablaFacturas();
        configurarTablaFacturasPendientes();
        configurarTablaPagos();
        configurarTablaCaja();
        configurarTablaIngresos();

        // Filtro facturas – por defecto "Pagada" (las pendientes se gestionan en Pagos)
        cmbFiltroFactura.setItems(FXCollections.observableArrayList(
                "Todas", "Pendiente", "Pago parcial", "Pagada", "Cancelada"));
        cmbFiltroFactura.setValue("Pagada");

        // Método de pago en formulario de cobro
        cmbMetodoPago.setItems(FXCollections.observableArrayList(Factura.MetodoPago.values()));
        cmbMetodoPago.setValue(Factura.MetodoPago.EFECTIVO);
        cmbMetodoPago.setConverter(new javafx.util.StringConverter<>() {
            @Override public String toString(Factura.MetodoPago m) {
                return m == null ? "" : m.toSpanish();
            }
            @Override public Factura.MetodoPago fromString(String s) { return null; }
        });

        // Combo factura para cobro
        cmbFacturaPago.setConverter(new javafx.util.StringConverter<>() {
            @Override public String toString(Factura inv) {
                if (inv == null) return "";
                String g = inv.getHuesped() != null
                        ? inv.getHuesped().getNombre() + " " + inv.getHuesped().getApellidos()
                        : "";
                return (inv.getNumeroFactura() != null ? inv.getNumeroFactura() : "#" + inv.getId())
                        + " – " + g;
            }
            @Override public Factura fromString(String s) { return null; }
        });

        // Al seleccionar factura: mostrar resumen y rellenar importe pendiente
        cmbFacturaPago.getSelectionModel().selectedItemProperty().addListener(
                (obs, old, sel) -> actualizarResumenFacturaPago(sel));

        // Exportación
        cmbTipoExport.setItems(FXCollections.observableArrayList(
                "Facturas", "Pagos", "Facturas y Pagos"));
        cmbTipoExport.setValue("Facturas y Pagos");
        cmbSeparador.setItems(FXCollections.observableArrayList(";", ",", "\t"));
        cmbSeparador.setValue(";");

        // Fechas por defecto (facturas: sin filtro de fechas para ver TODAS al abrir)
        LocalDate hoy = LocalDate.now();
        LocalDate inicioMes = hoy.withDayOfMonth(1);
        // dpFacturaDesde y dpFacturaHasta se dejan vacíos → cargarFacturas() usará getAll()
        dpPagoDesde.setValue(hoy);
        dpPagoHasta.setValue(hoy);
        dpIngDesde.setValue(inicioMes);
        dpIngHasta.setValue(hoy);
        dpExpDesde.setValue(inicioMes);
        dpExpHasta.setValue(hoy);

        // Al seleccionar cliente en el editor → validar datos de facturación
        cmbNFCliente.getSelectionModel().selectedItemProperty().addListener(
                (obs, old, sel) -> { if (sel != null && pnlNuevaFactura.isVisible()) validarDatosFacturacion(sel); });

        // Clic en tabla facturas → abrir editor
        tblFacturas.getSelectionModel().selectedItemProperty().addListener(
                (obs, old, sel) -> { if (sel != null) abrirEditorFactura(sel); });

        // Configurar tabla de líneas de nueva factura y columna de acciones
        configurarTablaNFLineas();
        configurarColumnaAcciones();

        // Combo método de pago en editor
        cmbNFMetodoPago.setItems(FXCollections.observableArrayList(Factura.MetodoPago.values()));
        cmbNFMetodoPago.setConverter(new javafx.util.StringConverter<>() {
            @Override public String toString(Factura.MetodoPago m) {
                return m == null ? "" : m.toSpanish();
            }
            @Override public Factura.MetodoPago fromString(String s) { return null; }
        });

        // Converter combo cliente
        cmbNFCliente.setConverter(new javafx.util.StringConverter<>() {
            @Override public String toString(Huesped g) {
                if (g == null) return "";
                String nif = (g.getNif() != null && !g.getNif().isEmpty()) ? " – " + g.getNif() : "";
                return g.getNombre() + " " + g.getApellidos() + nif;
            }
            @Override public Huesped fromString(String s) { return null; }
        });

        // Converter combo reserva
        cmbNFReserva.setConverter(new javafx.util.StringConverter<>() {
            @Override public String toString(Reserva r) {
                if (r == null) return "";
                String guest = r.getHuesped() != null ? r.getHuesped().getNombreCompleto() : "";
                return r.getNumeroReserva() + "  –  " + guest;
            }
            @Override public Reserva fromString(String s) { return null; }
        });

        // Al seleccionar reserva → auto-rellenar cliente y líneas
        // (solo en modo creación; si invoiceEdicionId>0 es una carga programática desde abrirEditorFactura)
        cmbNFReserva.getSelectionModel().selectedItemProperty().addListener(
                (obs, old, sel) -> { if (sel != null && invoiceEdicionId == 0) autoRellenarDesdeReserva(sel); });

        // Activar panel por defecto
        mostrarPanel(pnlFacturas, btnNavFacturas);
        cargarTicketsActivos();
        cargarFacturas();
        cargarFacturasPendientesParaCobro();
    }

    // ── Navegación sidebar

    @FXML private void navFacturas()  { mostrarPanel(pnlFacturas,  btnNavFacturas);  cargarTicketsActivos(); cargarFacturas(); }
    @FXML private void navPagos()     { mostrarPanel(pnlPagos,     btnNavPagos);     cargarFacturasPendientesParaCobro(); cargarPagos(); }
    @FXML private void navCaja()      { mostrarPanel(pnlCaja,      btnNavCaja);      cargarCaja(); }
    @FXML private void navIngresos()  { mostrarPanel(pnlIngresos,  btnNavIngresos);  }
    @FXML private void navExport()    { mostrarPanel(pnlExport,    btnNavExport); }

    private void mostrarPanel(VBox panel, Button btn) {
        for (VBox p : new VBox[]{pnlFacturas, pnlPagos, pnlCaja, pnlIngresos, pnlExport, pnlNuevaFactura}) {
            p.setVisible(false);
            p.setManaged(false);
        }
        panel.setVisible(true);
        panel.setManaged(true);

        for (Button b : new Button[]{btnNavFacturas, btnNavPagos, btnNavCaja, btnNavIngresos, btnNavExport}) {
            b.getStyleClass().remove("admin-nav-btn-active");
        }
        if (btn != null) btn.getStyleClass().add("admin-nav-btn-active");
    }

    @FXML private void volverInicio() {
        if (indexController != null) indexController.mostrarInicio();
    }

    // ── Panel 1 – Huéspedes en casa

    private void configurarTablaTicketsActivos() {
        colActHab.setCellValueFactory(c -> {
            var res = c.getValue().getReserva();
            String num = (res != null && res.getHabitacion() != null) ? res.getHabitacion().getNumero() : "—";
            return new SimpleStringProperty(num);
        });
        colActHuesped.setCellValueFactory(c -> new SimpleStringProperty(
                c.getValue().getHuesped() != null
                        ? c.getValue().getHuesped().getNombre() + " " + c.getValue().getHuesped().getApellidos()
                        : ""));
        colActCheckIn.setCellValueFactory(c -> {
            var res = c.getValue().getReserva();
            return new SimpleStringProperty(res != null && res.getFechaEntrada() != null
                    ? res.getFechaEntrada().format(DATE_FMT) : "");
        });
        colActCheckOut.setCellValueFactory(c -> {
            var res = c.getValue().getReserva();
            return new SimpleStringProperty(res != null && res.getFechaSalida() != null
                    ? res.getFechaSalida().format(DATE_FMT) : "");
        });
        colActTotal.setCellValueFactory(c -> new SimpleStringProperty(formatAmount(c.getValue().getImporteTotal())));
        colActPend.setCellValueFactory(c  -> new SimpleStringProperty(formatAmount(c.getValue().getImportePendiente())));
        colActEstado.setCellValueFactory(c -> new SimpleStringProperty(
                c.getValue().getEstado() != null ? c.getValue().getEstado().toSpanish() : ""));

        tblTicketsActivos.setRowFactory(tv -> new TableRow<>() {
            @Override protected void updateItem(Factura inv, boolean empty) {
                super.updateItem(inv, empty);
                if (empty || inv == null) { setStyle(""); return; }
                setStyle(inv.getEstado() == Factura.Estado.PARCIALMENTE_PAGADA
                        ? "-fx-background-color: #EBF5FB;" : "-fx-background-color: #FFF9E6;");
            }
        });

        colActAcciones.setCellFactory(col -> new TableCell<>() {
            private final Button btn = new Button("💳 Cobrar");
            {
                btn.setStyle("-fx-background-color: #27AE60; -fx-text-fill: white; " +
                        "-fx-cursor: hand; -fx-font-size: 11px; -fx-padding: 4 10; " +
                        "-fx-background-radius: 5;");
                btn.setOnAction(e -> irAPagosConFactura(getTableView().getItems().get(getIndex())));
            }
            @Override protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : btn);
            }
        });
    }

    @FXML
    private void cargarTicketsActivos() {
        try {
            tblTicketsActivos.setItems(
                    FXCollections.observableArrayList(FacturaDAO.getAbiertasDeRegistrados()));
        } catch (SQLException e) {
            mostrarError("Error cargando tickets activos", e.getMessage());
        }
    }

    private void irAPagosConFactura(Factura invoice) {
        navPagos();
        cmbFacturaPago.getItems().stream()
                .filter(inv -> inv.getId() == invoice.getId())
                .findFirst()
                .ifPresent(inv -> {
                    cmbFacturaPago.getSelectionModel().select(inv);
                    tblFacturasPendientes.getSelectionModel().select(inv);
                });
    }

    // ── Panel 1: Facturas ─────────────────────────────────────────

    private void configurarTablaFacturas() {
        colFacNum.setCellValueFactory(c -> new SimpleStringProperty(
                c.getValue().getNumeroFactura() != null ? c.getValue().getNumeroFactura() : "#" + c.getValue().getId()));
        colFacFecha.setCellValueFactory(c -> new SimpleStringProperty(
                c.getValue().getFechaEmision() != null ? c.getValue().getFechaEmision().format(DATE_FMT) : ""));
        colFacHuesped.setCellValueFactory(c -> new SimpleStringProperty(
                c.getValue().getHuesped() != null
                        ? c.getValue().getHuesped().getNombre() + " " + c.getValue().getHuesped().getApellidos()
                        : ""));
        colFacTotal.setCellValueFactory(c -> new SimpleStringProperty(
                formatAmount(c.getValue().getImporteTotal())));
        colFacPagado.setCellValueFactory(c -> new SimpleStringProperty(
                formatAmount(c.getValue().getImportePagado())));
        colFacPendiente.setCellValueFactory(c -> new SimpleStringProperty(
                formatAmount(c.getValue().getImportePendiente())));
        colFacEstado.setCellValueFactory(c -> new SimpleStringProperty(
                c.getValue().getEstado() != null ? c.getValue().getEstado().toSpanish() : ""));

        // Color por estado
        tblFacturas.setRowFactory(tv -> new TableRow<>() {
            @Override
            protected void updateItem(Factura inv, boolean empty) {
                super.updateItem(inv, empty);
                if (empty || inv == null) {
                    setStyle("");
                } else {
                    setStyle(switch (inv.getEstado()) {
                        case PENDIENTE        -> "-fx-background-color: #FFF9E6;";
                        case PARCIALMENTE_PAGADA -> "-fx-background-color: #EBF5FB;";
                        case PAGADA           -> "-fx-background-color: #EAFAF1;";
                        case CANCELADA      -> "-fx-background-color: #FDEDEC;";
                    });
                }
            }
        });
    }

    /**
     * Recarga facturas de la BD (con o sin filtro de fechas) y actualiza el caché.
     * Llamado por el botón "🔍 Filtrar" y en la inicialización.
     */
    @FXML
    private void cargarFacturas() {
        try {
            LocalDate desde = dpFacturaDesde.getValue();
            LocalDate hasta = dpFacturaHasta.getValue();

            if (desde != null && hasta != null) {
                listaFacturasCacheada = FacturaDAO.getPorRango(desde, hasta);
            } else {
                listaFacturasCacheada = FacturaDAO.obtenerTodas();
            }

            filtrarFacturasPorEstado();
        } catch (SQLException e) {
            mostrarError("Error cargando facturas", e.getMessage());
        }
    }

    /**
     * Aplica el filtro de estado sobre el caché ya cargado.
     * Llamado por el ComboBox de estado (onAction) para no relanzar la BD.
     */
    @FXML
    private void filtrarFacturasPorEstado() {
        String filtro = cmbFiltroFactura.getValue();
        List<Factura> filtradas;

        if (filtro == null || filtro.equals("Todas")) {
            filtradas = listaFacturasCacheada;
        } else {
            Factura.Estado estadoBuscar = switch (filtro) {
                case "Pendiente"    -> Factura.Estado.PENDIENTE;
                case "Pago parcial" -> Factura.Estado.PARCIALMENTE_PAGADA;
                case "Pagada"       -> Factura.Estado.PAGADA;
                case "Cancelada"    -> Factura.Estado.CANCELADA;
                default             -> null;
            };
            if (estadoBuscar != null) {
                Factura.Estado finalEstado = estadoBuscar;
                filtradas = listaFacturasCacheada.stream()
                        .filter(f -> f.getEstado() == finalEstado)
                        .toList();
            } else {
                filtradas = listaFacturasCacheada;
            }
        }

        tblFacturas.setItems(FXCollections.observableArrayList(filtradas));
    }

    /** Abre el editor con los datos de la factura seleccionada. */
    private void abrirEditorFactura(Factura inv) {
        invoiceEdicionId = inv.getId();

        // Ocultar panels principales y mostrar editor
        for (VBox p : new VBox[]{pnlFacturas, pnlPagos, pnlCaja, pnlIngresos, pnlExport}) {
            p.setVisible(false); p.setManaged(false);
        }
        pnlNuevaFactura.setVisible(true);
        pnlNuevaFactura.setManaged(true);

        // Precargar clientes
        try {
            cmbNFCliente.setItems(FXCollections.observableArrayList(HuespedDAO.buscar("")));
        } catch (SQLException e) {
            mostrarError("Error cargando clientes", e.getMessage());
        }

        // Rellenar campos con datos existentes
        if (inv.getHuesped() != null) {
            cmbNFCliente.setValue(
                    cmbNFCliente.getItems().stream()
                            .filter(g -> g.getId() == inv.getHuesped().getId())
                            .findFirst().orElse(null));
        }

        // Reserva vinculada (si existe)
        reservaSeleccionada = null;
        vboxNFReservaInfo.setVisible(false);
        vboxNFReservaInfo.setManaged(false);
        txtNFBuscarReserva.clear();
        if (inv.getReserva() != null && inv.getReserva().getId() > 0) {
            try {
                Reserva res = ReservaDAO.buscarPorId(inv.getReserva().getId());
                if (res != null) {
                    reservaSeleccionada = res;
                    cmbNFReserva.setItems(FXCollections.observableArrayList(List.of(res)));
                    cmbNFReserva.setValue(res);
                    vboxNFReservaInfo.setVisible(true);
                    vboxNFReservaInfo.setManaged(true);
                    lblNFReservaNum.setText("Reserva: " + res.getNumeroReserva());
                    String fechas = (res.getFechaEntrada() != null ? res.getFechaEntrada().format(DATE_FMT) : "?")
                            + " → " + (res.getFechaSalida() != null ? res.getFechaSalida().format(DATE_FMT) : "?")
                            + "  (" + res.getNoches() + " noche(s))";
                    lblNFReservaFechas.setText(fechas);
                    lblNFReservaHab.setText(res.getHabitacion() != null ? "Habitación " + res.getHabitacion().getNumero() : "Sin habitación");
                }
            } catch (SQLException e) {
                mostrarError("Error cargando reserva", e.getMessage());
            }
        }

        cmbNFMetodoPago.setValue(inv.getMetodoPago());
        txtNFNotas.setText(inv.getNotas() != null ? inv.getNotas() : "");

        // Datos de facturación alternativa
        boolean hasBilling = inv.tieneDatosFacturacion();
        chkNFBillingOverride.setSelected(hasBilling);
        vboxNFBillingFields.setVisible(hasBilling);
        vboxNFBillingFields.setManaged(hasBilling);
        txtNFBillingName.setText(inv.getNombreFacturacion()        != null ? inv.getNombreFacturacion()        : "");
        txtNFBillingNif.setText(inv.getNifFacturacion()          != null ? inv.getNifFacturacion()          : "");
        txtNFBillingAddress.setText(inv.getDomicilioFacturacion()  != null ? inv.getDomicilioFacturacion()  : "");
        txtNFBillingCity.setText(inv.getCiudadFacturacion()        != null ? inv.getCiudadFacturacion()        : "");
        txtNFBillingCp.setText(inv.getCpFacturacion()    != null ? inv.getCpFacturacion()    : "");
        txtNFBillingCountry.setText(inv.getPaisFacturacion()  != null ? inv.getPaisFacturacion()  : "");

        // Cargar líneas existentes
        lineasNuevaFactura.clear();
        try {
            lineasNuevaFactura.addAll(FacturaDAO.getLineas(inv.getId()));
        } catch (SQLException e) {
            mostrarError("Error cargando líneas", e.getMessage());
        }
        tblNFLineas.setItems(FXCollections.observableArrayList(lineasNuevaFactura));
        actualizarTotalesNF();

        // Modo edición: cambiar texto botón y mostrar Anular
        btnNFGuardar.setText("✓ Guardar cambios");
        btnNFCancelar.setVisible(true);
        btnNFCancelar.setManaged(true);
    }

    /** Anula la factura actualmente en edición y vuelve al listado. */
    @FXML
    private void cancelarFacturaDesdeEditor() {
        if (invoiceEdicionId <= 0) return;
        javafx.scene.control.Alert confirm = new javafx.scene.control.Alert(
                javafx.scene.control.Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Anular factura");
        confirm.setHeaderText("¿Anular esta factura?");
        confirm.setContentText("Esta acción no se puede deshacer.");
        confirm.showAndWait().ifPresent(r -> {
            if (r == ButtonType.OK) {
                try {
                    FacturaDAO.cancelar(invoiceEdicionId);
                    volverATickets();
                } catch (SQLException e) {
                    mostrarError("Error anulando factura", e.getMessage());
                }
            }
        });
    }

    private void configurarColumnaAcciones() {
        String btnStyle = "-fx-cursor: hand; -fx-font-size: 11px; -fx-padding: 3 8 3 8; " +
                "-fx-background-radius: 4; -fx-border-radius: 4;";
        colFacAcciones.setCellFactory(col -> new TableCell<>() {
            private final Button btnPdf   = new Button("PDF");
            private final Button btnEmail = new Button("Correo");
            private final HBox   box      = new HBox(4, btnPdf, btnEmail);
            {
                btnPdf.setStyle(btnStyle + "-fx-background-color: #E8EDF2; -fx-text-fill: #2C3E50;");
                btnPdf.setTooltip(new Tooltip("Generar PDF"));
                btnPdf.setOnAction(e -> {
                    Factura inv = getTableView().getItems().get(getIndex());
                    generarPdf(inv);
                });

                btnEmail.setStyle(btnStyle + "-fx-background-color: #D5F5E3; -fx-text-fill: #1A5276;");
                btnEmail.setTooltip(new Tooltip("Enviar por email"));
                btnEmail.setOnAction(e -> {
                    Factura inv = getTableView().getItems().get(getIndex());
                    enviarFacturaPorEmail(inv);
                });

                box.setAlignment(javafx.geometry.Pos.CENTER);
            }
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : box);
            }
        });
    }

    private void enviarFacturaPorEmail(Factura invoice) {
        try {
            if (invoice.getHuesped() != null && invoice.getHuesped().getId() > 0) {
                Huesped fullGuest = HuespedDAO.buscarPorId(invoice.getHuesped().getId());
                if (fullGuest != null) invoice.setHuesped(fullGuest);
            }

            List<LineaFactura> todasLineas;
            if (invoice.getReserva() != null && invoice.getReserva().getId() > 0) {
                todasLineas = FacturaDAO.getLineasPorReserva(invoice.getReserva().getId());
            } else {
                todasLineas = FacturaDAO.getLineas(invoice.getId());
            }

            List<LineaFactura> lineas = mostrarDialogoExclusion(todasLineas);
            if (lineas == null || lineas.isEmpty()) return;

            String num = invoice.getNumeroFactura() != null && !invoice.getNumeroFactura().isBlank()
                    ? invoice.getNumeroFactura().replaceAll("[^a-zA-Z0-9_-]", "_")
                    : "factura_" + invoice.getId();
            File tempPdf = File.createTempFile(num + "_", ".pdf");
            tempPdf.deleteOnExit();

            Hotel hotel = HotelDAO.get();
            GeneradorPdfFactura.generate(invoice, lineas, hotel, tempPdf);

            if (indexController != null) {
                indexController.abrirComunicacionConFactura(invoice, tempPdf);
            }
        } catch (DocumentException | IOException | SQLException e) {
            mostrarError("Error preparando el email", e.getMessage());
        }
    }

    private void generarPdf(Factura invoice) {
        try {
            // Cargar guest completo (la query de facturas solo trae nombre)
            if (invoice.getHuesped() != null && invoice.getHuesped().getId() > 0) {
                Huesped fullGuest = HuespedDAO.buscarPorId(invoice.getHuesped().getId());
                if (fullGuest != null) invoice.setHuesped(fullGuest);
            }

            // Cargar TODAS las líneas: si hay reserva, incluir extras de facturas paralelas
            List<LineaFactura> todasLineas;
            if (invoice.getReserva() != null && invoice.getReserva().getId() > 0) {
                todasLineas = FacturaDAO.getLineasPorReserva(invoice.getReserva().getId());
            } else {
                todasLineas = FacturaDAO.getLineas(invoice.getId());
            }

            // Mostrar diálogo de exclusión
            List<LineaFactura> lineasSeleccionadas = mostrarDialogoExclusion(todasLineas);
            if (lineasSeleccionadas == null) return;  // usuario canceló
            if (lineasSeleccionadas.isEmpty()) {
                mostrarInfo("Selecciona al menos una línea para incluir en la factura.");
                return;
            }

            // Seleccionar destino
            javafx.stage.FileChooser fc = new javafx.stage.FileChooser();
            fc.setTitle("Guardar factura PDF");
            String num = invoice.getNumeroFactura() != null && !invoice.getNumeroFactura().isBlank()
                    ? invoice.getNumeroFactura().replaceAll("[^a-zA-Z0-9_-]", "_")
                    : "ticket_" + invoice.getId();
            fc.setInitialFileName(num + ".pdf");
            fc.getExtensionFilters().add(new javafx.stage.FileChooser.ExtensionFilter("PDF", "*.pdf"));
            File dest = fc.showSaveDialog(tblFacturas.getScene().getWindow());
            if (dest == null) return;

            Hotel hotel = HotelDAO.get();
            GeneradorPdfFactura.generate(invoice, lineasSeleccionadas, hotel, dest);
            mostrarInfo("PDF generado: " + dest.getName());

        } catch (DocumentException | IOException | SQLException e) {
            mostrarError("Error generando PDF", e.getMessage());
        }
    }

    /**
     * Muestra un diálogo con todos los conceptos de la factura para que el usuario
     * seleccione cuáles desea incluir en el PDF.
     * Devuelve la lista de líneas seleccionadas, o null si el usuario canceló.
     */
    private List<LineaFactura> mostrarDialogoExclusion(List<LineaFactura> lineas) {
        Dialog<List<LineaFactura>> dialog = new Dialog<>();
        dialog.setTitle("Seleccionar conceptos para la factura");
        dialog.setHeaderText("¿Qué deseas excluir de la factura?\n"
                + "Desmarca los conceptos que se facturen por separado o ya estén pagados.");

        ButtonType btnGenerar = new ButtonType("Generar PDF", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(btnGenerar, ButtonType.CANCEL);

        // Lista de checkboxes — uno por línea
        javafx.scene.layout.VBox contenido = new javafx.scene.layout.VBox(10);
        contenido.setPadding(new javafx.geometry.Insets(16, 20, 8, 20));
        contenido.setMinWidth(480);

        Map<LineaFactura, CheckBox> checks = new LinkedHashMap<>();
        for (LineaFactura linea : lineas) {
            String etiqueta = linea.getDescripcion()
                    + "   ×" + (linea.getCantidad() != null
                    ? linea.getCantidad().stripTrailingZeros().toPlainString() : "1")
                    + "   →   " + formatAmount(linea.getPrecioTotal());
            CheckBox cb = new CheckBox(etiqueta);
            cb.setSelected(true);
            cb.setStyle("-fx-font-size: 12px;");
            checks.put(linea, cb);
            contenido.getChildren().add(cb);
        }

        // "Deseleccionar todo" / "Seleccionar todo"
        javafx.scene.layout.HBox botonesSel = new javafx.scene.layout.HBox(10);
        Button btnTodo  = new Button("Seleccionar todo");
        Button btnNada  = new Button("Deseleccionar todo");
        btnTodo.setOnAction(e -> checks.values().forEach(cb -> cb.setSelected(true)));
        btnNada.setOnAction(e -> checks.values().forEach(cb -> cb.setSelected(false)));
        botonesSel.getChildren().addAll(btnTodo, btnNada);
        contenido.getChildren().add(0, botonesSel);

        javafx.scene.control.ScrollPane scroll = new javafx.scene.control.ScrollPane(contenido);
        scroll.setFitToWidth(true);
        scroll.setMaxHeight(380);
        dialog.getDialogPane().setContent(scroll);

        dialog.setResultConverter(bt -> {
            if (bt != btnGenerar) return null;
            return checks.entrySet().stream()
                    .filter(e -> e.getValue().isSelected())
                    .map(Map.Entry::getKey)
                    .collect(Collectors.toList());
        });

        return dialog.showAndWait().orElse(null);
    }

    // ── Panel 2: Pagos

    private void configurarTablaFacturasPendientes() {
        colPendNum.setCellValueFactory(c -> new SimpleStringProperty(
                c.getValue().getNumeroFactura() != null ? c.getValue().getNumeroFactura() : "#" + c.getValue().getId()));
        colPendFecha.setCellValueFactory(c -> new SimpleStringProperty(
                c.getValue().getFechaEmision() != null ? c.getValue().getFechaEmision().format(DATE_FMT) : ""));
        colPendHuesped.setCellValueFactory(c -> new SimpleStringProperty(
                c.getValue().getHuesped() != null
                        ? c.getValue().getHuesped().getNombre() + " " + c.getValue().getHuesped().getApellidos()
                        : ""));
        colPendTotal.setCellValueFactory(c  -> new SimpleStringProperty(formatAmount(c.getValue().getImporteTotal())));
        colPendPagado.setCellValueFactory(c -> new SimpleStringProperty(formatAmount(c.getValue().getImportePagado())));
        colPendPend.setCellValueFactory(c   -> new SimpleStringProperty(formatAmount(c.getValue().getImportePendiente())));
        colPendEstado.setCellValueFactory(c -> new SimpleStringProperty(
                c.getValue().getEstado() != null ? c.getValue().getEstado().toSpanish() : ""));

        // Amarillo = pendiente, azul pálido = pago parcial
        tblFacturasPendientes.setRowFactory(tv -> new TableRow<>() {
            @Override
            protected void updateItem(Factura inv, boolean empty) {
                super.updateItem(inv, empty);
                if (empty || inv == null) { setStyle(""); return; }
                setStyle(inv.getEstado() == Factura.Estado.PARCIALMENTE_PAGADA
                        ? "-fx-background-color: #EBF5FB;"
                        : "-fx-background-color: #FFF9E6;");
            }
        });

        // Clic en fila → selecciona automáticamente la factura en el formulario de cobro
        tblFacturasPendientes.getSelectionModel().selectedItemProperty().addListener(
                (obs, old, sel) -> {
                    if (sel == null) return;
                    cmbFacturaPago.getItems().stream()
                            .filter(inv -> inv.getId() == sel.getId())
                            .findFirst()
                            .ifPresent(inv -> cmbFacturaPago.getSelectionModel().select(inv));
                });
    }

    private void configurarTablaPagos() {
        colPagFecha.setCellValueFactory(c -> new SimpleStringProperty(
                c.getValue().getFechaPago() != null ? c.getValue().getFechaPago().format(DT_FMT) : ""));
        colPagFactura.setCellValueFactory(c -> new SimpleStringProperty(
                c.getValue().getNumeroFactura() != null ? c.getValue().getNumeroFactura() : "#" + c.getValue().getIdFactura()));
        colPagHuesped.setCellValueFactory(c -> new SimpleStringProperty(
                c.getValue().getNombreHuesped() != null ? c.getValue().getNombreHuesped() : ""));
        colPagMetodo.setCellValueFactory(c -> new SimpleStringProperty(
                c.getValue().getMetodo() != null ? c.getValue().getMetodo().toSpanish() : ""));
        colPagImporte.setCellValueFactory(c -> new SimpleStringProperty(formatAmount(c.getValue().getImporte())));
    }

    @FXML
    private void cargarPagos() {
        LocalDate desde = dpPagoDesde.getValue();
        LocalDate hasta = dpPagoHasta.getValue();
        if (desde == null) desde = LocalDate.now();
        if (hasta == null) hasta = LocalDate.now();
        try {
            List<Pago> pagos = PagoDAO.getPorRango(desde, hasta);
            tblPagos.setItems(FXCollections.observableArrayList(pagos));
        } catch (SQLException e) {
            mostrarError("Error cargando pagos", e.getMessage());
        }
    }

    @FXML
    private void cargarFacturasPendientesParaCobro() {
        try {
            todasLasFacturasPendientes = FacturaDAO.getPendientesYParciales();
            cmbFacturaPago.setItems(FXCollections.observableArrayList(todasLasFacturasPendientes));
            tblFacturasPendientes.setItems(FXCollections.observableArrayList(todasLasFacturasPendientes));
            if (txtBuscarFactura != null) txtBuscarFactura.clear();
            vboxResumenPago.setVisible(false);
            vboxResumenPago.setManaged(false);
        } catch (SQLException e) {
            mostrarError("Error cargando facturas", e.getMessage());
        }
    }

    /** Filtra el combo de facturas pendientes según el texto del buscador. */
    @FXML
    private void buscarFacturaPago() {
        String texto = txtBuscarFactura.getText().trim().toLowerCase();
        if (texto.isEmpty()) {
            cmbFacturaPago.setItems(FXCollections.observableArrayList(todasLasFacturasPendientes));
        } else {
            List<Factura> filtradas = todasLasFacturasPendientes.stream()
                    .filter(inv -> {
                        String num = inv.getNumeroFactura() != null ? inv.getNumeroFactura().toLowerCase() : "";
                        String guest = inv.getHuesped() != null
                                ? (inv.getHuesped().getNombre() + " " + inv.getHuesped().getApellidos()).toLowerCase()
                                : "";
                        return num.contains(texto) || guest.contains(texto);
                    })
                    .toList();
            cmbFacturaPago.setItems(FXCollections.observableArrayList(filtradas));
        }
        cmbFacturaPago.show();
    }

    /** Actualiza las etiquetas de resumen y rellena el importe pendiente. */
    private void actualizarResumenFacturaPago(Factura inv) {
        if (inv == null) {
            vboxResumenPago.setVisible(false);
            vboxResumenPago.setManaged(false);
            txtImportePago.clear();
            return;
        }
        BigDecimal total     = inv.getImporteTotal()  != null ? inv.getImporteTotal()  : BigDecimal.ZERO;
        BigDecimal pagado    = inv.getImportePagado()   != null ? inv.getImportePagado()   : BigDecimal.ZERO;
        BigDecimal pendiente = inv.getImportePendiente() != null ? inv.getImportePendiente() : total.subtract(pagado);

        lblPagoTotal.setText(formatAmount(total));
        lblPagoPagado.setText(formatAmount(pagado));
        lblPagoPendiente.setText(formatAmount(pendiente));

        // Auto-rellenar el importe con lo que queda por pagar
        if (pendiente.compareTo(BigDecimal.ZERO) > 0) {
            txtImportePago.setText(pendiente.setScale(2, java.math.RoundingMode.HALF_UP).toPlainString().replace(".", ","));
        } else {
            txtImportePago.clear();
        }

        vboxResumenPago.setVisible(true);
        vboxResumenPago.setManaged(true);
    }

    @FXML
    private void registrarPago() {
        Factura factura = cmbFacturaPago.getValue();
        if (factura == null) {
            mostrarInfo("Seleccione una factura.");
            return;
        }
        String importeStr = txtImportePago.getText().trim().replace(",", ".");
        if (importeStr.isEmpty()) {
            mostrarInfo("Introduzca el importe.");
            return;
        }
        BigDecimal importe;
        try {
            importe = new BigDecimal(importeStr);
        } catch (NumberFormatException e) {
            mostrarInfo("Importe no válido.");
            return;
        }
        if (importe.compareTo(BigDecimal.ZERO) <= 0) {
            mostrarInfo("El importe debe ser mayor que 0.");
            return;
        }

        Pago p = new Pago();
        p.setIdFactura(factura.getId());
        p.setImporte(importe);
        p.setMetodo(cmbMetodoPago.getValue());
        p.setReferencia(txtReferenciaPago.getText().trim());
        try {
            PagoDAO.añadirPago(p);
            txtImportePago.clear();
            txtReferenciaPago.clear();
            cargarPagos();
            cargarFacturasPendientesParaCobro();
            mostrarInfo("Cobro registrado correctamente.");
        } catch (SQLException e) {
            mostrarError("Error registrando cobro", e.getMessage());
        }
    }

    // ── Panel 6: Nueva Factura

    /** Abre el panel de creación de nueva factura desde el botón del header. */
    @FXML
    private void abrirNuevaFactura() {
        limpiarNuevaFactura();
        for (VBox p : new VBox[]{pnlFacturas, pnlPagos, pnlCaja, pnlIngresos, pnlExport}) {
            p.setVisible(false);
            p.setManaged(false);
        }
        pnlNuevaFactura.setVisible(true);
        pnlNuevaFactura.setManaged(true);
        // Precargar todos los clientes en el combo
        try {
            cmbNFCliente.setItems(FXCollections.observableArrayList(HuespedDAO.buscar("")));
        } catch (SQLException e) {
            mostrarError("Error cargando clientes", e.getMessage());
        }
    }

    /** Vuelve al panel de Tickets y refresca la tabla. */
    @FXML
    private void volverATickets() {
        mostrarPanel(pnlFacturas, btnNavFacturas);
        cargarFacturas();
    }

    /** Busca reservas mientras el usuario escribe en el campo de reserva. */
    @FXML
    private void buscarReservaNF() {
        String texto = txtNFBuscarReserva.getText().trim();
        try {
            List<Reserva> resultados = texto.length() >= 2
                    ? ReservaDAO.buscar(texto)
                    : ReservaDAO.obtenerActivas();
            cmbNFReserva.setItems(FXCollections.observableArrayList(resultados));
            if (!resultados.isEmpty()) cmbNFReserva.show();
        } catch (SQLException e) {
            mostrarError("Error buscando reservas", e.getMessage());
        }
    }

    /**
     * Cuando se selecciona una reserva en el combo, auto-rellena:
     * – El combo de cliente con el huésped de la reserva
     * – Las líneas del ticket con la estancia + extras ya registrados
     */
    private void autoRellenarDesdeReserva(Reserva reserva) {
        reservaSeleccionada = reserva;
        invoicesParaFusionar.clear();

        // Mostrar info de la reserva
        vboxNFReservaInfo.setVisible(true);
        vboxNFReservaInfo.setManaged(true);
        lblNFReservaNum.setText("Reserva: " + reserva.getNumeroReserva());
        String fechas = (reserva.getFechaEntrada() != null ? reserva.getFechaEntrada().format(DATE_FMT) : "?")
                + " → "
                + (reserva.getFechaSalida() != null ? reserva.getFechaSalida().format(DATE_FMT) : "?")
                + "  (" + reserva.getNoches() + " noche(s))";
        lblNFReservaFechas.setText(fechas);
        String hab = reserva.getHabitacion() != null ? "Habitación " + reserva.getHabitacion().getNumero() : "Sin habitación asignada";
        lblNFReservaHab.setText(hab);

        // Auto-seleccionar cliente
        Huesped huesped = reserva.getHuesped();
        if (huesped != null) {
            try {
                List<Huesped> clientes = HuespedDAO.buscar("");
                cmbNFCliente.setItems(FXCollections.observableArrayList(clientes));
                int guestId = huesped.getId();
                cmbNFCliente.getItems().stream()
                        .filter(g -> g.getId() == guestId)
                        .findFirst()
                        .ifPresent(g -> cmbNFCliente.setValue(g));
            } catch (SQLException e) {
                mostrarError("Error cargando cliente", e.getMessage());
            }
        }

        lineasNuevaFactura.clear();
        try {
            List<Factura> facturasReserva = FacturaDAO.getTodasPorReserva(reserva.getId());

            if (!facturasReserva.isEmpty()) {
                // ── Hay facturas existentes → modo edición sobre la más antigua ──
                Factura principal = facturasReserva.get(0);
                invoiceEdicionId = principal.getId();

                // Recoger todas las líneas de todas las facturas de la reserva
                List<LineaFactura> todasLineas = FacturaDAO.getLineasPorReserva(reserva.getId());
                lineasNuevaFactura.addAll(todasLineas);

                // Registrar las facturas secundarias para cancelarlas al guardar
                facturasReserva.stream().skip(1)
                        .map(Factura::getId)
                        .forEach(invoicesParaFusionar::add);

                // Rellenar cabecera desde la factura principal
                cmbNFMetodoPago.setValue(principal.getMetodoPago());
                if (principal.getNotas() != null && !principal.getNotas().isBlank())
                    txtNFNotas.setText(principal.getNotas());

                // Cambiar UI a modo edición
                btnNFGuardar.setText("✓ Guardar cambios");
                btnNFCancelar.setVisible(true);
                btnNFCancelar.setManaged(true);

                if (!invoicesParaFusionar.isEmpty()) {
                    mostrarInfo("Se han detectado " + facturasReserva.size()
                            + " tickets para esta reserva. Al guardar quedarán consolidados en uno solo.");
                }
            } else {
                // ── Sin facturas → modo creación, generar línea de estancia ──
                invoiceEdicionId = 0;
                btnNFGuardar.setText("✓ Crear ticket");
                btnNFCancelar.setVisible(false);
                btnNFCancelar.setManaged(false);

                long nights = reserva.getNoches();
                if (nights > 0 && reserva.getPrecioTotal() != null
                        && reserva.getPrecioTotal().compareTo(BigDecimal.ZERO) > 0) {
                    BigDecimal pricePerNight = reserva.getPrecioTotal()
                            .divide(BigDecimal.valueOf(nights), 2, RoundingMode.HALF_UP);
                    String desc = "Estancia · " + hab + " · " + nights + " noche(s)";
                    lineasNuevaFactura.add(new LineaFactura(0, desc,
                            BigDecimal.valueOf(nights), pricePerNight));
                }
            }
        } catch (SQLException e) {
            mostrarError("Error cargando líneas de la reserva", e.getMessage());
        }

        tblNFLineas.setItems(FXCollections.observableArrayList(lineasNuevaFactura));
        actualizarTotalesNF();
    }

    /** Filtra el combo de clientes mientras el usuario escribe. */
    @FXML
    private void buscarClienteNF() {
        String texto = txtNFBuscarCliente.getText().trim();
        try {
            List<Huesped> resultados = HuespedDAO.buscar(texto);
            cmbNFCliente.setItems(FXCollections.observableArrayList(resultados));
            if (!resultados.isEmpty()) cmbNFCliente.show();
        } catch (SQLException e) {
            mostrarError("Error buscando clientes", e.getMessage());
        }
    }

    // ── Botones rápidos de servicio
    @FXML private void chipHabitacion()  { txtNFDesc.setText("Estancia – Habitación"); }
    @FXML private void chipRestaurante() { txtNFDesc.setText("Restaurante"); }
    @FXML private void chipMinibar()     { txtNFDesc.setText("Minibar"); }
    @FXML private void chipSpa()         { txtNFDesc.setText("Spa / Tratamiento"); }
    @FXML private void chipParking()     { txtNFDesc.setText("Parking"); }
    @FXML private void chipOtro()        { txtNFDesc.clear(); txtNFDesc.requestFocus(); }

    /** Añade la línea actual a la lista y actualiza totales. */
    @FXML
    private void agregarLineaNF() {
        String desc     = txtNFDesc.getText().trim();
        String cantStr  = txtNFCantidad.getText().trim();
        String precioStr = txtNFPrecio.getText().trim().replace(",", ".");

        if (desc.isEmpty())      { mostrarInfo("Introduce la descripción del servicio."); return; }
        if (precioStr.isEmpty()) { mostrarInfo("Introduce el precio unitario."); return; }
        if (cantStr.isEmpty())   cantStr = "1";

        try {
            BigDecimal cant   = new BigDecimal(cantStr.replace(",", "."));
            BigDecimal precio = new BigDecimal(precioStr);
            // Usamos el constructor que auto-calcula totalPrice
            LineaFactura linea = new LineaFactura(0, desc, cant, precio);
            lineasNuevaFactura.add(linea);
            tblNFLineas.setItems(FXCollections.observableArrayList(lineasNuevaFactura));
            actualizarTotalesNF();
            txtNFDesc.clear();
            txtNFCantidad.clear();
            txtNFPrecio.clear();
            txtNFDesc.requestFocus();
        } catch (NumberFormatException e) {
            mostrarInfo("Cantidad o precio no válidos. Usa punto o coma como separador decimal.");
        }
    }

    /** Recalcula y muestra subtotal, IVA y total. */
    private void actualizarTotalesNF() {
        BigDecimal subtotal = lineasNuevaFactura.stream()
                .map(LineaFactura::getPrecioTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .setScale(2, RoundingMode.HALF_UP);
        BigDecimal iva   = subtotal.multiply(new BigDecimal("0.10")).setScale(2, RoundingMode.HALF_UP);
        BigDecimal total = subtotal.add(iva);
        lblNFSubtotal.setText(formatAmount(subtotal));
        lblNFIva.setText(formatAmount(iva));
        lblNFTotal.setText(formatAmount(total));
    }

    /** Limpia todo el formulario y resetea al modo creación. */
    @FXML
    private void limpiarNuevaFactura() {
        invoiceEdicionId = 0;
        reservaSeleccionada = null;
        invoicesParaFusionar.clear();
        cmbNFReserva.setValue(null);
        cmbNFReserva.getItems().clear();
        txtNFBuscarReserva.clear();
        vboxNFReservaInfo.setVisible(false);
        vboxNFReservaInfo.setManaged(false);
        cmbNFCliente.setValue(null);
        txtNFBuscarCliente.clear();
        cmbNFMetodoPago.setValue(null);
        txtNFNotas.clear();
        txtNFDesc.clear();
        txtNFCantidad.clear();
        txtNFPrecio.clear();
        lineasNuevaFactura.clear();
        tblNFLineas.getItems().clear();
        lblNFSubtotal.setText("0,00 €");
        lblNFIva.setText("0,00 €");
        lblNFTotal.setText("0,00 €");
        btnNFGuardar.setText("✓ Crear ticket");
        btnNFCancelar.setVisible(false);
        btnNFCancelar.setManaged(false);
        // Limpiar sección de facturación alternativa
        chkNFBillingOverride.setSelected(false);
        vboxNFBillingFields.setVisible(false);
        vboxNFBillingFields.setManaged(false);
        txtNFBillingName.clear();
        txtNFBillingNif.clear();
        txtNFBillingAddress.clear();
        txtNFBillingCity.clear();
        txtNFBillingCp.clear();
        txtNFBillingCountry.clear();
    }

    /** Crea o actualiza la factura según el modo activo (invoiceEdicionId == 0 = nueva). */
    @FXML
    private void crearFactura() {
        Huesped cliente = cmbNFCliente.getValue();
        if (cliente == null)              { mostrarInfo("Selecciona un cliente."); return; }
        if (lineasNuevaFactura.isEmpty()) { mostrarInfo("Añade al menos un servicio."); return; }

        try {
            Factura.MetodoPago metodo = cmbNFMetodoPago.getValue();
            String notas = txtNFNotas.getText().trim();

            // Datos de facturación alternativa
            boolean usaBilling = chkNFBillingOverride.isSelected();
            String bName    = usaBilling ? txtNFBillingName.getText().trim()    : null;
            String bNif     = usaBilling ? txtNFBillingNif.getText().trim()     : null;
            String bAddr    = usaBilling ? txtNFBillingAddress.getText().trim() : null;
            String bCity    = usaBilling ? txtNFBillingCity.getText().trim()    : null;
            String bCp      = usaBilling ? txtNFBillingCp.getText().trim()      : null;
            String bCountry = usaBilling ? txtNFBillingCountry.getText().trim() : null;

            if (usaBilling && (bName == null || bName.isBlank())) {
                mostrarInfo("Indica el nombre o razón social de la empresa/tercero para la factura.");
                return;
            }

            if (invoiceEdicionId > 0) {
                // ── Modo edición
                FacturaDAO.eliminarTodasLineas(invoiceEdicionId);
                for (LineaFactura linea : lineasNuevaFactura) {
                    linea.setIdFactura(invoiceEdicionId);
                    FacturaDAO.añadirLinea(linea);
                }
                FacturaDAO.actualizarCabecera(invoiceEdicionId, metodo, notas);
                FacturaDAO.actualizarDatosFacturacion(invoiceEdicionId, bName, bNif, bAddr, bCity, bCp, bCountry);
                // Cancelar facturas secundarias de la misma reserva (consolidación)
                for (int dupId : invoicesParaFusionar) {
                    FacturaDAO.eliminarTodasLineas(dupId);
                    FacturaDAO.cancelar(dupId);
                }
                invoicesParaFusionar.clear();
                mostrarInfo("Ticket actualizado correctamente.");
            } else {
                // ── Modo creación
                Factura inv = new Factura();
                inv.setHuesped(cliente);
                inv.setFechaEmision(LocalDate.now());
                inv.setMetodoPago(metodo);
                inv.setNotas(notas);
                if (reservaSeleccionada != null) inv.setReserva(reservaSeleccionada);
                int invoiceId = FacturaDAO.crear(inv);
                FacturaDAO.actualizarCabecera(invoiceId, metodo, notas);
                FacturaDAO.actualizarDatosFacturacion(invoiceId, bName, bNif, bAddr, bCity, bCp, bCountry);
                for (LineaFactura linea : lineasNuevaFactura) {
                    linea.setIdFactura(invoiceId);
                    FacturaDAO.añadirLinea(linea);
                }
                mostrarInfo("Ticket creado correctamente.");
            }
            volverATickets();
        } catch (SQLException e) {
            mostrarError("Error guardando el ticket", e.getMessage());
        }
    }

    /** Muestra u oculta los campos de datos de facturación según el estado del CheckBox. */
    @FXML
    private void toggleBillingFields() {
        boolean activo = chkNFBillingOverride.isSelected();
        vboxNFBillingFields.setVisible(activo);
        vboxNFBillingFields.setManaged(activo);
    }

    /**
     * Comprueba que el cliente tiene NIF y dirección de facturación.
     * Si falta alguno abre un diálogo para completarlos y los persiste.
     */
    private void validarDatosFacturacion(Huesped guest) {
        boolean faltaNif      = guest.getNif()     == null || guest.getNif().isBlank();
        boolean faltaDireccion = guest.getDomicilio() == null || guest.getDomicilio().isBlank();
        if (!faltaNif && !faltaDireccion) return;

        // Construir mensaje de aviso
        StringBuilder aviso = new StringBuilder("El cliente ");
        aviso.append(guest.getNombre()).append(" ").append(guest.getApellidos());
        aviso.append(" no tiene ");
        if (faltaNif && faltaDireccion) aviso.append("NIF ni dirección de facturación.");
        else if (faltaNif)              aviso.append("NIF asignado.");
        else                            aviso.append("dirección de facturación.");
        aviso.append("\n\nCompleta los datos para que aparezcan en la factura.");

        javafx.scene.control.Alert alerta = new javafx.scene.control.Alert(
                javafx.scene.control.Alert.AlertType.WARNING);
        alerta.setTitle("Datos de facturación incompletos");
        alerta.setHeaderText(aviso.toString());
        alerta.setContentText("¿Deseas añadir los datos ahora?");
        alerta.getButtonTypes().setAll(ButtonType.YES, ButtonType.NO);
        alerta.showAndWait().ifPresent(resp -> {
            if (resp == ButtonType.YES) abrirDialogoCompletarCliente(guest);
        });
    }

    /** Diálogo para completar NIF y/o dirección de un cliente existente. */
    private void abrirDialogoCompletarCliente(Huesped guest) {
        javafx.scene.control.Dialog<Huesped> dialog = new javafx.scene.control.Dialog<>();
        dialog.setTitle("Completar datos de facturación");
        dialog.setHeaderText("Cliente: " + guest.getNombre() + " " + guest.getApellidos());

        ButtonType btnGuardar = new ButtonType("Guardar", javafx.scene.control.ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(btnGuardar, ButtonType.CANCEL);

        // Formulario
        javafx.scene.layout.GridPane grid = new javafx.scene.layout.GridPane();
        grid.setHgap(12);
        grid.setVgap(10);
        grid.setPadding(new javafx.geometry.Insets(20, 20, 10, 20));

        javafx.scene.control.TextField fNif       = new javafx.scene.control.TextField(guest.getNif() != null ? guest.getNif() : "");
        javafx.scene.control.TextField fDireccion = new javafx.scene.control.TextField(guest.getDomicilio() != null ? guest.getDomicilio() : "");
        javafx.scene.control.TextField fCiudad    = new javafx.scene.control.TextField(guest.getCiudad() != null ? guest.getCiudad() : "");
        javafx.scene.control.TextField fCp        = new javafx.scene.control.TextField(guest.getCodigoPostal() != null ? guest.getCodigoPostal() : "");
        javafx.scene.control.TextField fPais      = new javafx.scene.control.TextField(guest.getPais() != null ? guest.getPais() : "España");

        fNif.setPromptText("12345678A");
        fDireccion.setPromptText("Calle, número, piso...");
        fCiudad.setPromptText("Ciudad");
        fCp.setPromptText("28001");
        fPais.setPromptText("País");
        fDireccion.setPrefWidth(260);

        // Resaltar campos vacíos
        String estiloVacio = "-fx-border-color: #E74C3C; -fx-border-width: 1; -fx-border-radius: 4;";
        if (guest.getNif()     == null || guest.getNif().isBlank())     fNif.setStyle(estiloVacio);
        if (guest.getDomicilio() == null || guest.getDomicilio().isBlank()) fDireccion.setStyle(estiloVacio);

        int row = 0;
        grid.add(new javafx.scene.control.Label("NIF / DNI:"),   0, row); grid.add(fNif,       1, row++);
        grid.add(new javafx.scene.control.Label("Dirección:"),   0, row); grid.add(fDireccion, 1, row++);
        grid.add(new javafx.scene.control.Label("Ciudad:"),      0, row); grid.add(fCiudad,    1, row++);
        grid.add(new javafx.scene.control.Label("Código postal:"),0, row); grid.add(fCp,       1, row++);
        grid.add(new javafx.scene.control.Label("País:"),        0, row); grid.add(fPais,      1, row++);

        dialog.getDialogPane().setContent(grid);
        javafx.application.Platform.runLater(fNif::requestFocus);

        // Convertidor: construir Huesped actualizado
        dialog.setResultConverter(btn -> {
            if (btn != btnGuardar) return null;
            guest.setNif(fNif.getText().trim());
            guest.setDomicilio(fDireccion.getText().trim());
            guest.setCiudad(fCiudad.getText().trim());
            guest.setCodigoPostal(fCp.getText().trim());
            guest.setPais(fPais.getText().trim());
            return guest;
        });

        dialog.showAndWait().ifPresent(gActualizado -> {
            try {
                HuespedDAO.actualizar(gActualizado);
                // Refrescar el item en el combo para que el PDF lo use
                int idx = cmbNFCliente.getItems().indexOf(guest);
                if (idx >= 0) {
                    cmbNFCliente.getItems().set(idx, gActualizado);
                    cmbNFCliente.setValue(gActualizado);
                }
                mostrarInfo("Datos del cliente actualizados correctamente.");
            } catch (SQLException e) {
                mostrarError("Error guardando datos del cliente", e.getMessage());
            }
        });
    }

    private void configurarTablaNFLineas() {
        colNFDesc.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getDescripcion()));
        colNFCant.setCellValueFactory(c -> new SimpleStringProperty(
                c.getValue().getCantidad() != null ? c.getValue().getCantidad().stripTrailingZeros().toPlainString() : ""));
        colNFPrecio.setCellValueFactory(c -> new SimpleStringProperty(formatAmount(c.getValue().getPrecioUnitario())));
        colNFTotal.setCellValueFactory(c -> new SimpleStringProperty(formatAmount(c.getValue().getPrecioTotal())));
        colNFElim.setCellFactory(col -> new TableCell<>() {
            private final Button btn = new Button("✕");
            {
                btn.setStyle("-fx-background-color: transparent; -fx-text-fill: #E74C3C; " +
                        "-fx-cursor: hand; -fx-padding: 0 4 0 4;");
                btn.setOnAction(e -> {
                    LineaFactura linea = getTableView().getItems().get(getIndex());
                    lineasNuevaFactura.remove(linea);
                    tblNFLineas.setItems(FXCollections.observableArrayList(lineasNuevaFactura));
                    actualizarTotalesNF();
                });
            }
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : btn);
            }
        });
    }

    // ── Panel 3: Caja del Día

    private void configurarTablaCaja() {
        colCajaHora.setCellValueFactory(c -> new SimpleStringProperty(
                c.getValue().getFechaPago() != null ? c.getValue().getFechaPago().format(TIME_FMT) : ""));
        colCajaFactura.setCellValueFactory(c -> new SimpleStringProperty(
                c.getValue().getNumeroFactura() != null ? c.getValue().getNumeroFactura() : "#" + c.getValue().getIdFactura()));
        colCajaHuesped.setCellValueFactory(c -> new SimpleStringProperty(
                c.getValue().getNombreHuesped() != null ? c.getValue().getNombreHuesped() : ""));
        colCajaMetodo.setCellValueFactory(c -> new SimpleStringProperty(
                c.getValue().getMetodo() != null ? c.getValue().getMetodo().toSpanish() : ""));
        colCajaImporte.setCellValueFactory(c -> new SimpleStringProperty(formatAmount(c.getValue().getImporte())));
    }

    @FXML
    private void cargarCaja() {
        LocalDate hoy = LocalDate.now();
        try {
            List<Pago> pagos = PagoDAO.getPorFecha(hoy);
            tblCaja.setItems(FXCollections.observableArrayList(pagos));

            Map<Factura.MetodoPago, BigDecimal> resumen = PagoDAO.getResumenDiario(hoy);
            kpiEfectivo.setText(formatAmount(resumen.get(Factura.MetodoPago.EFECTIVO)));
            kpiTarjetaCredito.setText(formatAmount(resumen.get(Factura.MetodoPago.TARJETA_CREDITO)));
            kpiTarjetaDebito.setText(formatAmount(resumen.get(Factura.MetodoPago.TARJETA_DEBITO)));
            kpiTransferencia.setText(formatAmount(resumen.get(Factura.MetodoPago.TRANSFERENCIA)));

            BigDecimal total = resumen.values().stream()
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            kpiTotalDia.setText(formatAmount(total));
        } catch (SQLException e) {
            mostrarError("Error cargando caja", e.getMessage());
        }
    }

    @FXML
    private void arqueoCaja() {
        LocalDate hoy = LocalDate.now();
        try {
            Map<Factura.MetodoPago, BigDecimal> resumen = PagoDAO.getResumenDiario(hoy);
            StringBuilder sb = new StringBuilder();
            BigDecimal total = BigDecimal.ZERO;
            for (Factura.MetodoPago m : Factura.MetodoPago.values()) {
                BigDecimal v = resumen.getOrDefault(m, BigDecimal.ZERO);
                if (v.compareTo(BigDecimal.ZERO) > 0) {
                    sb.append(String.format("%-20s %s%n", m.toSpanish() + ":", formatAmount(v)));
                    total = total.add(v);
                }
            }
            sb.append(String.format("%-20s %s", "TOTAL:", formatAmount(total)));

            javafx.scene.control.Alert alert = new javafx.scene.control.Alert(
                    javafx.scene.control.Alert.AlertType.INFORMATION);
            alert.setTitle("Arqueo de caja");
            alert.setHeaderText("Resumen del día " + hoy.format(DATE_FMT));
            alert.setContentText(sb.toString());
            alert.showAndWait();
        } catch (SQLException e) {
            mostrarError("Error en arqueo", e.getMessage());
        }
    }

    // ── Panel 4: Control de Ingresos

    private void configurarTablaIngresos() {
        colIngNum.setCellValueFactory(c -> new SimpleStringProperty(
                c.getValue().getNumeroFactura() != null ? c.getValue().getNumeroFactura() : "#" + c.getValue().getId()));
        colIngFecha.setCellValueFactory(c -> new SimpleStringProperty(
                c.getValue().getFechaEmision() != null ? c.getValue().getFechaEmision().format(DATE_FMT) : ""));
        colIngHuesped.setCellValueFactory(c -> new SimpleStringProperty(
                c.getValue().getHuesped() != null
                        ? c.getValue().getHuesped().getNombre() + " " + c.getValue().getHuesped().getApellidos()
                        : ""));
        colIngTotal.setCellValueFactory(c -> new SimpleStringProperty(formatAmount(c.getValue().getImporteTotal())));
        colIngCobrado.setCellValueFactory(c -> new SimpleStringProperty(formatAmount(c.getValue().getImportePagado())));
        colIngEstado.setCellValueFactory(c -> new SimpleStringProperty(
                c.getValue().getEstado() != null ? c.getValue().getEstado().toSpanish() : ""));
    }

    @FXML
    private void cargarIngresos() {
        LocalDate desde = dpIngDesde.getValue();
        LocalDate hasta = dpIngHasta.getValue();
        if (desde == null || hasta == null) {
            mostrarInfo("Seleccione el rango de fechas.");
            return;
        }
        try {
            List<Factura> facturas = FacturaDAO.getPorRango(desde, hasta);
            tblIngresos.setItems(FXCollections.observableArrayList(facturas));

            BigDecimal totalFac = facturas.stream()
                    .map(f -> f.getImporteTotal() != null ? f.getImporteTotal() : BigDecimal.ZERO)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            BigDecimal totalCob = facturas.stream()
                    .map(f -> f.getImportePagado() != null ? f.getImportePagado() : BigDecimal.ZERO)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            BigDecimal totalPend = totalFac.subtract(totalCob);

            kpiTotalFacturado.setText(formatAmount(totalFac));
            kpiTotalCobrado.setText(formatAmount(totalCob));
            kpiTotalPendiente.setText(formatAmount(totalPend));
        } catch (SQLException e) {
            mostrarError("Error cargando ingresos", e.getMessage());
        }
    }

    // ── Panel 5: Exportación

    @FXML
    private void exportarCSV() {
        LocalDate desde = dpExpDesde.getValue();
        LocalDate hasta = dpExpHasta.getValue();
        if (desde == null || hasta == null) {
            mostrarInfo("Seleccione el rango de fechas.");
            return;
        }
        String tipo = cmbTipoExport.getValue();
        String sep  = cmbSeparador.getValue();

        FileChooser chooser = new FileChooser();
        chooser.setTitle("Guardar exportación CSV");
        chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("CSV", "*.csv"));
        chooser.setInitialFileName("pmsuite_export_" + desde + "_" + hasta + ".csv");
        File file = chooser.showSaveDialog(pnlExport.getScene().getWindow());
        if (file == null) return;

        try (BufferedWriter w = Files.newBufferedWriter(file.toPath(), StandardCharsets.UTF_8)) {
            // BOM for Excel compatibility
            w.write('\uFEFF');

            if (tipo.contains("Facturas")) {
                w.write(String.join(sep, "Nº Factura", "Fecha", "Huésped", "Subtotal",
                        "IVA", "Total", "Pagado", "Pendiente", "Estado"));
                w.newLine();
                List<Factura> facturas = FacturaDAO.getPorRango(desde, hasta);
                for (Factura f : facturas) {
                    String huesped = f.getHuesped() != null
                            ? f.getHuesped().getNombre() + " " + f.getHuesped().getApellidos() : "";
                    w.write(String.join(sep,
                            nvl(f.getNumeroFactura(), "#" + f.getId()),
                            f.getFechaEmision() != null ? f.getFechaEmision().format(DATE_FMT) : "",
                            huesped,
                            decimal(f.getSubtotal()),
                            decimal(f.getImporteImpuesto()),
                            decimal(f.getImporteTotal()),
                            decimal(f.getImportePagado()),
                            decimal(f.getImportePendiente()),
                            f.getEstado() != null ? f.getEstado().toSpanish() : ""
                    ));
                    w.newLine();
                }
                if (tipo.contains("Pagos")) w.newLine();
            }

            if (tipo.contains("Pagos")) {
                w.write(String.join(sep, "Fecha/Hora", "Factura", "Huésped", "Método", "Importe", "Referencia"));
                w.newLine();
                List<Pago> pagos = PagoDAO.getPorRango(desde, hasta);
                for (Pago p : pagos) {
                    w.write(String.join(sep,
                            p.getFechaPago() != null ? p.getFechaPago().format(DT_FMT) : "",
                            nvl(p.getNumeroFactura(), "#" + p.getIdFactura()),
                            nvl(p.getNombreHuesped(), ""),
                            p.getMetodo() != null ? p.getMetodo().toSpanish() : "",
                            decimal(p.getImporte()),
                            nvl(p.getReferencia(), "")
                    ));
                    w.newLine();
                }
            }

            lblExportEstado.setText("✓ Exportado correctamente: " + file.getName());
        } catch (IOException | SQLException e) {
            lblExportEstado.setText("✗ Error: " + e.getMessage());
            mostrarError("Error exportando", e.getMessage());
        }
    }

    // ── Helpers

    private String formatAmount(BigDecimal value) {
        if (value == null) return "0,00 €";
        return String.format("%,.2f €", value).replace(".", "X").replace(",", ".").replace("X", ",");
    }

    private String decimal(BigDecimal value) {
        if (value == null) return "0,00";
        return value.setScale(2, java.math.RoundingMode.HALF_UP)
                .toPlainString().replace(".", ",");
    }

    private String nvl(String value, String fallback) {
        return value != null ? value : fallback;
    }

    private void mostrarError(String titulo, String mensaje) {
        javafx.scene.control.Alert alert = new javafx.scene.control.Alert(
                javafx.scene.control.Alert.AlertType.ERROR);
        alert.setTitle(titulo);
        alert.setHeaderText(titulo);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }

    private void mostrarInfo(String mensaje) {
        javafx.scene.control.Alert alert = new javafx.scene.control.Alert(
                javafx.scene.control.Alert.AlertType.INFORMATION);
        alert.setTitle("Información");
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }
}

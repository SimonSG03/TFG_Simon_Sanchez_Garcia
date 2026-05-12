package es.simonsg.pmsuite.controller;

import es.simonsg.pmsuite.dao.*;
import es.simonsg.pmsuite.model.*;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.Alert;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.VBox;
import javafx.util.StringConverter;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class RecepcionController {

    // ── Referencia al controlador padre ────────────────────────────────────────
    private IndexController indexController;

    // ── Paneles ────────────────────────────────────────────────────────────────
    @FXML private VBox pnlCheckin;
    @FXML private VBox pnlCheckout;
    @FXML private VBox pnlNuevaReserva;
    @FXML private VBox pnlModificarReserva;
    @FXML private VBox pnlFichaHuesped;
    @FXML private VBox pnlAsignarHabitacion;
    @FXML private VBox pnlExtras;
    @FXML private VBox pnlPagos;
    @FXML private VBox pnlFacturacion;
    @FXML private VBox pnlHistorial;
    @FXML private VBox pnlDetalle;

    private List<VBox> allPanels;

    // ── Botones de navegación (sidebar) ───────────────────────────────────────
    @FXML private Button btnNavCheckin;
    @FXML private Button btnNavCheckout;
    @FXML private Button btnNavNuevaReserva;
    @FXML private Button btnNavModRes;
    @FXML private Button btnNavFichaHuesped;
    @FXML private Button btnNavAsignar;
    @FXML private Button btnNavExtras;
    @FXML private Button btnNavPagos;
    @FXML private Button btnNavFacturacion;
    @FXML private Button btnNavHistorial;
    @FXML private Button btnNavDetalle;

    private Button activeNavBtn;

    // ── Panel Detalle Reserva ──────────────────────────────────────────────────
    @FXML private TextField                         txtDetalleBuscar;
    @FXML private ComboBox<Reserva>             cmbDetalleReserva;
    @FXML private VBox                              vboxDetalleInfo;
    @FXML private Label                             lblDetalleHuesped;
    @FXML private Label                             lblDetalleDni;
    @FXML private Label                             lblDetalleNum;
    @FXML private Label                             lblDetalleFechas;
    @FXML private Label                             lblDetalleHab;
    @FXML private Label                             lblDetalleNoches;
    @FXML private TableView<LineaFactura>            tblDetalleLineas;
    @FXML private TableColumn<LineaFactura, String>  colDetDesc;
    @FXML private TableColumn<LineaFactura, String>  colDetCant;
    @FXML private TableColumn<LineaFactura, String>  colDetPrecio;
    @FXML private TableColumn<LineaFactura, String>  colDetTotal;
    @FXML private Label                             lblDetalleSubtotal;
    @FXML private Label                             lblDetalleIva;
    @FXML private Label                             lblDetalleTotalFinal;

    // ── Panel Check-in ─────────────────────────────────────────────────────────
    @FXML private Label lblCheckinInfo;
    @FXML private TextField txtCheckinSearch;
    @FXML private TableView<Reserva> tblCheckin;
    @FXML private TableColumn<Reserva, String> colCiNum;
    @FXML private TableColumn<Reserva, String> colCiHuesped;
    @FXML private TableColumn<Reserva, String> colCiHab;
    @FXML private TableColumn<Reserva, String> colCiFecha;
    @FXML private TableColumn<Reserva, String> colCiAdultos;
    @FXML private TableColumn<Reserva, String> colCiPeticiones;
    @FXML private TableColumn<Reserva, String> colCiEstado;

    // ── Panel Check-out ────────────────────────────────────────────────────────
    @FXML private Label lblCheckoutInfo;
    @FXML private TextField txtCheckoutSearch;
    @FXML private TableView<Reserva> tblCheckout;
    @FXML private TableColumn<Reserva, String> colCoNum;
    @FXML private TableColumn<Reserva, String> colCoHuesped;
    @FXML private TableColumn<Reserva, String> colCoHab;
    @FXML private TableColumn<Reserva, String> colCoFecha;
    @FXML private TableColumn<Reserva, String> colCoNoches;
    @FXML private TableColumn<Reserva, String> colCoTotal;

    // ── Panel Nueva Reserva ────────────────────────────────────────────────────
    @FXML private TextField txtBuscarHuesped;
    @FXML private ComboBox<Huesped> cmbHuesped;
    @FXML private ComboBox<Habitacion> cmbHabitacion;
    @FXML private DatePicker dpCheckinNueva;
    @FXML private DatePicker dpCheckoutNueva;
    @FXML private Spinner<Integer> spnAdultos;
    @FXML private Spinner<Integer> spnNinos;
    @FXML private Label lblPrecioTotal;
    @FXML private TextArea txtPeticiones;
    @FXML private TextArea txtNotasNueva;

    // ── Panel Modificar Reserva ────────────────────────────────────────────────
    @FXML private TextField txtBuscarReserva;
    @FXML private TableView<Reserva> tblReservas;
    @FXML private TableColumn<Reserva, String> colResNum;
    @FXML private TableColumn<Reserva, String> colResHuesped;
    @FXML private TableColumn<Reserva, String> colResEntrada;
    @FXML private TableColumn<Reserva, String> colResSalida;
    @FXML private TableColumn<Reserva, String> colResEstado;
    @FXML private DatePicker dpCiMod;
    @FXML private DatePicker dpCoMod;
    @FXML private ComboBox<Reserva.Estado> cmbEstadoMod;
    @FXML private Spinner<Integer> spnAdultosMod;
    @FXML private Spinner<Integer> spnNinosMod;
    @FXML private TextArea txtPeticionesMod;
    @FXML private TextArea txtNotasMod;

    // ── Panel Ficha Huésped ────────────────────────────────────────────────────
    @FXML private TextField txtBuscarFicha;
    @FXML private TableView<Huesped> tblFichaHuespedes;
    @FXML private TableColumn<Huesped, String> colFichaNif;
    @FXML private TableColumn<Huesped, String> colFichaNombre;
    @FXML private TableColumn<Huesped, String> colFichaEmail;
    @FXML private TableColumn<Huesped, String> colFichaTelefono;
    @FXML private TextField txtFichaNif;
    @FXML private TextField txtFichaNombre;
    @FXML private TextField txtFichaApellidos;
    @FXML private TextField txtFichaEmail;
    @FXML private TextField txtFichaTelefono;
    @FXML private TextField txtFichaNacionalidad;
    @FXML private TextField txtFichaPais;
    @FXML private TextField txtFichaDireccion;
    @FXML private TextField txtFichaCiudad;
    @FXML private TextField txtFichaCP;
    @FXML private DatePicker dpFichaNacimiento;
    @FXML private TextArea txtFichaNotas;

    // ── Panel Asignar Habitación ───────────────────────────────────────────────
    @FXML private TableView<Reserva> tblSinRoom;
    @FXML private TableColumn<Reserva, String> colSrNum;
    @FXML private TableColumn<Reserva, String> colSrHuesped;
    @FXML private TableColumn<Reserva, String> colSrEntrada;
    @FXML private TableView<Habitacion> tblRoomsDisp;
    @FXML private TableColumn<Habitacion, String> colRdNum;
    @FXML private TableColumn<Habitacion, String> colRdTipo;
    @FXML private TableColumn<Habitacion, String> colRdPlanta;
    @FXML private TableColumn<Habitacion, String> colRdPrecio;

    // ── Panel Extras ───────────────────────────────────────────────────────────
    @FXML private ComboBox<Reserva> cmbResExtras;
    @FXML private TableView<LineaFactura> tblExtrasLineas;
    @FXML private TableColumn<LineaFactura, String> colExtDesc;
    @FXML private TableColumn<LineaFactura, String> colExtCant;
    @FXML private TableColumn<LineaFactura, String> colExtPrecio;
    @FXML private TableColumn<LineaFactura, String> colExtTotal;
    @FXML private TableColumn<LineaFactura, String> colExtElim;
    @FXML private Label  lblExtFormTitle;
    @FXML private Button btnExtGuardar;
    @FXML private Button btnExtCancelar;

    /** Línea que se está editando actualmente (null = modo creación). */
    private LineaFactura lineaEditandoExt = null;
    @FXML private TextField txtExtDesc;
    @FXML private TextField txtExtCant;
    @FXML private TextField txtExtPrecio;

    // ── Panel Pagos ────────────────────────────────────────────────────────────
    @FXML private ComboBox<Factura> cmbFacturaPago;
    @FXML private Label lblPagoTotal;
    @FXML private Label lblPagoPagado;
    @FXML private Label lblPagoPendiente;
    @FXML private TextField txtPagoImporte;
    @FXML private ComboBox<Factura.MetodoPago> cmbPagoMetodo;
    @FXML private TextField txtPagoRef;
    @FXML private TextField txtPagoNotas;

    // ── Panel Facturación ──────────────────────────────────────────────────────
    @FXML private TableView<Factura> tblFacturas;
    @FXML private TableColumn<Factura, String> colFactNum;
    @FXML private TableColumn<Factura, String> colFactHuesped;
    @FXML private TableColumn<Factura, String> colFactReserva;
    @FXML private TableColumn<Factura, String> colFactTotal;
    @FXML private TableColumn<Factura, String> colFactPagado;
    @FXML private TableColumn<Factura, String> colFactEstado;
    @FXML private TableView<LineaFactura> tblFactLineas;
    @FXML private TableColumn<LineaFactura, String> colFLDesc;
    @FXML private TableColumn<LineaFactura, String> colFLCant;
    @FXML private TableColumn<LineaFactura, String> colFLPrecio;
    @FXML private TableColumn<LineaFactura, String> colFLTotal;

    // ── Panel Historial ────────────────────────────────────────────────────────
    @FXML private TextField txtBuscarHistorial;
    @FXML private Label lblHistNombre;
    @FXML private Label lblHistEmail;
    @FXML private Label lblHistEstancias;
    @FXML private Label lblHistGastado;
    @FXML private TableView<Reserva> tblHistorial;
    @FXML private TableColumn<Reserva, String> colHistNum;
    @FXML private TableColumn<Reserva, String> colHistEntrada;
    @FXML private TableColumn<Reserva, String> colHistSalida;
    @FXML private TableColumn<Reserva, String> colHistHab;
    @FXML private TableColumn<Reserva, String> colHistEstado;
    @FXML private TableColumn<Reserva, String> colHistTotal;

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final String EURO_FORMAT = "%.2f €";

    // INICIALIZACIÓN

    @FXML
    public void initialize() {
        allPanels = List.of(pnlCheckin, pnlCheckout, pnlNuevaReserva, pnlModificarReserva,
                pnlFichaHuesped, pnlAsignarHabitacion, pnlExtras, pnlPagos,
                pnlFacturacion, pnlHistorial, pnlDetalle);

        setupTablaCheckin();
        setupTablaCheckout();
        setupTablaReservas();
        setupTablaFicha();
        setupTablaAsignar();
        setupTablaExtras();
        setupTablaFacturacion();
        setupTablaHistorial();
        setupTablaDetalle();
        setupSpinners();
        setupCombos();

        // Panel inicial: Check-in
        mostrarCheckin();
    }

    // SETUP DE TABLAS Y CONTROLES

    private void setupTablaCheckin() {
        colCiNum.setCellValueFactory(d -> sp(d.getValue().getNumeroReserva()));
        colCiHuesped.setCellValueFactory(d -> sp(d.getValue().getHuesped() != null
                ? d.getValue().getHuesped().getNombreCompleto() : ""));
        colCiHab.setCellValueFactory(d -> sp(d.getValue().getHabitacion() != null
                ? "Hab. " + d.getValue().getHabitacion().getNumero() : "Sin asignar"));
        colCiFecha.setCellValueFactory(d -> sp(d.getValue().getFechaEntrada() != null
                ? d.getValue().getFechaEntrada().format(DATE_FMT) : ""));
        colCiAdultos.setCellValueFactory(d -> sp(String.valueOf(d.getValue().getAdultos())));
        colCiPeticiones.setCellValueFactory(d -> sp(
                d.getValue().getPeticionesEspeciales() != null ? d.getValue().getPeticionesEspeciales() : ""));
        colCiEstado.setCellValueFactory(d -> sp(d.getValue().getEstado() != null
                ? d.getValue().getEstado().toSpanish() : ""));
    }

    private void setupTablaCheckout() {
        colCoNum.setCellValueFactory(d -> sp(d.getValue().getNumeroReserva()));
        colCoHuesped.setCellValueFactory(d -> sp(d.getValue().getHuesped() != null
                ? d.getValue().getHuesped().getNombreCompleto() : ""));
        colCoHab.setCellValueFactory(d -> sp(d.getValue().getHabitacion() != null
                ? "Hab. " + d.getValue().getHabitacion().getNumero() : ""));
        colCoFecha.setCellValueFactory(d -> sp(d.getValue().getFechaSalida() != null
                ? d.getValue().getFechaSalida().format(DATE_FMT) : ""));
        colCoNoches.setCellValueFactory(d -> sp(String.valueOf(d.getValue().getNoches())));
        colCoTotal.setCellValueFactory(d -> sp(d.getValue().getPrecioTotal() != null
                ? String.format(EURO_FORMAT, d.getValue().getPrecioTotal()) : "—"));
    }

    private void setupTablaReservas() {
        colResNum.setCellValueFactory(d -> sp(d.getValue().getNumeroReserva()));
        colResHuesped.setCellValueFactory(d -> sp(d.getValue().getHuesped() != null
                ? d.getValue().getHuesped().getNombreCompleto() : ""));
        colResEntrada.setCellValueFactory(d -> sp(d.getValue().getFechaEntrada() != null
                ? d.getValue().getFechaEntrada().format(DATE_FMT) : ""));
        colResSalida.setCellValueFactory(d -> sp(d.getValue().getFechaSalida() != null
                ? d.getValue().getFechaSalida().format(DATE_FMT) : ""));
        colResEstado.setCellValueFactory(d -> sp(d.getValue().getEstado() != null
                ? d.getValue().getEstado().toSpanish() : ""));

        tblReservas.getSelectionModel().selectedItemProperty().addListener((obs, old, res) -> {
            if (res != null) rellenarFormModificarReserva(res);
        });
    }

    private void setupTablaFicha() {
        colFichaNif.setCellValueFactory(d -> sp(d.getValue().getNif() != null ? d.getValue().getNif() : ""));
        colFichaNombre.setCellValueFactory(d -> sp(d.getValue().getNombreCompleto()));
        colFichaEmail.setCellValueFactory(d -> sp(d.getValue().getEmail() != null ? d.getValue().getEmail() : ""));
        colFichaTelefono.setCellValueFactory(d -> sp(d.getValue().getTelefono() != null ? d.getValue().getTelefono() : ""));

        tblFichaHuespedes.getSelectionModel().selectedItemProperty().addListener((obs, old, g) -> {
            if (g != null) rellenarFormHuesped(g);
        });
    }

    private void setupTablaAsignar() {
        colSrNum.setCellValueFactory(d -> sp(d.getValue().getNumeroReserva()));
        colSrHuesped.setCellValueFactory(d -> sp(d.getValue().getHuesped() != null
                ? d.getValue().getHuesped().getNombreCompleto() : ""));
        colSrEntrada.setCellValueFactory(d -> sp(d.getValue().getFechaEntrada() != null
                ? d.getValue().getFechaEntrada().format(DATE_FMT) : ""));

        colRdNum.setCellValueFactory(d -> sp("Hab. " + d.getValue().getNumero()));
        colRdTipo.setCellValueFactory(d -> sp(d.getValue().getTipoHabitacion() != null
                ? d.getValue().getTipoHabitacion().getNombre() : ""));
        colRdPlanta.setCellValueFactory(d -> sp("Planta " + d.getValue().getPlanta()));
        colRdPrecio.setCellValueFactory(d -> sp(d.getValue().getTipoHabitacion() != null
                && d.getValue().getTipoHabitacion().getPrecioBase() != null
                ? String.format(EURO_FORMAT, d.getValue().getTipoHabitacion().getPrecioBase()) : ""));
    }

    private void setupTablaExtras() {
        colExtDesc.setCellValueFactory(d -> sp(d.getValue().getDescripcion()));
        colExtCant.setCellValueFactory(d -> sp(d.getValue().getCantidad() != null
                ? d.getValue().getCantidad().toPlainString() : ""));
        colExtPrecio.setCellValueFactory(d -> sp(d.getValue().getPrecioUnitario() != null
                ? String.format(EURO_FORMAT, d.getValue().getPrecioUnitario()) : ""));
        colExtTotal.setCellValueFactory(d -> sp(d.getValue().getPrecioTotal() != null
                ? String.format(EURO_FORMAT, d.getValue().getPrecioTotal()) : ""));

        // Columna de eliminar (opcional: solo si el FXML tiene fx:id="colExtElim")
        if (colExtElim != null) {
            colExtElim.setCellFactory(col -> new TableCell<>() {
                private final Button btnDel = new Button("✕");
                {
                    btnDel.setStyle("-fx-background-color: transparent; -fx-text-fill: #C0392B; "
                            + "-fx-cursor: hand; -fx-font-weight: bold;");
                    btnDel.setTooltip(new Tooltip("Eliminar línea"));
                    btnDel.setOnAction(e -> {
                        LineaFactura linea = getTableView().getItems().get(getIndex());
                        eliminarExtra(linea);
                    });
                }
                @Override
                protected void updateItem(String item, boolean empty) {
                    super.updateItem(item, empty);
                    setGraphic(empty ? null : btnDel);
                }
            });
        }

        // Clic en fila -> cargar datos en el formulario para editar
        tblExtrasLineas.getSelectionModel().selectedItemProperty().addListener((obs, old, sel) -> {
            if (sel != null) cargarExtraEnFormulario(sel);
        });

        cmbResExtras.getSelectionModel().selectedItemProperty().addListener((obs, old, res) -> {
            if (res != null) { cancelarEdicionExtra(); cargarLineasExtras(res); }
        });
    }

    /** Carga los datos de una línea en el formulario para editar. */
    private void cargarExtraEnFormulario(LineaFactura linea) {
        lineaEditandoExt = linea;
        txtExtDesc.setText(linea.getDescripcion() != null ? linea.getDescripcion() : "");
        txtExtCant.setText(linea.getCantidad() != null ? linea.getCantidad().toPlainString() : "1");
        txtExtPrecio.setText(linea.getPrecioUnitario() != null
                ? linea.getPrecioUnitario().setScale(2, java.math.RoundingMode.HALF_UP).toPlainString() : "0.00");
        if (lblExtFormTitle != null) lblExtFormTitle.setText("✏️  Editando concepto");
        if (btnExtGuardar  != null) btnExtGuardar.setText("✓ Guardar cambios");
        if (btnExtCancelar != null) { btnExtCancelar.setVisible(true); btnExtCancelar.setManaged(true); }
    }

    /** Cancela la edición y vuelve al modo "Añadir". */
    @FXML
    private void cancelarEdicionExtra() {
        lineaEditandoExt = null;
        txtExtDesc.clear();
        txtExtCant.clear();
        txtExtPrecio.clear();
        tblExtrasLineas.getSelectionModel().clearSelection();
        if (lblExtFormTitle != null) lblExtFormTitle.setText("Añadir concepto");
        if (btnExtGuardar  != null) btnExtGuardar.setText("+ Añadir");
        if (btnExtCancelar != null) { btnExtCancelar.setVisible(false); btnExtCancelar.setManaged(false); }
    }

    /** Elimina una línea de la factura y recarga la tabla. */
    private void eliminarExtra(LineaFactura linea) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Eliminar línea");
        confirm.setHeaderText("¿Eliminar \"" + linea.getDescripcion() + "\"?");
        confirm.setContentText("Esta acción no se puede deshacer.");
        confirm.showAndWait().ifPresent(r -> {
            if (r == ButtonType.OK) {
                try {
                    FacturaDAO.eliminarLinea(linea.getId(), linea.getIdFactura());
                    if (lineaEditandoExt != null && lineaEditandoExt.getId() == linea.getId()) {
                        cancelarEdicionExtra();
                    }
                    Reserva res = cmbResExtras.getValue();
                    if (res != null) cargarLineasExtras(res);
                } catch (Exception e) { showError("Error al eliminar la línea", e.getMessage()); }
            }
        });
    }

    private void setupTablaFacturacion() {
        colFactNum.setCellValueFactory(d -> sp(d.getValue().getNumeroFactura()));
        colFactHuesped.setCellValueFactory(d -> sp(d.getValue().getHuesped() != null
                ? d.getValue().getHuesped().getNombreCompleto() : ""));
        colFactReserva.setCellValueFactory(d -> sp(d.getValue().getReserva() != null
                ? d.getValue().getReserva().getNumeroReserva() : ""));
        colFactTotal.setCellValueFactory(d -> sp(d.getValue().getImporteTotal() != null
                ? String.format(EURO_FORMAT, d.getValue().getImporteTotal()) : ""));
        colFactPagado.setCellValueFactory(d -> sp(d.getValue().getImportePagado() != null
                ? String.format(EURO_FORMAT, d.getValue().getImportePagado()) : ""));
        colFactEstado.setCellValueFactory(d -> sp(d.getValue().getEstado() != null
                ? d.getValue().getEstado().toSpanish() : ""));

        colFLDesc.setCellValueFactory(d -> sp(d.getValue().getDescripcion()));
        colFLCant.setCellValueFactory(d -> sp(d.getValue().getCantidad() != null
                ? d.getValue().getCantidad().toPlainString() : ""));
        colFLPrecio.setCellValueFactory(d -> sp(d.getValue().getPrecioUnitario() != null
                ? String.format(EURO_FORMAT, d.getValue().getPrecioUnitario()) : ""));
        colFLTotal.setCellValueFactory(d -> sp(d.getValue().getPrecioTotal() != null
                ? String.format(EURO_FORMAT, d.getValue().getPrecioTotal()) : ""));

        tblFacturas.getSelectionModel().selectedItemProperty().addListener((obs, old, inv) -> {
            if (inv != null) cargarLineasFactura(inv);
        });

        cmbFacturaPago.getSelectionModel().selectedItemProperty().addListener((obs, old, inv) -> {
            if (inv != null) actualizarResumenPago(inv);
        });
    }

    private void setupTablaHistorial() {
        colHistNum.setCellValueFactory(d -> sp(d.getValue().getNumeroReserva()));
        colHistEntrada.setCellValueFactory(d -> sp(d.getValue().getFechaEntrada() != null
                ? d.getValue().getFechaEntrada().format(DATE_FMT) : ""));
        colHistSalida.setCellValueFactory(d -> sp(d.getValue().getFechaSalida() != null
                ? d.getValue().getFechaSalida().format(DATE_FMT) : ""));
        colHistHab.setCellValueFactory(d -> sp(d.getValue().getHabitacion() != null
                ? "Hab. " + d.getValue().getHabitacion().getNumero() : ""));
        colHistEstado.setCellValueFactory(d -> sp(d.getValue().getEstado() != null
                ? d.getValue().getEstado().toSpanish() : ""));
        colHistTotal.setCellValueFactory(d -> sp(d.getValue().getPrecioTotal() != null
                ? String.format(EURO_FORMAT, d.getValue().getPrecioTotal()) : ""));
    }

    private void setupSpinners() {
        spnAdultos.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 20, 1));
        spnNinos.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 20, 0));
        spnAdultosMod.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 20, 1));
        spnNinosMod.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 20, 0));
    }

    private void setupCombos() {
        // Combo de estado de reserva
        cmbEstadoMod.getItems().setAll(Reserva.Estado.values());
        cmbEstadoMod.setConverter(new StringConverter<>() {
            @Override public String toString(Reserva.Estado s) { return s != null ? s.toSpanish() : ""; }
            @Override public Reserva.Estado fromString(String s) { return null; }
        });

        // Combo de método de pago
        cmbPagoMetodo.getItems().setAll(Factura.MetodoPago.values());
        cmbPagoMetodo.setConverter(new StringConverter<>() {
            @Override public String toString(Factura.MetodoPago m) { return m != null ? m.toSpanish() : ""; }
            @Override public Factura.MetodoPago fromString(String s) { return null; }
        });

        // Fechas por defecto para nueva reserva
        dpCheckinNueva.setValue(LocalDate.now());
        dpCheckoutNueva.setValue(LocalDate.now().plusDays(1));

        // Actualizar precio cuando cambian fechas o habitación
        dpCheckinNueva.valueProperty().addListener((o, ov, nv) -> recalcularPrecio());
        dpCheckoutNueva.valueProperty().addListener((o, ov, nv) -> recalcularPrecio());
        cmbHabitacion.getSelectionModel().selectedItemProperty().addListener((o, ov, nv) -> recalcularPrecio());
    }

    // NAVEGACIÓN


    @FXML public void mostrarCheckin()           { showPanel(pnlCheckin, btnNavCheckin);           cargarCheckins(); }
    @FXML public void mostrarCheckout()          { showPanel(pnlCheckout, btnNavCheckout);          cargarCheckouts(); }
    @FXML public void mostrarNuevaReserva()      { showPanel(pnlNuevaReserva, btnNavNuevaReserva);  cargarHabitacionesDisponibles(); }
    @FXML public void mostrarModificarReserva()  { showPanel(pnlModificarReserva, btnNavModRes); }
    @FXML public void mostrarFichaHuesped()      { showPanel(pnlFichaHuesped, btnNavFichaHuesped);  limpiarFormHuesped(); }
    @FXML public void mostrarAsignarHabitacion() { showPanel(pnlAsignarHabitacion, btnNavAsignar);  cargarAsignar(); }
    @FXML public void mostrarExtras()            { showPanel(pnlExtras, btnNavExtras);              cargarResActivas(); }
    @FXML public void mostrarPagos()             { showPanel(pnlPagos, btnNavPagos);               cargarFacturasPendientes(); }
    @FXML public void mostrarFacturacion()       { showPanel(pnlFacturacion, btnNavFacturacion);    cargarFacturas(); }
    @FXML public void mostrarHistorial()         { showPanel(pnlHistorial, btnNavHistorial); }
    @FXML public void mostrarDetalle()           { showPanel(pnlDetalle,   btnNavDetalle); }

    private void showPanel(VBox panel, Button navBtn) {
        allPanels.forEach(p -> { p.setVisible(false); p.setManaged(false); });
        panel.setVisible(true);
        panel.setManaged(true);

        if (activeNavBtn != null) activeNavBtn.getStyleClass().remove("nav-btn-active");
        navBtn.getStyleClass().add("nav-btn-active");
        activeNavBtn = navBtn;
    }

    // PANEL CHECK-IN

    private void cargarCheckins() {
        try {
            List<Reserva> lista = ReservaDAO.getLlegadasHoy();
            tblCheckin.setItems(FXCollections.observableArrayList(lista));
            lblCheckinInfo.setText("Llegadas hoy: " + lista.size());
        } catch (Exception e) {
            showError("Error al cargar llegadas", e.getMessage());
        }
    }

    @FXML private void buscarCheckin() {
        String q = txtCheckinSearch.getText().trim();
        if (q.isEmpty()) { cargarCheckins(); return; }
        try {
            tblCheckin.setItems(FXCollections.observableArrayList(ReservaDAO.buscar(q)));
        } catch (Exception e) { showError("Error en búsqueda", e.getMessage()); }
    }

    @FXML private void realizarCheckin() {
        Reserva sel = tblCheckin.getSelectionModel().getSelectedItem();
        if (sel == null) { showWarning("Selecciona una reserva para hacer el check-in."); return; }
        if (sel.getHabitacion() == null) {
            showWarning("La reserva no tiene habitación asignada. Asígnala antes de hacer el check-in.");
            return;
        }
        try {
            ReservaDAO.realizarCheckin(sel.getId());
            showInfo("Check-in realizado", "Check-in completado para " + sel.getHuesped().getNombreCompleto()
                    + "\nHabitación: " + sel.getHabitacion().getNumero());
            cargarCheckins();
        } catch (Exception e) { showError("Error al realizar el check-in", e.getMessage()); }
    }

    // PANEL CHECK-OUT

    private void cargarCheckouts() {
        try {
            List<Reserva> lista = ReservaDAO.getSalidasHoy();
            tblCheckout.setItems(FXCollections.observableArrayList(lista));
            lblCheckoutInfo.setText("Salidas hoy: " + lista.size());
        } catch (Exception e) { showError("Error al cargar salidas", e.getMessage()); }
    }

    @FXML private void buscarCheckout() {
        String q = txtCheckoutSearch.getText().trim();
        if (q.isEmpty()) { cargarCheckouts(); return; }
        try {
            tblCheckout.setItems(FXCollections.observableArrayList(ReservaDAO.buscarRegistrados(q)));
        } catch (Exception e) { showError("Error en búsqueda", e.getMessage()); }
    }

    @FXML private void realizarCheckout() {
        Reserva sel = tblCheckout.getSelectionModel().getSelectedItem();
        if (sel == null) { showWarning("Selecciona una reserva para hacer el check-out."); return; }
        try {
            BigDecimal total = sel.getPrecioTotal() != null ? sel.getPrecioTotal() : BigDecimal.ZERO;

            // Si la reserva no está CHECKED_IN, hacer check-in implícito
            if (sel.getEstado() != null
                    && sel.getEstado() != Reserva.Estado.REGISTRADA) {
                ReservaDAO.realizarCheckin(sel.getId());
            }

            // Buscar o crear el ticket de la reserva
            Factura existente = FacturaDAO.getPorReserva(sel.getId());
            int invoiceId;
            if (existente == null) {
                Factura inv = new Factura();
                inv.setReserva(sel);
                inv.setHuesped(sel.getHuesped());
                inv.setFechaEmision(LocalDate.now());
                invoiceId = FacturaDAO.crear(inv);
            } else {
                invoiceId = existente.getId();
            }

            // Añadir la línea de estancia si no existe ya en el ticket
            boolean yaHayEstancia = FacturaDAO.getLineas(invoiceId).stream()
                    .anyMatch(l -> l.getDescripcion() != null
                            && l.getDescripcion().startsWith("Estancia ·"));
            if (!yaHayEstancia) {
                long nights = sel.getNoches();
                BigDecimal pricePerNight = nights > 0
                        ? total.divide(BigDecimal.valueOf(nights), 2, RoundingMode.HALF_UP)
                        : total;
                FacturaDAO.añadirLinea(new LineaFactura(invoiceId,
                        "Estancia · " + nights + " noche(s)",
                        BigDecimal.valueOf(nights), pricePerNight));
            }

            ReservaDAO.realizarCheckout(sel.getId(), total);
            // Ir directamente a Administración > Pagos con la factura preseleccionada
            if (indexController != null) {
                indexController.abrirAdministracionEnPagos(sel.getId());
            } else {
                cargarCheckouts();
            }
        } catch (Exception e) { showError("Error al realizar el check-out", e.getMessage()); }
    }

    // PANEL NUEVA RESERVA

    private void cargarHabitacionesDisponibles() {
        try {
            cmbHabitacion.setItems(FXCollections.observableArrayList(HabitacionDAO.getDisponibles()));
        } catch (Exception e) { showError("Error al cargar habitaciones", e.getMessage()); }
    }

    @FXML private void buscarHuesped() {
        String q = txtBuscarHuesped.getText().trim();
        if (q.isEmpty()) return;
        try {
            cmbHuesped.setItems(FXCollections.observableArrayList(HuespedDAO.buscar(q)));
            if (!cmbHuesped.getItems().isEmpty()) cmbHuesped.show();
        } catch (Exception e) { showError("Error al buscar huésped", e.getMessage()); }
    }

    private void recalcularPrecio() {
        Habitacion room = cmbHabitacion.getValue();
        LocalDate ci = dpCheckinNueva.getValue();
        LocalDate co = dpCheckoutNueva.getValue();
        if (room == null || ci == null || co == null || !co.isAfter(ci)) {
            lblPrecioTotal.setText("—");
            return;
        }
        long nights = java.time.temporal.ChronoUnit.DAYS.between(ci, co);
        BigDecimal base = room.getTipoHabitacion() != null && room.getTipoHabitacion().getPrecioBase() != null
                ? room.getTipoHabitacion().getPrecioBase() : BigDecimal.ZERO;
        BigDecimal total = base.multiply(BigDecimal.valueOf(nights));
        lblPrecioTotal.setText(String.format("%.2f €  (%d noches × %.2f €/noche)", total, nights, base));
    }

    @FXML private void crearReserva() {
        Huesped guest = cmbHuesped.getValue();
        Habitacion room = cmbHabitacion.getValue();
        LocalDate ci = dpCheckinNueva.getValue();
        LocalDate co = dpCheckoutNueva.getValue();

        if (guest == null) { showWarning("Selecciona un huésped."); return; }
        if (ci == null || co == null || !co.isAfter(ci)) {
            showWarning("Las fechas de entrada y salida no son válidas.");
            return;
        }

        try {
            Reserva res = new Reserva();
            res.setHuesped(guest);
            res.setHabitacion(room);
            res.setFechaEntrada(ci);
            res.setFechaSalida(co);
            res.setAdultos(spnAdultos.getValue());
            res.setNinos(spnNinos.getValue());
            res.setEstado(Reserva.Estado.CONFIRMADA);
            res.setPeticionesEspeciales(txtPeticiones.getText());
            res.setNotas(txtNotasNueva.getText());

            // Calcular precio
            if (room != null && room.getTipoHabitacion() != null && room.getTipoHabitacion().getPrecioBase() != null) {
                long nights = java.time.temporal.ChronoUnit.DAYS.between(ci, co);
                res.setPrecioTotal(room.getTipoHabitacion().getPrecioBase().multiply(BigDecimal.valueOf(nights)));
            } else {
                res.setPrecioTotal(BigDecimal.ZERO);
            }
            res.setDepositoPagado(BigDecimal.ZERO);

            int id = ReservaDAO.crear(res);
            showInfo("Reserva creada", "Reserva creada correctamente.\nID: " + id);
            limpiarFormNuevaReserva();
            cargarHabitacionesDisponibles();
        } catch (Exception e) { showError("Error al crear la reserva", e.getMessage()); }
    }

    private void limpiarFormNuevaReserva() {
        cmbHuesped.getSelectionModel().clearSelection();
        cmbHabitacion.getSelectionModel().clearSelection();
        dpCheckinNueva.setValue(LocalDate.now());
        dpCheckoutNueva.setValue(LocalDate.now().plusDays(1));
        spnAdultos.getValueFactory().setValue(1);
        spnNinos.getValueFactory().setValue(0);
        txtPeticiones.clear();
        txtNotasNueva.clear();
        lblPrecioTotal.setText("—");
    }

    // PANEL MODIFICAR RESERVA

    @FXML private void buscarReserva() {
        String q = txtBuscarReserva.getText().trim();
        if (q.isEmpty()) return;
        try {
            tblReservas.setItems(FXCollections.observableArrayList(ReservaDAO.buscar(q)));
        } catch (Exception e) { showError("Error en búsqueda", e.getMessage()); }
    }

    private void rellenarFormModificarReserva(Reserva res) {
        dpCiMod.setValue(res.getFechaEntrada());
        dpCoMod.setValue(res.getFechaSalida());
        cmbEstadoMod.setValue(res.getEstado());
        spnAdultosMod.getValueFactory().setValue(res.getAdultos());
        spnNinosMod.getValueFactory().setValue(res.getNinos());
        txtPeticionesMod.setText(res.getPeticionesEspeciales() != null ? res.getPeticionesEspeciales() : "");
        txtNotasMod.setText(res.getNotas() != null ? res.getNotas() : "");
    }

    @FXML private void guardarModificacion() {
        Reserva sel = tblReservas.getSelectionModel().getSelectedItem();
        if (sel == null) { showWarning("Selecciona una reserva para modificar."); return; }
        LocalDate ci = dpCiMod.getValue();
        LocalDate co = dpCoMod.getValue();
        if (ci == null || co == null || !co.isAfter(ci)) {
            showWarning("Las fechas no son válidas.");
            return;
        }
        try {
            sel.setFechaEntrada(ci);
            sel.setFechaSalida(co);
            sel.setEstado(cmbEstadoMod.getValue());
            sel.setAdultos(spnAdultosMod.getValue());
            sel.setNinos(spnNinosMod.getValue());
            sel.setPeticionesEspeciales(txtPeticionesMod.getText());
            sel.setNotas(txtNotasMod.getText());
            ReservaDAO.actualizar(sel);
            showInfo("Reserva actualizada", "Los cambios se han guardado correctamente.");
            buscarReserva();
        } catch (Exception e) { showError("Error al guardar", e.getMessage()); }
    }

    @FXML private void cancelarReserva() {
        Reserva sel = tblReservas.getSelectionModel().getSelectedItem();
        if (sel == null) { showWarning("Selecciona una reserva para cancelar."); return; }
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION,
                "¿Cancelar la reserva " + sel.getNumeroReserva() + "?",
                ButtonType.YES, ButtonType.NO);
        confirm.setTitle("Confirmar cancelación");
        confirm.showAndWait().ifPresent(bt -> {
            if (bt == ButtonType.YES) {
                try {
                    sel.setEstado(Reserva.Estado.CANCELADA);
                    ReservaDAO.actualizar(sel);
                    buscarReserva();
                } catch (Exception e) { showError("Error al cancelar", e.getMessage()); }
            }
        });
    }

    // PANEL FICHA HUÉSPED

    @FXML private void buscarFicha() {
        String q = txtBuscarFicha.getText().trim();
        if (q.isEmpty()) return;
        try {
            tblFichaHuespedes.setItems(FXCollections.observableArrayList(HuespedDAO.buscar(q)));
        } catch (Exception e) { showError("Error en búsqueda", e.getMessage()); }
    }

    private void rellenarFormHuesped(Huesped g) {
        txtFichaNif.setText(g.getNif() != null ? g.getNif() : "");
        txtFichaNombre.setText(g.getNombre() != null ? g.getNombre() : "");
        txtFichaApellidos.setText(g.getApellidos() != null ? g.getApellidos() : "");
        txtFichaEmail.setText(g.getEmail() != null ? g.getEmail() : "");
        txtFichaTelefono.setText(g.getTelefono() != null ? g.getTelefono() : "");
        txtFichaNacionalidad.setText(g.getNacionalidad() != null ? g.getNacionalidad() : "");
        txtFichaPais.setText(g.getPais() != null ? g.getPais() : "");
        txtFichaDireccion.setText(g.getDomicilio() != null ? g.getDomicilio() : "");
        txtFichaCiudad.setText(g.getCiudad() != null ? g.getCiudad() : "");
        txtFichaCP.setText(g.getCodigoPostal() != null ? g.getCodigoPostal() : "");
        dpFichaNacimiento.setValue(g.getFechaNacimiento());
        txtFichaNotas.setText(g.getNotas() != null ? g.getNotas() : "");
    }

    @FXML private void nuevoHuesped() { limpiarFormHuesped(); tblFichaHuespedes.getSelectionModel().clearSelection(); }

    private void limpiarFormHuesped() {
        txtFichaNif.clear(); txtFichaNombre.clear(); txtFichaApellidos.clear();
        txtFichaEmail.clear(); txtFichaTelefono.clear(); txtFichaNacionalidad.clear();
        txtFichaPais.clear(); txtFichaDireccion.clear(); txtFichaCiudad.clear();
        txtFichaCP.clear(); dpFichaNacimiento.setValue(null); txtFichaNotas.clear();
    }

    @FXML private void guardarHuesped() {
        if (txtFichaNombre.getText().isBlank() || txtFichaApellidos.getText().isBlank()) {
            showWarning("El nombre y los apellidos son obligatorios.");
            return;
        }
        try {
            Huesped sel = tblFichaHuespedes.getSelectionModel().getSelectedItem();
            Huesped g = sel != null ? sel : new Huesped();
            g.setNif(txtFichaNif.getText().trim());
            g.setNombre(txtFichaNombre.getText().trim());
            g.setApellidos(txtFichaApellidos.getText().trim());
            g.setEmail(txtFichaEmail.getText().trim());
            g.setTelefono(txtFichaTelefono.getText().trim());
            g.setNacionalidad(txtFichaNacionalidad.getText().trim());
            g.setPais(txtFichaPais.getText().trim());
            g.setDomicilio(txtFichaDireccion.getText().trim());
            g.setCiudad(txtFichaCiudad.getText().trim());
            g.setCodigoPostal(txtFichaCP.getText().trim());
            g.setFechaNacimiento(dpFichaNacimiento.getValue());
            g.setNotas(txtFichaNotas.getText().trim());
            HuespedDAO.guardar(g);
            showInfo("Guardado", "Ficha del huésped guardada correctamente.");
            buscarFicha();
        } catch (Exception e) { showError("Error al guardar huésped", e.getMessage()); }
    }

    // PANEL ASIGNAR HABITACIÓN

    private void cargarAsignar() {
        try {
            tblSinRoom.setItems(FXCollections.observableArrayList(ReservaDAO.getSinHabitacion()));
            tblRoomsDisp.setItems(FXCollections.observableArrayList(HabitacionDAO.getDisponibles()));
        } catch (Exception e) { showError("Error al cargar datos", e.getMessage()); }
    }

    @FXML private void asignarHabitacion() {
        Reserva res = tblSinRoom.getSelectionModel().getSelectedItem();
        Habitacion room = tblRoomsDisp.getSelectionModel().getSelectedItem();
        if (res == null) { showWarning("Selecciona una reserva."); return; }
        if (room == null) { showWarning("Selecciona una habitación."); return; }
        try {
            ReservaDAO.asignarHabitacion(res.getId(), room.getId());
            showInfo("Habitación asignada", "Habitación " + room.getNumero()
                    + " asignada a la reserva " + res.getNumeroReserva());
            cargarAsignar();
        } catch (Exception e) { showError("Error al asignar habitación", e.getMessage()); }
    }

    // PANEL EXTRAS

    private void cargarResActivas() {
        try {
            cmbResExtras.setItems(FXCollections.observableArrayList(ReservaDAO.obtenerActivas()));
        } catch (Exception e) { showError("Error al cargar reservas", e.getMessage()); }
    }

    private void cargarLineasExtras(Reserva res) {
        try {
            // Muestra TODAS las líneas de TODAS las facturas de la reserva
            List<LineaFactura> lineas = FacturaDAO.getLineasPorReserva(res.getId());
            tblExtrasLineas.setItems(FXCollections.observableArrayList(lineas));
        } catch (Exception e) { showError("Error al cargar líneas", e.getMessage()); }
    }

    /** Guarda el extra: crea uno nuevo o actualiza el seleccionado según el modo activo. */
    @FXML
    private void guardarExtra() {
        Reserva res = cmbResExtras.getValue();
        if (res == null) { showWarning("Selecciona una reserva."); return; }
        String desc = txtExtDesc.getText().trim();
        String cantStr = txtExtCant.getText().trim();
        String precioStr = txtExtPrecio.getText().trim();
        if (desc.isEmpty() || cantStr.isEmpty() || precioStr.isEmpty()) {
            showWarning("Rellena descripción, cantidad y precio.");
            return;
        }
        try {
            BigDecimal cant = new BigDecimal(cantStr.replace(",", "."));
            BigDecimal precio = new BigDecimal(precioStr.replace(",", "."));

            if (lineaEditandoExt != null) {
                // ── Modo edición: actualizar la línea existente ──────────
                lineaEditandoExt.setDescripcion(desc);
                lineaEditandoExt.setCantidad(cant);
                lineaEditandoExt.setPrecioUnitario(precio);
                FacturaDAO.actualizarLinea(lineaEditandoExt);
            } else {
                // ── Modo creación: siempre al ticket de la reserva ──────
                // Regla: UNA reserva = UN ticket. Nunca crear factura separada.
                Factura inv = FacturaDAO.getPorReserva(res.getId());
                int invoiceId;
                if (inv == null) {
                    // Aún no existe ticket para esta reserva → crearlo
                    Factura newInv = new Factura();
                    newInv.setReserva(res);
                    newInv.setHuesped(res.getHuesped());
                    newInv.setFechaEmision(LocalDate.now());
                    invoiceId = FacturaDAO.crear(newInv);
                } else {
                    invoiceId = inv.getId();
                }
                FacturaDAO.añadirLinea(new LineaFactura(invoiceId, desc, cant, precio));
            }

            cancelarEdicionExtra();
            cargarLineasExtras(res);
        } catch (NumberFormatException e) {
            showWarning("Cantidad y precio deben ser números válidos.");
        } catch (Exception e) { showError("Error al guardar el extra", e.getMessage()); }
    }

    // PANEL PAGOS

    private void cargarFacturasPendientes() {
        try {
            cmbFacturaPago.setItems(FXCollections.observableArrayList(FacturaDAO.getPendientesYParciales()));
        } catch (Exception e) { showError("Error al cargar facturas", e.getMessage()); }
    }

    private void actualizarResumenPago(Factura inv) {
        BigDecimal total = inv.getImporteTotal() != null ? inv.getImporteTotal() : BigDecimal.ZERO;
        BigDecimal pagado = inv.getImportePagado() != null ? inv.getImportePagado() : BigDecimal.ZERO;
        BigDecimal pendiente = total.subtract(pagado);
        lblPagoTotal.setText(String.format("%.2f €", total));
        lblPagoPagado.setText(String.format("%.2f €", pagado));
        lblPagoPendiente.setText(String.format("%.2f €", pendiente));
        txtPagoImporte.setText(pendiente.compareTo(BigDecimal.ZERO) > 0
                ? pendiente.setScale(2, RoundingMode.HALF_UP).toPlainString() : "");
    }

    @FXML private void registrarPago() {
        Factura inv = cmbFacturaPago.getValue();
        Factura.MetodoPago metodo = cmbPagoMetodo.getValue();
        String importeStr = txtPagoImporte.getText().trim();

        if (inv == null) { showWarning("Selecciona una factura."); return; }
        if (metodo == null) { showWarning("Selecciona un método de pago."); return; }
        if (importeStr.isEmpty()) { showWarning("Introduce el importe."); return; }

        try {
            BigDecimal importe = new BigDecimal(importeStr.replace(",", "."));
            Pago pago = new Pago();
            pago.setIdFactura(inv.getId());
            pago.setImporte(importe);
            pago.setMetodo(metodo);
            pago.setReferencia(txtPagoRef.getText().trim());
            pago.setNotas(txtPagoNotas.getText().trim());
            PagoDAO.añadirPago(pago);
            showInfo("Pago registrado", String.format("Pago de %.2f € registrado correctamente.", importe));
            txtPagoImporte.clear(); txtPagoRef.clear(); txtPagoNotas.clear();
            cargarFacturasPendientes();
        } catch (NumberFormatException e) {
            showWarning("El importe no es un número válido.");
        } catch (Exception e) { showError("Error al registrar el pago", e.getMessage()); }
    }

    // PANEL FACTURACIÓN

    private void cargarFacturas() {
        try {
            tblFacturas.setItems(FXCollections.observableArrayList(FacturaDAO.obtenerTodas()));
        } catch (Exception e) { showError("Error al cargar facturas", e.getMessage()); }
    }

    private void cargarLineasFactura(Factura inv) {
        try {
            tblFactLineas.setItems(FXCollections.observableArrayList(FacturaDAO.getLineas(inv.getId())));
        } catch (Exception e) { showError("Error al cargar líneas", e.getMessage()); }
    }

    // PANEL HISTORIAL

    @FXML private void buscarHistorial() {
        String q = txtBuscarHistorial.getText().trim();
        if (q.isEmpty()) return;
        try {
            List<Huesped> guests = HuespedDAO.buscar(q);
            if (guests.isEmpty()) { showWarning("No se encontró ningún huésped."); return; }
            Huesped g = guests.getFirst();
            lblHistNombre.setText(g.getNombreCompleto());
            lblHistEmail.setText(g.getEmail() != null ? g.getEmail() : "—");

            List<Reserva> hist = ReservaDAO.obtenerPorHuesped(g.getId());
            tblHistorial.setItems(FXCollections.observableArrayList(hist));
            lblHistEstancias.setText(String.valueOf(hist.size()));
            BigDecimal totalGastado = hist.stream()
                    .filter(r -> r.getPrecioTotal() != null)
                    .map(Reserva::getPrecioTotal)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            lblHistGastado.setText(String.format("%.2f €", totalGastado));
        } catch (Exception e) { showError("Error al buscar historial", e.getMessage()); }
    }

    // UTILIDADES

    public void setIndexController(IndexController ic) {
        this.indexController = ic;
    }

    @FXML
    private void volverInicio() {
        indexController.mostrarInicio();
    }

    private SimpleStringProperty sp(String value) { return new SimpleStringProperty(value != null ? value : ""); }

    private void showError(String title, String msg) {
        Alert a = new Alert(Alert.AlertType.ERROR, msg, ButtonType.OK);
        a.setTitle(title);
        a.setHeaderText(title);
        a.showAndWait();
    }

    private void showWarning(String msg) {
        Alert a = new Alert(Alert.AlertType.WARNING, msg, ButtonType.OK);
        a.setTitle("Atención");
        a.showAndWait();
    }

    private void showInfo(String title, String msg) {
        Alert a = new Alert(Alert.AlertType.INFORMATION, msg, ButtonType.OK);
        a.setTitle(title);
        a.setHeaderText(title);
        a.showAndWait();
    }

    // PANEL DETALLE RESERVA

    private void setupTablaDetalle() {
        colDetDesc.setCellValueFactory(d -> sp(d.getValue().getDescripcion()));
        colDetCant.setCellValueFactory(d -> sp(d.getValue().getCantidad() != null
                ? d.getValue().getCantidad().toPlainString() : ""));
        colDetPrecio.setCellValueFactory(d -> sp(d.getValue().getPrecioUnitario() != null
                ? String.format(EURO_FORMAT, d.getValue().getPrecioUnitario()) : ""));
        colDetTotal.setCellValueFactory(d -> sp(d.getValue().getPrecioTotal() != null
                ? String.format(EURO_FORMAT, d.getValue().getPrecioTotal()) : ""));

        cmbDetalleReserva.setConverter(new javafx.util.StringConverter<>() {
            @Override public String toString(Reserva r) {
                if (r == null) return "";
                String guest = r.getHuesped() != null ? r.getHuesped().getNombreCompleto() : "—";
                return r.getNumeroReserva() + "  ·  " + guest;
            }
            @Override public Reserva fromString(String s) { return null; }
        });

        cmbDetalleReserva.getSelectionModel().selectedItemProperty()
                .addListener((obs, old, sel) -> { if (sel != null) cargarDetalleReserva(sel); });
    }

    @FXML
    private void buscarDetalleReserva() {
        String q = txtDetalleBuscar.getText().trim();
        try {
            List<Reserva> resultados = ReservaDAO.buscar(q);
            cmbDetalleReserva.setItems(FXCollections.observableArrayList(resultados));
            if (resultados.size() == 1) cmbDetalleReserva.getSelectionModel().selectFirst();
        } catch (Exception e) { showError("Error en búsqueda", e.getMessage()); }
    }

    private void cargarDetalleReserva(Reserva res) {
        try {
            // ── Info huésped ──
            Huesped g = res.getHuesped();
            lblDetalleHuesped.setText(g != null ? g.getNombreCompleto() : "—");
            lblDetalleDni.setText(g != null && g.getNif() != null ? "DNI/NIF: " + g.getNif() : "Sin DNI registrado");

            // ── Info reserva ──
            lblDetalleNum.setText(res.getNumeroReserva() != null ? res.getNumeroReserva() : "—");
            DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd/MM/yyyy");
            String entrada = res.getFechaEntrada() != null ? res.getFechaEntrada().format(fmt) : "—";
            String salida  = res.getFechaSalida() != null ? res.getFechaSalida().format(fmt) : "—";
            lblDetalleFechas.setText(entrada + " → " + salida);

            // ── Info habitación ──
            lblDetalleHab.setText(res.getHabitacion() != null ? "Hab. " + res.getHabitacion().getNumero() : "Sin habitación asignada");
            long nights = res.getNoches();
            lblDetalleNoches.setText(nights + " noche" + (nights != 1 ? "s" : ""));

            // ── Líneas de coste ──
            List<LineaFactura> lineas = FacturaDAO.getLineasPorReserva(res.getId());

            // Si aún no hay factura, añadir línea calculada de la estancia
            boolean hayFactura = !lineas.isEmpty() || FacturaDAO.getPorReserva(res.getId()) != null;
            if (!hayFactura && res.getPrecioTotal() != null && nights > 0) {
                BigDecimal total = res.getPrecioTotal();
                BigDecimal pxN = total.divide(java.math.BigDecimal.valueOf(nights), 2, java.math.RoundingMode.HALF_UP);
                LineaFactura estimada = new LineaFactura();
                estimada.setDescripcion("Estancia · " + nights + " noche(s)  (estimado)");
                estimada.setCantidad(java.math.BigDecimal.valueOf(nights));
                estimada.setPrecioUnitario(pxN);
                estimada.setPrecioTotal(total);
                lineas = new java.util.ArrayList<>(lineas);
                lineas.add(estimada);
            }

            tblDetalleLineas.setItems(FXCollections.observableArrayList(lineas));

            // ── Totales ──
            java.math.BigDecimal subtotal = lineas.stream()
                    .map(l -> l.getPrecioTotal() != null ? l.getPrecioTotal() : java.math.BigDecimal.ZERO)
                    .reduce(java.math.BigDecimal.ZERO, java.math.BigDecimal::add);
            java.math.BigDecimal iva = subtotal.multiply(new java.math.BigDecimal("0.10"))
                    .setScale(2, java.math.RoundingMode.HALF_UP);
            java.math.BigDecimal totalFinal = subtotal.add(iva);

            lblDetalleSubtotal.setText(String.format(EURO_FORMAT, subtotal));
            lblDetalleIva.setText(String.format(EURO_FORMAT, iva));
            lblDetalleTotalFinal.setText(String.format(EURO_FORMAT, totalFinal));

            vboxDetalleInfo.setVisible(true);
            vboxDetalleInfo.setManaged(true);
        } catch (Exception e) { showError("Error al cargar detalle", e.getMessage()); }
    }
}

package es.simonsg.pmsuite.controller;

import es.simonsg.pmsuite.dao.MantenimientoDAO;
import es.simonsg.pmsuite.dao.HabitacionDAO;
import es.simonsg.pmsuite.model.SolicitudMantenimiento;
import es.simonsg.pmsuite.model.Habitacion;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.util.StringConverter;

import java.sql.SQLException;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class MantenimientoController {

    // ── Navigation
    @FXML private Button btnNavIncidencias;
    @FXML private Button btnNavEstado;
    @FXML private Button btnNavHistorial;

    // ── Panels
    @FXML private VBox pnlIncidencias;
    @FXML private VBox pnlEstadoHabitaciones;
    @FXML private VBox pnlHistorial;

    // ── Incidencias – tabla
    @FXML private TableView<SolicitudMantenimiento>              tblIncidencias;
    @FXML private TableColumn<SolicitudMantenimiento, String>    colIncHab;
    @FXML private TableColumn<SolicitudMantenimiento, String>    colIncTitulo;
    @FXML private TableColumn<SolicitudMantenimiento, String>    colIncPrioridad;
    @FXML private TableColumn<SolicitudMantenimiento, String>    colIncEstado;
    @FXML private TableColumn<SolicitudMantenimiento, String>    colIncFecha;

    // ── Incidencias – formulario
    @FXML private ComboBox<String>                           cmbFiltroEstado;
    @FXML private ComboBox<Habitacion>                             cmbHabitacion;
    @FXML private ComboBox<SolicitudMantenimiento.Prioridad>      cmbPrioridad;
    @FXML private ComboBox<SolicitudMantenimiento.Estado>        cmbEstadoInc;
    @FXML private TextField                                  txtTitulo;
    @FXML private TextArea                                   txtDescripcion;
    @FXML private TextField                                  txtNotasInc;

    // ── Estado habitaciones
    @FXML private TableView<Habitacion>                            tblEstadoHab;
    @FXML private TableColumn<Habitacion, String>                  colEstHabNum;
    @FXML private TableColumn<Habitacion, String>                  colEstHabTipo;
    @FXML private TableColumn<Habitacion, String>                  colEstHabPlanta;
    @FXML private TableColumn<Habitacion, String>                  colEstHabEstado;
    @FXML private ComboBox<Habitacion.Estado>                      cmbNuevoEstado;

    // ── Historial
    @FXML private TableView<SolicitudMantenimiento>              tblHistorial;
    @FXML private TableColumn<SolicitudMantenimiento, String>    colHistHab;
    @FXML private TableColumn<SolicitudMantenimiento, String>    colHistTitulo;
    @FXML private TableColumn<SolicitudMantenimiento, String>    colHistPrioridad;
    @FXML private TableColumn<SolicitudMantenimiento, String>    colHistEstado;
    @FXML private TableColumn<SolicitudMantenimiento, String>    colHistFechaInicio;
    @FXML private TableColumn<SolicitudMantenimiento, String>    colHistFechaFin;

    // ── State
    private IndexController indexController;
    private SolicitudMantenimiento seleccionada;

    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    // ── Lifecycle

    @FXML
    public void initialize() {
        inicializarTablaIncidencias();
        inicializarTablaEstado();
        inicializarTablaHistorial();
        inicializarCombos();
        cargarIncidencias();
        mostrarPanel(pnlIncidencias, btnNavIncidencias);
    }

    public void setIndexController(IndexController ic) {
        this.indexController = ic;
    }

    /**
     * Called when navigating from another module with a known DB roomId.
     */
    public void mostrarHabitacion(int roomId, String roomName) {
        mostrarPanel(pnlIncidencias, btnNavIncidencias);
        try {
            List<SolicitudMantenimiento> lista = MantenimientoDAO.getPorHabitacion(roomId);
            tblIncidencias.setItems(FXCollections.observableArrayList(lista));
            cmbHabitacion.getItems().stream()
                    .filter(r -> r.getId() == roomId)
                    .findFirst()
                    .ifPresent(r -> cmbHabitacion.setValue(r));
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * Called from PlanningController (mock data) using room number instead of DB id.
     */
    public void mostrarHabitacionPorNumero(int roomNumber, String roomName) {
        mostrarPanel(pnlIncidencias, btnNavIncidencias);
        // Find matching room by number in combo
        cmbHabitacion.getItems().stream()
                .filter(r -> {
                    try { return Integer.parseInt(r.getNumero()) == roomNumber; }
                    catch (NumberFormatException e) { return r.getNumero().equals(String.valueOf(roomNumber)); }
                })
                .findFirst()
                .ifPresentOrElse(
                        room -> {
                            cmbHabitacion.setValue(room);
                            try {
                                List<SolicitudMantenimiento> lista = MantenimientoDAO.getPorHabitacion(room.getId());
                                tblIncidencias.setItems(FXCollections.observableArrayList(lista));
                            } catch (SQLException ex) {
                                ex.printStackTrace();
                            }
                        },
                        () -> {
                            // Habitacion not in DB yet, show all active and pre-fill room field
                            cargarIncidencias();
                        }
                );
    }

    // ── Navigation

    @FXML private void volverInicio() {
        if (indexController != null) indexController.mostrarInicio();
    }

    @FXML private void navIncidencias() {
        mostrarPanel(pnlIncidencias, btnNavIncidencias);
    }

    @FXML private void navEstado() {
        mostrarPanel(pnlEstadoHabitaciones, btnNavEstado);
        cargarEstadoHabitaciones();
    }

    @FXML private void navHistorial() {
        mostrarPanel(pnlHistorial, btnNavHistorial);
        cargarHistorial();
    }

    // ── Incidencias actions

    @FXML
    public void cargarIncidencias() {
        try {
            String filtro = cmbFiltroEstado.getValue();
            List<SolicitudMantenimiento> lista;
            if ("Pendiente".equals(filtro)) {
                lista = MantenimientoDAO.obtenerActivas().stream()
                        .filter(r -> r.getEstado() == SolicitudMantenimiento.Estado.PENDIENTE).toList();
            } else if ("En curso".equals(filtro)) {
                lista = MantenimientoDAO.obtenerActivas().stream()
                        .filter(r -> r.getEstado() == SolicitudMantenimiento.Estado.EN_PROGRESO).toList();
            } else {
                lista = MantenimientoDAO.obtenerActivas();
            }
            tblIncidencias.setItems(FXCollections.observableArrayList(lista));
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void nuevaIncidencia() {
        seleccionada = null;
        limpiarFormularioInc();
    }

    @FXML
    private void guardarIncidencia() {
        if (txtTitulo.getText().isBlank()) {
            mostrarError("El título es obligatorio.");
            return;
        }
        try {
            SolicitudMantenimiento mr = seleccionada != null ? seleccionada : new SolicitudMantenimiento();
            mr.setHabitacion(cmbHabitacion.getValue());
            mr.setTitulo(txtTitulo.getText().trim());
            mr.setDescripcion(txtDescripcion.getText().trim());
            mr.setPrioridad(cmbPrioridad.getValue() != null ? cmbPrioridad.getValue() : SolicitudMantenimiento.Prioridad.MEDIA);
            mr.setEstado(cmbEstadoInc.getValue() != null ? cmbEstadoInc.getValue() : SolicitudMantenimiento.Estado.PENDIENTE);
            mr.setNotas(txtNotasInc.getText().trim());

            if (seleccionada != null) {
                MantenimientoDAO.actualizar(mr);
            } else {
                MantenimientoDAO.crear(mr);
            }
            limpiarFormularioInc();
            cargarIncidencias();
        } catch (SQLException e) {
            e.printStackTrace();
            mostrarError("Error al guardar la incidencia.");
        }
    }

    @FXML
    private void completarIncidencia() {
        SolicitudMantenimiento sel = tblIncidencias.getSelectionModel().getSelectedItem();
        if (sel == null) { mostrarError("Selecciona una incidencia de la tabla."); return; }
        try {
            MantenimientoDAO.actualizarEstado(sel.getId(), SolicitudMantenimiento.Estado.COMPLETADA);
            limpiarFormularioInc();
            cargarIncidencias();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void cancelarIncidencia() {
        SolicitudMantenimiento sel = tblIncidencias.getSelectionModel().getSelectedItem();
        if (sel == null) { mostrarError("Selecciona una incidencia de la tabla."); return; }
        try {
            MantenimientoDAO.actualizarEstado(sel.getId(), SolicitudMantenimiento.Estado.CANCELADA);
            limpiarFormularioInc();
            cargarIncidencias();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @FXML
    public void limpiarFormularioInc() {
        seleccionada = null;
        cmbHabitacion.setValue(null);
        cmbPrioridad.setValue(SolicitudMantenimiento.Prioridad.MEDIA);
        cmbEstadoInc.setValue(SolicitudMantenimiento.Estado.PENDIENTE);
        txtTitulo.clear();
        txtDescripcion.clear();
        txtNotasInc.clear();
        tblIncidencias.getSelectionModel().clearSelection();
    }

    // ── Estado habitaciones actions

    private void cargarEstadoHabitaciones() {
        try {
            tblEstadoHab.setItems(FXCollections.observableArrayList(HabitacionDAO.obtenerTodas()));
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void aplicarEstado() {
        Habitacion sel = tblEstadoHab.getSelectionModel().getSelectedItem();
        if (sel == null)                    { mostrarError("Selecciona una habitación."); return; }
        if (cmbNuevoEstado.getValue() == null) { mostrarError("Selecciona el nuevo estado."); return; }
        try {
            HabitacionDAO.actualizarEstado(sel.getId(), cmbNuevoEstado.getValue());
            cmbNuevoEstado.setValue(null);
            cargarEstadoHabitaciones();
        } catch (SQLException e) {
            e.printStackTrace();
            mostrarError("Error al actualizar el estado.");
        }
    }

    // ── Historial

    @FXML
    public void cargarHistorial() {
        try {
            tblHistorial.setItems(FXCollections.observableArrayList(MantenimientoDAO.getHistorial()));
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // ── Private helpers

    private void inicializarTablaIncidencias() {
        colIncHab.setCellValueFactory(c -> {
            Habitacion r = c.getValue().getHabitacion();
            return new SimpleStringProperty(r != null ? "Hab. " + r.getNumero() : "General");
        });
        colIncTitulo.setCellValueFactory(c ->
                new SimpleStringProperty(c.getValue().getTitulo()));
        colIncPrioridad.setCellValueFactory(c ->
                new SimpleStringProperty(c.getValue().getPrioridad().toSpanish()));
        colIncEstado.setCellValueFactory(c ->
                new SimpleStringProperty(c.getValue().getEstado().toSpanish()));
        colIncFecha.setCellValueFactory(c -> {
            var dt = c.getValue().getFechaReporte();
            return new SimpleStringProperty(dt != null ? dt.format(FMT) : "");
        });

        // Color rows by priority
        tblIncidencias.setRowFactory(tv -> new TableRow<>() {
            @Override
            protected void updateItem(SolicitudMantenimiento item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setStyle("");
                } else {
                    setStyle(switch (item.getPrioridad()) {
                        case URGENTE -> "-fx-background-color: #FDEDEC;";
                        case ALTA   -> "-fx-background-color: #FEF9E7;";
                        default     -> "";
                    });
                }
            }
        });

        tblIncidencias.getSelectionModel().selectedItemProperty().addListener((obs, old, sel) -> {
            if (sel != null) rellenarFormularioInc(sel);
        });
    }

    private void inicializarTablaEstado() {
        colEstHabNum.setCellValueFactory(c ->
                new SimpleStringProperty(c.getValue().getNumero()));
        colEstHabTipo.setCellValueFactory(c -> {
            var rt = c.getValue().getTipoHabitacion();
            return new SimpleStringProperty(rt != null ? rt.getNombre() : "");
        });
        colEstHabPlanta.setCellValueFactory(c ->
                new SimpleStringProperty("Planta " + c.getValue().getPlanta()));
        colEstHabEstado.setCellValueFactory(c ->
                new SimpleStringProperty(c.getValue().getEstado().toSpanish()));
    }

    private void inicializarTablaHistorial() {
        colHistHab.setCellValueFactory(c -> {
            Habitacion r = c.getValue().getHabitacion();
            return new SimpleStringProperty(r != null ? "Hab. " + r.getNumero() : "General");
        });
        colHistTitulo.setCellValueFactory(c ->
                new SimpleStringProperty(c.getValue().getTitulo()));
        colHistPrioridad.setCellValueFactory(c ->
                new SimpleStringProperty(c.getValue().getPrioridad().toSpanish()));
        colHistEstado.setCellValueFactory(c ->
                new SimpleStringProperty(c.getValue().getEstado().toSpanish()));
        colHistFechaInicio.setCellValueFactory(c -> {
            var dt = c.getValue().getFechaReporte();
            return new SimpleStringProperty(dt != null ? dt.format(FMT) : "");
        });
        colHistFechaFin.setCellValueFactory(c -> {
            var dt = c.getValue().getFechaCompletado();
            return new SimpleStringProperty(dt != null ? dt.format(FMT) : "");
        });
    }

    private void inicializarCombos() {
        cmbFiltroEstado.setItems(FXCollections.observableArrayList("Todas", "Pendiente", "En curso"));
        cmbFiltroEstado.setValue("Todas");

        cmbPrioridad.setItems(FXCollections.observableArrayList(SolicitudMantenimiento.Prioridad.values()));
        cmbPrioridad.setValue(SolicitudMantenimiento.Prioridad.MEDIA);
        cmbPrioridad.setConverter(new StringConverter<>() {
            @Override public String toString(SolicitudMantenimiento.Prioridad p) { return p != null ? p.toSpanish() : ""; }
            @Override public SolicitudMantenimiento.Prioridad fromString(String s) { return null; }
        });

        cmbEstadoInc.setItems(FXCollections.observableArrayList(SolicitudMantenimiento.Estado.values()));
        cmbEstadoInc.setValue(SolicitudMantenimiento.Estado.PENDIENTE);
        cmbEstadoInc.setConverter(new StringConverter<>() {
            @Override public String toString(SolicitudMantenimiento.Estado s) { return s != null ? s.toSpanish() : ""; }
            @Override public SolicitudMantenimiento.Estado fromString(String s) { return null; }
        });

        try {
            cmbHabitacion.setItems(FXCollections.observableArrayList(HabitacionDAO.obtenerTodas()));
        } catch (SQLException e) {
            e.printStackTrace();
        }

        cmbNuevoEstado.setItems(FXCollections.observableArrayList(Habitacion.Estado.values()));
        cmbNuevoEstado.setConverter(new StringConverter<>() {
            @Override public String toString(Habitacion.Estado s) { return s != null ? s.toSpanish() : ""; }
            @Override public Habitacion.Estado fromString(String s) { return null; }
        });
    }

    private void rellenarFormularioInc(SolicitudMantenimiento mr) {
        seleccionada = mr;
        if (mr.getHabitacion() != null) {
            cmbHabitacion.getItems().stream()
                    .filter(r -> r.getId() == mr.getHabitacion().getId())
                    .findFirst()
                    .ifPresent(r -> cmbHabitacion.setValue(r));
        } else {
            cmbHabitacion.setValue(null);
        }
        txtTitulo.setText(mr.getTitulo() != null ? mr.getTitulo() : "");
        txtDescripcion.setText(mr.getDescripcion() != null ? mr.getDescripcion() : "");
        cmbPrioridad.setValue(mr.getPrioridad());
        cmbEstadoInc.setValue(mr.getEstado());
        txtNotasInc.setText(mr.getNotas() != null ? mr.getNotas() : "");
    }

    private void mostrarPanel(VBox panel, Button btn) {
        pnlIncidencias.setVisible(false);        pnlIncidencias.setManaged(false);
        pnlEstadoHabitaciones.setVisible(false); pnlEstadoHabitaciones.setManaged(false);
        pnlHistorial.setVisible(false);          pnlHistorial.setManaged(false);

        panel.setVisible(true);
        panel.setManaged(true);

        btnNavIncidencias.getStyleClass().remove("nav-btn-active");
        btnNavEstado.getStyleClass().remove("nav-btn-active");
        btnNavHistorial.getStyleClass().remove("nav-btn-active");
        if (!btn.getStyleClass().contains("nav-btn-active")) {
            btn.getStyleClass().add("nav-btn-active");
        }
    }

    private void mostrarError(String msg) {
        new Alert(Alert.AlertType.ERROR, msg, ButtonType.OK).showAndWait();
    }
}

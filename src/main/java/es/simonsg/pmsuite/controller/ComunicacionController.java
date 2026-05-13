package es.simonsg.pmsuite.controller;

import es.simonsg.pmsuite.dao.ComunicacionDAO;
import es.simonsg.pmsuite.dao.PlantillaEmailDAO;
import es.simonsg.pmsuite.dao.HuespedDAO;
import es.simonsg.pmsuite.dao.HotelDAO;
import es.simonsg.pmsuite.dao.ReservaDAO;
import es.simonsg.pmsuite.model.Comunicacion;
import es.simonsg.pmsuite.model.Comunicacion.Canal;
import es.simonsg.pmsuite.model.Comunicacion.Sentido;
import es.simonsg.pmsuite.model.PlantillaEmail;
import es.simonsg.pmsuite.model.Huesped;
import es.simonsg.pmsuite.model.Factura;
import es.simonsg.pmsuite.model.Reserva;
import es.simonsg.pmsuite.util.ServicioEmail;
import es.simonsg.pmsuite.util.ServicioEmail.ConfigSmtp;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.io.File;
import java.sql.SQLException;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;

public class ComunicacionController {

    private IndexController indexController;

    private static final DateTimeFormatter DT_FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    // ── Navigation ────────────────────────────────────────────────────────────
    @FXML private Button btnNavNuevo;
    @FXML private Button btnNavConfirmaciones;
    @FXML private Button btnNavInternos;
    @FXML private Button btnNavHistorial;
    @FXML private Button btnNavPlantillas;
    @FXML private Button btnNavConfig;

    // ── Panels ────────────────────────────────────────────────────────────────
    @FXML private VBox pnlNuevo;
    @FXML private VBox pnlConfirmaciones;
    @FXML private VBox pnlInternos;
    @FXML private VBox pnlHistorial;
    @FXML private VBox pnlPlantillas;
    @FXML private VBox pnlConfig;

    // ── Panel 1: Nuevo Mensaje ────────────────────────────────────────────────
    @FXML private ComboBox<Huesped>         cmbDestinatario;
    @FXML private ComboBox<Reserva>   cmbReserva;
    @FXML private ComboBox<Comunicacion.Canal>       cmbCanal;
    @FXML private ComboBox<PlantillaEmail> cmbPlantilla;
    @FXML private TextField               txtAsunto;
    @FXML private TextArea                txtCuerpo;
    @FXML private HBox                    hboxAdjunto;
    @FXML private Label                   lblAdjunto;
    @FXML private Label                   lblNuevoStatus;

    private File adjuntoFile = null;

    // ── Panel 2: Confirmaciones ───────────────────────────────────────────────
    @FXML private TableView<Reserva>           tblConfirmaciones;
    @FXML private TableColumn<Reserva, String> colConfNum;
    @FXML private TableColumn<Reserva, String> colConfHuesped;
    @FXML private TableColumn<Reserva, String> colConfEmail;
    @FXML private TableColumn<Reserva, String> colConfEntrada;
    @FXML private TableColumn<Reserva, String> colConfEstado;
    @FXML private Label                            lblConfStatus;

    // ── Panel 3: Mensajes Internos ────────────────────────────────────────────
    @FXML private TextArea  txtInternoMensaje;
    @FXML private TextField txtInternoAsunto;
    @FXML private Label     lblInternoStatus;
    @FXML private TableView<Comunicacion>           tblInternos;
    @FXML private TableColumn<Comunicacion, String> colIntFecha;
    @FXML private TableColumn<Comunicacion, String> colIntAsunto;
    @FXML private TableColumn<Comunicacion, String> colIntCuerpo;

    // ── Panel 4: Historial ────────────────────────────────────────────────────
    @FXML private ComboBox<String>                   cmbHistFiltro;
    @FXML private TableView<Comunicacion>           tblHistorial;
    @FXML private TableColumn<Comunicacion, String> colHistFecha;
    @FXML private TableColumn<Comunicacion, String> colHistCanal;
    @FXML private TableColumn<Comunicacion, String> colHistDireccion;
    @FXML private TableColumn<Comunicacion, String> colHistHuesped;
    @FXML private TableColumn<Comunicacion, String> colHistAsunto;
    @FXML private TextArea                           txtHistDetalle;

    // ── Panel 5: Plantillas ───────────────────────────────────────────────────
    @FXML private TableView<PlantillaEmail>           tblPlantillas;
    @FXML private TableColumn<PlantillaEmail, String> colPltNombre;
    @FXML private TableColumn<PlantillaEmail, String> colPltTipo;
    @FXML private TableColumn<PlantillaEmail, String> colPltAsunto;
    @FXML private TableColumn<PlantillaEmail, String> colPltActiva;
    @FXML private TextField                          txtPltNombre;
    @FXML private ComboBox<PlantillaEmail.Tipo>       cmbPltTipo;
    @FXML private TextField                          txtPltAsunto;
    @FXML private TextArea                           txtPltCuerpo;
    @FXML private CheckBox                           chkPltActiva;
    @FXML private Label                              lblPltStatus;

    // ── Panel 6: Config SMTP ──────────────────────────────────────────────────
    @FXML private TextField txtSmtpHost;
    @FXML private TextField txtSmtpPort;
    @FXML private TextField txtSmtpUser;
    @FXML private PasswordField txtSmtpPassword;
    @FXML private TextField txtSmtpFrom;
    @FXML private TextField txtSmtpFromName;
    @FXML private CheckBox  chkSmtpTls;
    @FXML private TextField txtSmtpTestTo;
    @FXML private Label     lblSmtpStatus;

    private List<VBox>   allPanels;
    private List<Button> allNavBtns;

    public void setIndexController(IndexController idx) {
        this.indexController = idx;
    }

    @FXML
    public void initialize() {
        allPanels  = List.of(pnlNuevo, pnlConfirmaciones, pnlInternos, pnlHistorial, pnlPlantillas, pnlConfig);
        allNavBtns = List.of(btnNavNuevo, btnNavConfirmaciones, btnNavInternos, btnNavHistorial, btnNavPlantillas, btnNavConfig);

        configurarTablas();
        configurarCombos();
        cargarNuevoMensaje();
        mostrarPanel(pnlNuevo, btnNavNuevo);
    }

    // ── Navigation ────────────────────────────────────────────────────────────

    @FXML private void irNuevo()          { mostrarPanel(pnlNuevo, btnNavNuevo); cargarNuevoMensaje(); }
    @FXML private void irConfirmaciones() { mostrarPanel(pnlConfirmaciones, btnNavConfirmaciones); cargarConfirmaciones(); }
    @FXML private void irInternos()       { mostrarPanel(pnlInternos, btnNavInternos); cargarInternos(); }
    @FXML private void irHistorial()      { mostrarPanel(pnlHistorial, btnNavHistorial); cargarHistorial(); }
    @FXML private void irPlantillas()     { mostrarPanel(pnlPlantillas, btnNavPlantillas); cargarPlantillas(); }
    @FXML private void irConfig()         { mostrarPanel(pnlConfig, btnNavConfig); cargarConfigSmtp(); }
    @FXML private void volverInicio()     { if (indexController != null) indexController.mostrarInicio(); }

    private void mostrarPanel(VBox panel, Button btn) {
        for (int i = 0; i < allPanels.size(); i++) {
            boolean active = allPanels.get(i) == panel;
            allPanels.get(i).setVisible(active);
            allPanels.get(i).setManaged(active);
            allNavBtns.get(i).getStyleClass().remove("com-nav-btn-active");
            if (active) allNavBtns.get(i).getStyleClass().add("com-nav-btn-active");
        }
    }

    // ── Panel 1: Nuevo Mensaje ────────────────────────────────────────────────

    private void cargarNuevoMensaje() {
        try {
            List<Huesped> huespedes = HuespedDAO.buscar("");
            cmbDestinatario.setItems(FXCollections.observableArrayList(huespedes));

            List<Reserva> reservas = ReservaDAO.obtenerActivas();
            cmbReserva.setItems(FXCollections.observableArrayList(reservas));
            cmbReserva.getItems().add(0, null);

            List<PlantillaEmail> plantillas = PlantillaEmailDAO.obtenerActivas();
            cmbPlantilla.setItems(FXCollections.observableArrayList(plantillas));
            cmbPlantilla.getItems().add(0, null);
        } catch (SQLException e) {
            lblNuevoStatus.setText("Error cargando datos: " + e.getMessage());
        }

        cmbPlantilla.setOnAction(ev -> aplicarPlantilla());
    }

    private void aplicarPlantilla() {
        PlantillaEmail t = cmbPlantilla.getValue();
        if (t == null) return;
        Huesped g = cmbDestinatario.getValue();
        Reserva r = cmbReserva.getValue();

        Map<String, String> vars = buildVars(g, r);
        txtAsunto.setText(t.renderAsunto(vars));
        txtCuerpo.setText(t.renderCuerpo(vars));
    }

    @FXML
    private void enviarMensaje() {
        Comunicacion.Canal canal = cmbCanal.getValue();
        String asunto = txtAsunto.getText().trim();
        String cuerpo = txtCuerpo.getText().trim();

        if (canal == null || asunto.isEmpty() || cuerpo.isEmpty()) {
            lblNuevoStatus.setText("Complete canal, asunto y cuerpo.");
            return;
        }

        Huesped g = cmbDestinatario.getValue();
        Reserva r = cmbReserva.getValue();

        Comunicacion c = new Comunicacion();
        c.setHuesped(g);
        c.setReserva(r);
        c.setAsunto(asunto);
        c.setCuerpo(cuerpo);
        c.setCanal(canal);
        c.setSentido(Comunicacion.Sentido.SALIENTE);

        if (canal == Comunicacion.Canal.EMAIL) {
            if (g == null || g.getEmail() == null || g.getEmail().isBlank()) {
                lblNuevoStatus.setText("El huésped no tiene email registrado.");
                return;
            }
            lblNuevoStatus.setText("Enviando email...");
            String toEmail = g.getEmail();
            File adjunto = adjuntoFile;
            Executors.newSingleThreadExecutor().execute(() -> {
                try {
                    if (adjunto != null && adjunto.exists()) {
                        ServicioEmail.sendWithAttachmentStoredConfig(toEmail, asunto, cuerpo, adjunto);
                    } else {
                        ServicioEmail.sendWithStoredConfig(toEmail, asunto, cuerpo);
                    }
                    ComunicacionDAO.crear(c);
                    Platform.runLater(() -> {
                        lblNuevoStatus.setText("Email enviado correctamente a " + toEmail);
                        limpiarFormulario();
                    });
                } catch (Exception ex) {
                    Platform.runLater(() -> lblNuevoStatus.setText("Error: " + ex.getMessage()));
                }
            });
        } else {
            try {
                ComunicacionDAO.crear(c);
                lblNuevoStatus.setText("Mensaje registrado correctamente.");
                limpiarFormulario();
            } catch (SQLException ex) {
                lblNuevoStatus.setText("Error al guardar: " + ex.getMessage());
            }
        }
    }

    @FXML
    private void quitarAdjunto() {
        adjuntoFile = null;
        hboxAdjunto.setVisible(false);
        hboxAdjunto.setManaged(false);
        lblAdjunto.setText("");
    }

    private void limpiarFormulario() {
        cmbDestinatario.setValue(null);
        cmbReserva.setValue(null);
        cmbPlantilla.setValue(null);
        txtAsunto.clear();
        txtCuerpo.clear();
        quitarAdjunto();
    }

    /**
     * Called from IndexController when opening Comunicación from Administración.
     * Pre-loads the Nuevo Mensaje panel with the INVOICE template and a PDF attachment.
     */
    public void precargarParaFactura(Factura invoice, File pdfAdjunto) {
        mostrarPanel(pnlNuevo, btnNavNuevo);
        cargarNuevoMensaje();

        if (invoice.getHuesped() != null) {
            int gId = invoice.getHuesped().getId();
            cmbDestinatario.getItems().stream()
                    .filter(g -> g != null && g.getId() == gId)
                    .findFirst()
                    .ifPresent(cmbDestinatario::setValue);
        }

        cmbCanal.setValue(Comunicacion.Canal.EMAIL);

        try {
            PlantillaEmail template = buscarPlantillaTipo(PlantillaEmail.Tipo.FACTURA);
            Map<String, String> vars = buildVarsFactura(invoice);
            if (template != null) {
                cmbPlantilla.setValue(template);
                txtAsunto.setText(template.renderAsunto(vars));
                txtCuerpo.setText(template.renderCuerpo(vars));
            } else {
                txtAsunto.setText("Factura " + invoice.getNumeroFactura());
                txtCuerpo.setText(buildCuerpoFactura(invoice));
            }
        } catch (SQLException e) {
            lblNuevoStatus.setText("Error cargando plantilla: " + e.getMessage());
        }

        if (pdfAdjunto != null && pdfAdjunto.exists()) {
            adjuntoFile = pdfAdjunto;
            lblAdjunto.setText(pdfAdjunto.getName());
            hboxAdjunto.setVisible(true);
            hboxAdjunto.setManaged(true);
        }
    }

    // ── Panel 2: Confirmaciones ───────────────────────────────────────────────

    private void cargarConfirmaciones() {
        try {
            List<Reserva> reservas = ReservaDAO.obtenerActivas();
            tblConfirmaciones.setItems(FXCollections.observableArrayList(reservas));
            lblConfStatus.setText("");
        } catch (SQLException e) {
            lblConfStatus.setText("Error: " + e.getMessage());
        }
    }

    @FXML
    private void enviarConfirmacionSeleccionada() {
        Reserva r = tblConfirmaciones.getSelectionModel().getSelectedItem();
        if (r == null) { lblConfStatus.setText("Seleccione una reserva."); return; }
        if (r.getHuesped() == null || r.getHuesped().getEmail() == null || r.getHuesped().getEmail().isBlank()) {
            lblConfStatus.setText("El huésped no tiene email registrado.");
            return;
        }

        try {
            PlantillaEmail template = buscarPlantillaTipo(PlantillaEmail.Tipo.CONFIRMACION);
            Map<String, String> vars = buildVars(r.getHuesped(), r);
            String subject = template != null ? template.renderAsunto(vars) : "Confirmación reserva " + r.getNumeroReserva();
            String body    = template != null ? template.renderCuerpo(vars)    : buildConfirmacionBody(r);

            Comunicacion c = new Comunicacion();
            c.setHuesped(r.getHuesped());
            c.setReserva(r);
            c.setAsunto(subject);
            c.setCuerpo(body);
            c.setCanal(Comunicacion.Canal.EMAIL);
            c.setSentido(Comunicacion.Sentido.SALIENTE);

            String toEmail = r.getHuesped().getEmail();
            lblConfStatus.setText("Enviando...");
            Executors.newSingleThreadExecutor().execute(() -> {
                try {
                    ServicioEmail.sendWithStoredConfig(toEmail, subject, body);
                    ComunicacionDAO.crear(c);
                    Platform.runLater(() -> lblConfStatus.setText("Confirmación enviada a " + toEmail));
                } catch (Exception ex) {
                    Platform.runLater(() -> lblConfStatus.setText("Error: " + ex.getMessage()));
                }
            });
        } catch (Exception e) {
            lblConfStatus.setText("Error: " + e.getMessage());
        }
    }

    // ── Panel 3: Mensajes Internos ────────────────────────────────────────────

    private void cargarInternos() {
        try {
            List<Comunicacion> internos = ComunicacionDAO.getInternal();
            tblInternos.setItems(FXCollections.observableArrayList(internos));
            lblInternoStatus.setText("");
        } catch (SQLException e) {
            lblInternoStatus.setText("Error: " + e.getMessage());
        }
    }

    @FXML
    private void enviarInterno() {
        String asunto = txtInternoAsunto.getText().trim();
        String msg    = txtInternoMensaje.getText().trim();
        if (asunto.isEmpty() || msg.isEmpty()) {
            lblInternoStatus.setText("Complete asunto y mensaje.");
            return;
        }

        Comunicacion c = new Comunicacion();
        c.setAsunto(asunto);
        c.setCuerpo(msg);
        c.setCanal(Comunicacion.Canal.INTERNO);
        c.setSentido(Comunicacion.Sentido.SALIENTE);
        try {
            ComunicacionDAO.crear(c);
            lblInternoStatus.setText("Mensaje interno registrado.");
            txtInternoAsunto.clear();
            txtInternoMensaje.clear();
            cargarInternos();
        } catch (SQLException e) {
            lblInternoStatus.setText("Error: " + e.getMessage());
        }
    }

    // ── Panel 4: Historial ────────────────────────────────────────────────────

    private void cargarHistorial() {
        String filtro = cmbHistFiltro.getValue();
        try {
            List<Comunicacion> lista;
            if ("Enviados".equals(filtro)) {
                lista = ComunicacionDAO.getByDirection(Comunicacion.Sentido.SALIENTE);
            } else if ("Recibidos".equals(filtro)) {
                lista = ComunicacionDAO.getByDirection(Comunicacion.Sentido.ENTRANTE);
            } else if ("Email".equals(filtro)) {
                lista = ComunicacionDAO.getByChannel(Comunicacion.Canal.EMAIL);
            } else if ("Interno".equals(filtro)) {
                lista = ComunicacionDAO.getByChannel(Comunicacion.Canal.INTERNO);
            } else {
                lista = ComunicacionDAO.obtenerTodas();
            }
            tblHistorial.setItems(FXCollections.observableArrayList(lista));
            txtHistDetalle.clear();
        } catch (SQLException e) {
            txtHistDetalle.setText("Error: " + e.getMessage());
        }
    }

    // ── Panel 5: Plantillas ───────────────────────────────────────────────────

    private void cargarPlantillas() {
        try {
            tblPlantillas.setItems(FXCollections.observableArrayList(PlantillaEmailDAO.obtenerTodas()));
            lblPltStatus.setText("");
        } catch (SQLException e) {
            lblPltStatus.setText("Error: " + e.getMessage());
        }
    }

    @FXML
    private void guardarPlantilla() {
        String nombre = txtPltNombre.getText().trim();
        PlantillaEmail.Tipo tipo = cmbPltTipo.getValue();
        String asunto = txtPltAsunto.getText().trim();
        String cuerpo = txtPltCuerpo.getText().trim();

        if (nombre.isEmpty() || tipo == null || asunto.isEmpty() || cuerpo.isEmpty()) {
            lblPltStatus.setText("Complete todos los campos de la plantilla.");
            return;
        }

        PlantillaEmail selected = tblPlantillas.getSelectionModel().getSelectedItem();
        try {
            if (selected != null) {
                selected.setNombre(nombre);
                selected.setTipo(tipo);
                selected.setAsunto(asunto);
                selected.setCuerpo(cuerpo);
                selected.setActivo(chkPltActiva.isSelected());
                PlantillaEmailDAO.actualizar(selected);
                lblPltStatus.setText("Plantilla actualizada.");
            } else {
                PlantillaEmail t = new PlantillaEmail();
                t.setNombre(nombre);
                t.setTipo(tipo);
                t.setAsunto(asunto);
                t.setCuerpo(cuerpo);
                t.setActivo(chkPltActiva.isSelected());
                PlantillaEmailDAO.crear(t);
                lblPltStatus.setText("Plantilla creada.");
            }
            cargarPlantillas();
            limpiarFormularioPlantilla();
        } catch (SQLException e) {
            lblPltStatus.setText("Error: " + e.getMessage());
        }
    }

    @FXML
    private void eliminarPlantilla() {
        PlantillaEmail selected = tblPlantillas.getSelectionModel().getSelectedItem();
        if (selected == null) { lblPltStatus.setText("Seleccione una plantilla."); return; }
        try {
            PlantillaEmailDAO.eliminar(selected.getId());
            lblPltStatus.setText("Plantilla eliminada.");
            cargarPlantillas();
            limpiarFormularioPlantilla();
        } catch (SQLException e) {
            lblPltStatus.setText("Error: " + e.getMessage());
        }
    }

    @FXML
    private void nuevaPlantilla() {
        limpiarFormularioPlantilla();
        lblPltStatus.setText("Rellena el formulario y pulsa Guardar para crear una nueva plantilla.");
    }

    private void limpiarFormularioPlantilla() {
        txtPltNombre.clear();
        cmbPltTipo.setValue(null);
        txtPltAsunto.clear();
        txtPltCuerpo.clear();
        chkPltActiva.setSelected(true);
        tblPlantillas.getSelectionModel().clearSelection();
    }

    // ── Panel 6: Config SMTP ──────────────────────────────────────────────────

    private void cargarConfigSmtp() {
        ConfigSmtp cfg = ServicioEmail.loadConfig();
        txtSmtpHost.setText(cfg.host());
        txtSmtpPort.setText(String.valueOf(cfg.port()));
        txtSmtpUser.setText(cfg.user());
        txtSmtpPassword.setText(cfg.password());
        txtSmtpFrom.setText(cfg.from());
        txtSmtpFromName.setText(cfg.fromName());
        chkSmtpTls.setSelected(cfg.tls());
        lblSmtpStatus.setText("");
    }

    @FXML
    private void guardarConfigSmtp() {
        try {
            ConfigSmtp cfg = new ConfigSmtp(
                    txtSmtpHost.getText().trim(),
                    parsePort(txtSmtpPort.getText()),
                    txtSmtpUser.getText().trim(),
                    txtSmtpPassword.getText(),
                    txtSmtpFrom.getText().trim(),
                    txtSmtpFromName.getText().trim(),
                    chkSmtpTls.isSelected()
            );
            ServicioEmail.saveConfig(cfg);
            lblSmtpStatus.setText("Configuración guardada correctamente.");
        } catch (Exception e) {
            lblSmtpStatus.setText("Error: " + e.getMessage());
        }
    }

    @FXML
    private void enviarEmailPrueba() {
        String to = txtSmtpTestTo.getText().trim();
        if (to.isEmpty()) { lblSmtpStatus.setText("Introduzca un email de prueba."); return; }
        ConfigSmtp cfg = new ConfigSmtp(
                txtSmtpHost.getText().trim(),
                parsePort(txtSmtpPort.getText()),
                txtSmtpUser.getText().trim(),
                txtSmtpPassword.getText(),
                txtSmtpFrom.getText().trim(),
                txtSmtpFromName.getText().trim(),
                chkSmtpTls.isSelected()
        );
        lblSmtpStatus.setText("Enviando email de prueba...");
        Executors.newSingleThreadExecutor().execute(() -> {
            try {
                ServicioEmail.send(cfg, to, "Test PMSuite", "Este es un email de prueba enviado desde PMSuite.");
                Platform.runLater(() -> lblSmtpStatus.setText("Email de prueba enviado a " + to));
            } catch (Exception ex) {
                Platform.runLater(() -> lblSmtpStatus.setText("Error: " + ex.getMessage()));
            }
        });
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private void configurarTablas() {
        // Confirmaciones
        colConfNum.setCellValueFactory(c -> new SimpleStringProperty(
                c.getValue().getNumeroReserva()));
        colConfHuesped.setCellValueFactory(c -> {
            Huesped g = c.getValue().getHuesped();
            return new SimpleStringProperty(g != null ? g.getNombre() + " " + g.getApellidos() : "");
        });
        colConfEmail.setCellValueFactory(c -> {
            Huesped g = c.getValue().getHuesped();
            return new SimpleStringProperty(g != null ? g.getEmail() : "");
        });
        colConfEntrada.setCellValueFactory(c -> new SimpleStringProperty(
                c.getValue().getFechaEntrada() != null ? c.getValue().getFechaEntrada().toString() : ""));
        colConfEstado.setCellValueFactory(c -> new SimpleStringProperty(
                c.getValue().getEstado() != null ? c.getValue().getEstado().name() : ""));

        // Internos
        colIntFecha.setCellValueFactory(c -> new SimpleStringProperty(
                c.getValue().getEnviadoEn() != null ? c.getValue().getEnviadoEn().format(DT_FMT) : ""));
        colIntAsunto.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getAsunto()));
        colIntCuerpo.setCellValueFactory(c -> {
            String body = c.getValue().getCuerpo();
            return new SimpleStringProperty(body != null && body.length() > 60 ? body.substring(0, 60) + "..." : body);
        });

        // Historial
        colHistFecha.setCellValueFactory(c -> new SimpleStringProperty(
                c.getValue().getEnviadoEn() != null ? c.getValue().getEnviadoEn().format(DT_FMT) : ""));
        colHistCanal.setCellValueFactory(c -> new SimpleStringProperty(
                c.getValue().getCanal() != null ? c.getValue().getCanal().toSpanish() : ""));
        colHistDireccion.setCellValueFactory(c -> new SimpleStringProperty(
                c.getValue().getSentido() != null ? c.getValue().getSentido().toSpanish() : ""));
        colHistHuesped.setCellValueFactory(c -> {
            Huesped g = c.getValue().getHuesped();
            return new SimpleStringProperty(g != null ? g.getNombre() + " " + g.getApellidos() : "(interno)");
        });
        colHistAsunto.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getAsunto()));

        tblHistorial.getSelectionModel().selectedItemProperty().addListener((obs, old, sel) -> {
            if (sel != null) {
                txtHistDetalle.setText(
                        "De: " + (sel.getSentido() == Comunicacion.Sentido.SALIENTE ? "Nosotros" : "Huésped") + "\n" +
                                "Canal: " + (sel.getCanal() != null ? sel.getCanal().toSpanish() : "") + "\n" +
                                "Asunto: " + sel.getAsunto() + "\n\n" + sel.getCuerpo()
                );
                if (!sel.isLeido()) {
                    try { ComunicacionDAO.marcarLeida(sel.getId()); }
                    catch (SQLException ignored) {}
                }
            }
        });

        // Plantillas
        colPltNombre.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getNombre()));
        colPltTipo.setCellValueFactory(c -> new SimpleStringProperty(
                c.getValue().getTipo() != null ? c.getValue().getTipo().toSpanish() : ""));
        colPltAsunto.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getAsunto()));
        colPltActiva.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().isActivo() ? "Sí" : "No"));

        tblPlantillas.getSelectionModel().selectedItemProperty().addListener((obs, old, sel) -> {
            if (sel != null) {
                txtPltNombre.setText(sel.getNombre());
                cmbPltTipo.setValue(sel.getTipo());
                txtPltAsunto.setText(sel.getAsunto());
                txtPltCuerpo.setText(sel.getCuerpo());
                chkPltActiva.setSelected(sel.isActivo());
            }
        });
    }

    private void configurarCombos() {
        cmbCanal.setItems(FXCollections.observableArrayList(Comunicacion.Canal.values()));
        cmbCanal.setValue(Comunicacion.Canal.EMAIL);

        cmbHistFiltro.setItems(FXCollections.observableArrayList(
                "Todos", "Enviados", "Recibidos", "Email", "Interno"));
        cmbHistFiltro.setValue("Todos");
        cmbHistFiltro.setOnAction(e -> cargarHistorial());

        cmbPltTipo.setItems(FXCollections.observableArrayList(PlantillaEmail.Tipo.values()));

        cmbDestinatario.setConverter(new javafx.util.StringConverter<>() {
            @Override public String toString(Huesped g) {
                return g == null ? "" : g.getNombre() + " " + g.getApellidos();
            }
            @Override public Huesped fromString(String s) { return null; }
        });

        cmbReserva.setConverter(new javafx.util.StringConverter<>() {
            @Override public String toString(Reserva r) {
                return r == null ? "(ninguna)" : r.getNumeroReserva();
            }
            @Override public Reserva fromString(String s) { return null; }
        });

        cmbPlantilla.setConverter(new javafx.util.StringConverter<>() {
            @Override public String toString(PlantillaEmail t) {
                return t == null ? "(sin plantilla)" : t.getNombre();
            }
            @Override public PlantillaEmail fromString(String s) { return null; }
        });
    }

    private Map<String, String> buildVars(Huesped g, Reserva r) {
        Map<String, String> vars = new java.util.HashMap<>();
        try {
            es.simonsg.pmsuite.model.Hotel hotel = HotelDAO.get();
            vars.put("hotel_name", hotel != null && hotel.getNombre() != null ? hotel.getNombre() : "");
        } catch (java.sql.SQLException ignored) {
            vars.put("hotel_name", "");
        }
        if (g != null) {
            vars.put("guest_name",  g.getNombre() + " " + g.getApellidos());
            vars.put("guest_email", g.getEmail() != null ? g.getEmail() : "");
        }
        if (r != null) {
            vars.put("reservation_number", r.getNumeroReserva());
            vars.put("check_in",   r.getFechaEntrada() != null ? r.getFechaEntrada().toString() : "");
            vars.put("check_out",  r.getFechaSalida() != null ? r.getFechaSalida().toString() : "");
            vars.put("room_number", r.getHabitacion() != null ? r.getHabitacion().getNumero() : "");
            vars.put("total_price", r.getPrecioTotal() != null ? r.getPrecioTotal().toPlainString() : "");
        }
        return vars;
    }

    private String buildConfirmacionBody(Reserva r) {
        Huesped g = r.getHuesped();
        return "Estimado/a " + (g != null ? g.getNombre() + " " + g.getApellidos() : "") + ",\n\n" +
                "Confirmamos su reserva " + r.getNumeroReserva() + ".\n" +
                "Entrada: " + r.getFechaEntrada() + "\n" +
                "Salida:  " + r.getFechaSalida() + "\n" +
                (r.getHabitacion() != null ? "Habitación: " + r.getHabitacion().getNumero() + "\n" : "") +
                (r.getPrecioTotal() != null ? "Importe: " + r.getPrecioTotal() + " €\n" : "") +
                "\nAtentamente,\nEl equipo del hotel";
    }

    private PlantillaEmail buscarPlantillaTipo(PlantillaEmail.Tipo tipo) throws SQLException {
        return PlantillaEmailDAO.obtenerTodas().stream()
                .filter(t -> t.getTipo() == tipo && t.isActivo())
                .findFirst().orElse(null);
    }

    private Map<String, String> buildVarsFactura(Factura inv) {
        Map<String, String> vars = buildVars(inv.getHuesped(), inv.getReserva());
        vars.put("invoice_number", inv.getNumeroFactura() != null ? inv.getNumeroFactura() : "");
        vars.put("total_amount",   inv.getImporteTotal() != null ? inv.getImporteTotal().toPlainString() : "");
        vars.put("issue_date",     inv.getFechaEmision() != null ? inv.getFechaEmision().toString() : "");
        return vars;
    }

    private String buildCuerpoFactura(Factura inv) {
        Huesped g = inv.getHuesped();
        return "Estimado/a " + (g != null ? g.getNombre() + " " + g.getApellidos() : "") + ",\n\n" +
                "Adjunto encontrará la factura " + inv.getNumeroFactura() + ".\n" +
                (inv.getImporteTotal() != null ? "Importe total: " + inv.getImporteTotal() + " €\n" : "") +
                (inv.getFechaEmision() != null ? "Fecha: " + inv.getFechaEmision() + "\n" : "") +
                "\nGracias por su visita.\n\nAtentamente,\nEl equipo del hotel";
    }

    private int parsePort(String s) {
        try { return Integer.parseInt(s.trim()); }
        catch (NumberFormatException e) { return 587; }
    }
}

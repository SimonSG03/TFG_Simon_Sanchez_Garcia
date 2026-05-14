package es.simonsg.pmsuite.controller;

import es.simonsg.pmsuite.dao.HotelDAO;
import es.simonsg.pmsuite.dao.ReservaDAO;
import es.simonsg.pmsuite.dao.UsuarioDAO;
import es.simonsg.pmsuite.model.Hotel;
import es.simonsg.pmsuite.model.Factura;
import es.simonsg.pmsuite.model.Reserva;
import es.simonsg.pmsuite.model.Usuario;
import javafx.animation.KeyFrame;
import javafx.animation.PauseTransition;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Bounds;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.geometry.Side;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.control.MenuButton;
import javafx.scene.control.MenuItem;
import javafx.scene.control.Separator;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.stage.Popup;
import javafx.util.Duration;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class IndexController {

    @FXML private Label      fecha_label;
    @FXML private Label      hotel_label;
    @FXML private BorderPane rootBorderPane;
    @FXML private TextField  searchField;
    @FXML private HBox       searchHBox;
    @FXML private Button     infoIconBtn;
    @FXML private Button     menuIconBtn;
    @FXML private Button     onoffBtn;
    @FXML private MenuButton usuarioMenuBtn;

    private ContextMenu soporteMenu;
    private Usuario     usuarioActual;

    private Node vistaInicio;

    private final DateTimeFormatter formatter =
            DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    private Popup searchPopup;
    private VBox  searchContainer;

    @FXML
    public void initialize() {
        actualizarFechaHora();

        Timeline timeline = new Timeline(
                new KeyFrame(Duration.minutes(1), e -> actualizarFechaHora())
        );
        timeline.setCycleCount(Timeline.INDEFINITE);
        timeline.play();

        // Hotel name
        try {
            Hotel hotel = HotelDAO.get();
            hotel_label.setText(hotel.getNombre() != null ? hotel.getNombre() : "Hotel");
        } catch (Exception e) {
            hotel_label.setText("Hotel");
        }

        // Tooltip on the info button
        Tooltip versionTip = new Tooltip("PMSuite v1.0");
        versionTip.setStyle("-fx-font-size: 12px;");
        infoIconBtn.setTooltip(versionTip);

        // Support context menu for the menu icon button
        MenuItem itemSoporte = new MenuItem("📧  Contactar con Soporte");
        itemSoporte.setOnAction(e -> contactarSoporte());
        soporteMenu = new ContextMenu(itemSoporte);

        // Global search popup
        setupSearch();

        // User switcher in top bar
        cargarUsuarios();
    }

    // ── User switcher ─────────────────────────────────────────────────────────

    private void cargarUsuarios() {
        try {
            List<Usuario> usuarios = UsuarioDAO.obtenerTodos();
            usuarioMenuBtn.getItems().clear();

            for (Usuario u : usuarios) {
                String label = u.getNombreCompleto() != null && !u.getNombreCompleto().isBlank()
                        ? u.getNombreCompleto() : u.getEmail();
                String sublabel = u.getRol() != null ? u.getRol().toSpanish() : "";

                Label nombre = new Label(label);
                nombre.setStyle("-fx-font-weight: bold; -fx-font-size: 12px;");
                Label rol = new Label(sublabel);
                rol.setStyle("-fx-font-size: 10px; -fx-text-fill: #9CA3AF;");
                VBox content = new VBox(1, nombre, rol);

                MenuItem item = new MenuItem();
                item.setGraphic(content);
                item.setOnAction(e -> seleccionarUsuario(u));
                usuarioMenuBtn.getItems().add(item);
            }

            if (!usuarios.isEmpty()) {
                usuarioMenuBtn.getItems().add(new SeparatorMenuItem());
                MenuItem cerrarSesion = new MenuItem("Cerrar sesión");
                cerrarSesion.setStyle("-fx-text-fill: #DC2626;");
                cerrarSesion.setOnAction(e -> cerrarApp());
                usuarioMenuBtn.getItems().add(cerrarSesion);

                seleccionarUsuario(usuarios.get(0));
            }
        } catch (Exception e) {
            usuarioMenuBtn.setText("Sin conexión");
        }
    }

    private void seleccionarUsuario(Usuario u) {
        usuarioActual = u;
        String display = u.getNombreCompleto() != null && !u.getNombreCompleto().isBlank()
                ? u.getNombreCompleto() : u.getEmail();
        usuarioMenuBtn.setText(display);
    }

    public Usuario getUsuarioActual() {
        return usuarioActual;
    }

    // ── Top-bar actions ───────────────────────────────────────────────────────

    @FXML
    private void mostrarMenuSoporte() {
        soporteMenu.show(menuIconBtn, Side.BOTTOM, 0, 4);
    }

    private void contactarSoporte() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Soporte PMSuite");
        alert.setHeaderText("Contactar con Soporte Técnico");
        alert.setContentText(
                "Email:    soporte@pmsuite.es\n" +
                        "Teléfono: +34 900 123 456\n" +
                        "Horario:  Lun – Vie  9:00 – 18:00");
        alert.showAndWait();
    }

    @FXML
    private void cerrarApp() {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Cerrar PMSuite");
        confirm.setHeaderText("¿Cerrar la aplicación?");
        confirm.setContentText("Se cerrarán todas las sesiones activas.");
        confirm.showAndWait().ifPresent(r -> {
            if (r == ButtonType.OK) Platform.exit();
        });
    }

    // ── Search popup ──────────────────────────────────────────────────────────

    private void setupSearch() {
        searchContainer = new VBox();
        searchContainer.setStyle(
                "-fx-background-color: white;" +
                        "-fx-border-color: #E5E7EB;" +
                        "-fx-border-radius: 10;" +
                        "-fx-background-radius: 10;" +
                        "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.18), 14, 0, 0, 5);");
        searchContainer.setMinWidth(430);
        searchContainer.setMaxWidth(430);

        searchPopup = new Popup();
        searchPopup.setAutoHide(true);
        searchPopup.setAutoFix(false);
        searchPopup.getContent().add(searchContainer);

        PauseTransition debounce = new PauseTransition(Duration.millis(240));

        searchField.textProperty().addListener((obs, old, text) -> {
            debounce.stop();
            if (text == null || text.isBlank() || text.length() < 2) {
                searchPopup.hide();
                return;
            }
            debounce.setOnFinished(ev -> buscarYMostrar(text.trim()));
            debounce.playFromStart();
        });

        searchField.setOnKeyPressed(e -> {
            if (e.getCode() == KeyCode.ESCAPE) {
                searchPopup.hide();
                searchField.clear();
            }
        });
    }

    private void buscarYMostrar(String query) {
        searchContainer.getChildren().clear();
        try {
            List<Reserva> results = ReservaDAO.buscar(query);

            if (results.isEmpty()) {
                Label noRes = new Label("Sin resultados para \"" + query + "\"");
                noRes.setStyle("-fx-padding: 14 18; -fx-text-fill: #6B7280; -fx-font-size: 12px;");
                searchContainer.getChildren().add(noRes);
            } else {
                for (int i = 0; i < results.size(); i++) {
                    searchContainer.getChildren().add(crearFilaResultado(results.get(i)));
                    if (i < results.size() - 1) {
                        Separator sep = new Separator();
                        sep.setStyle("-fx-padding: 0; -fx-opacity: 0.4;");
                        searchContainer.getChildren().add(sep);
                    }
                }
            }

            // Position popup just below the search HBox
            Bounds sb = searchHBox.localToScreen(searchHBox.getBoundsInLocal());
            if (sb != null) {
                searchPopup.show(searchField.getScene().getWindow(),
                        sb.getMinX(), sb.getMaxY() + 6);
            }

        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    private HBox crearFilaResultado(Reserva r) {
        String statusColor = switch (r.getEstado()) {
            case CONFIRMADA   -> "#2A9D8F";
            case REGISTRADA  -> "#16A34A";
            case CHECKOUT_REALIZADO -> "#9CA3AF";
            case CANCELADA   -> "#DC2626";
            default          -> "#F59E0B";
        };
        String statusLabel = switch (r.getEstado()) {
            case CONFIRMADA   -> "Confirmada";
            case REGISTRADA  -> "Check-in";
            case CHECKOUT_REALIZADO -> "Check-out";
            case CANCELADA   -> "Cancelada";
            default          -> "Pendiente";
        };

        Circle dot = new Circle(5, Color.web(statusColor));

        String guest = r.getHuesped() != null
                ? r.getHuesped().getApellidos() + ", " + r.getHuesped().getNombre()
                : "—";
        String room = r.getHabitacion() != null ? "Hab. " + r.getHabitacion().getNumero() : "Sin hab.";
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        String dates = r.getFechaEntrada().format(fmt) + " → " + r.getFechaSalida().format(fmt);

        Label guestLbl = new Label(guest);
        guestLbl.setStyle("-fx-font-weight: bold; -fx-font-size: 12px; -fx-text-fill: #111827;");

        Label detLbl = new Label(r.getNumeroReserva() + "  ·  " + room + "  ·  " + dates);
        detLbl.setStyle("-fx-font-size: 11px; -fx-text-fill: #6B7280;");

        VBox infoBox = new VBox(2, guestLbl, detLbl);

        Label badge = new Label(statusLabel);
        badge.setStyle(
                "-fx-background-color: " + statusColor + "22;" +
                        "-fx-text-fill: "         + statusColor + ";" +
                        "-fx-font-size: 10px; -fx-padding: 2 8; -fx-background-radius: 10;");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        HBox row = new HBox(10, dot, infoBox, spacer, badge);
        row.setAlignment(Pos.CENTER_LEFT);
        row.setPadding(new Insets(10, 16, 10, 16));
        row.setStyle("-fx-cursor: hand;");

        row.setOnMouseEntered(e ->
                row.setStyle("-fx-background-color: #F3F4F6; -fx-cursor: hand;"));
        row.setOnMouseExited(e ->
                row.setStyle("-fx-cursor: hand;"));

        row.setOnMouseClicked(e -> {
            searchPopup.hide();
            searchField.clear();
            ReservaTicketDialog.show(r);
        });

        return row;
    }

    // ── Navigation ────────────────────────────────────────────────────────────

    @FXML
    private void abrirCentral() {
        try {
            if (vistaInicio == null) vistaInicio = rootBorderPane.getCenter();
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/es/simonsg/pmsuite/central.fxml"));
            Node centralView = loader.load();
            CentralController centralController = loader.getController();
            centralController.setIndexController(this);
            rootBorderPane.setCenter(centralView);
        } catch (IOException e) { e.printStackTrace(); }
    }

    @FXML
    private void abrirPlanning() {
        try {
            if (vistaInicio == null) vistaInicio = rootBorderPane.getCenter();
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/es/simonsg/pmsuite/planning.fxml"));
            Node planningView = loader.load();
            PlanningController planningController = loader.getController();
            planningController.setIndexController(this);
            rootBorderPane.setCenter(planningView);
        } catch (IOException e) { e.printStackTrace(); }
    }

    public void abrirPlanningConHabitacion(int roomNumber) {
        try {
            if (vistaInicio == null) vistaInicio = rootBorderPane.getCenter();
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/es/simonsg/pmsuite/planning.fxml"));
            Node planningView = loader.load();
            PlanningController planningController = loader.getController();
            planningController.setIndexController(this);
            rootBorderPane.setCenter(planningView);
            Platform.runLater(() -> planningController.destacarHabitacion(roomNumber));
        } catch (IOException e) { e.printStackTrace(); }
    }

    @FXML
    private void abrirRecepcion() {
        try {
            if (vistaInicio == null) vistaInicio = rootBorderPane.getCenter();
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/es/simonsg/pmsuite/recepcion.fxml"));
            Node recepcionView = loader.load();
            RecepcionController recepcionController = loader.getController();
            recepcionController.setIndexController(this);
            rootBorderPane.setCenter(recepcionView);
        } catch (IOException e) { e.printStackTrace(); }
    }

    @FXML
    private void abrirPrecios() {
        try {
            if (vistaInicio == null) vistaInicio = rootBorderPane.getCenter();
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/es/simonsg/pmsuite/precios.fxml"));
            Node preciosView = loader.load();
            PreciosController preciosController = loader.getController();
            preciosController.setIndexController(this);
            rootBorderPane.setCenter(preciosView);
        } catch (IOException e) { e.printStackTrace(); }
    }

    @FXML
    private void abrirMantenimiento() {
        try {
            if (vistaInicio == null) vistaInicio = rootBorderPane.getCenter();
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/es/simonsg/pmsuite/mantenimiento.fxml"));
            Node mantenimientoView = loader.load();
            MantenimientoController mantenimientoController = loader.getController();
            mantenimientoController.setIndexController(this);
            rootBorderPane.setCenter(mantenimientoView);
        } catch (IOException e) { e.printStackTrace(); }
    }

    public void abrirMantenimientoConHabitacion(int roomId, String roomName) {
        try {
            if (vistaInicio == null) vistaInicio = rootBorderPane.getCenter();
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/es/simonsg/pmsuite/mantenimiento.fxml"));
            Node mantenimientoView = loader.load();
            MantenimientoController mantenimientoController = loader.getController();
            mantenimientoController.setIndexController(this);
            rootBorderPane.setCenter(mantenimientoView);
            mantenimientoController.mostrarHabitacion(roomId, roomName);
        } catch (IOException e) { e.printStackTrace(); }
    }

    public void abrirMantenimientoConNumeroHabitacion(int roomNumber, String roomName) {
        try {
            if (vistaInicio == null) vistaInicio = rootBorderPane.getCenter();
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/es/simonsg/pmsuite/mantenimiento.fxml"));
            Node mantenimientoView = loader.load();
            MantenimientoController mantenimientoController = loader.getController();
            mantenimientoController.setIndexController(this);
            rootBorderPane.setCenter(mantenimientoView);
            mantenimientoController.mostrarHabitacionPorNumero(roomNumber, roomName);
        } catch (IOException e) { e.printStackTrace(); }
    }

    @FXML
    private void abrirAdministracion() {
        abrirAdministracionEnPagos(-1);
    }

    public void abrirAdministracionEnPagos(int reservationId) {
        try {
            if (vistaInicio == null) vistaInicio = rootBorderPane.getCenter();
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/es/simonsg/pmsuite/administracion.fxml"));
            Node administracionView = loader.load();
            AdministracionController administracionController = loader.getController();
            administracionController.setIndexController(this);
            rootBorderPane.setCenter(administracionView);
            if (reservationId > 0) {
                administracionController.abrirPagosConReserva(reservationId);
            }
        } catch (IOException e) { e.printStackTrace(); }
    }

    @FXML
    private void abrirRestauracion() {
        try {
            if (vistaInicio == null) vistaInicio = rootBorderPane.getCenter();
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/es/simonsg/pmsuite/restauracion.fxml"));
            Node restauracionView = loader.load();
            RestauracionController restauracionController = loader.getController();
            restauracionController.setIndexController(this);
            rootBorderPane.setCenter(restauracionView);
        } catch (IOException e) { e.printStackTrace(); }
    }

    @FXML
    private void abrirComunicacion() {
        try {
            if (vistaInicio == null) vistaInicio = rootBorderPane.getCenter();
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/es/simonsg/pmsuite/comunicacion.fxml"));
            Node comunicacionView = loader.load();
            ComunicacionController comunicacionController = loader.getController();
            comunicacionController.setIndexController(this);
            rootBorderPane.setCenter(comunicacionView);
        } catch (IOException e) { e.printStackTrace(); }
    }

    public void abrirComunicacionConFactura(Factura invoice, File pdfAdjunto) {
        try {
            if (vistaInicio == null) vistaInicio = rootBorderPane.getCenter();
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/es/simonsg/pmsuite/comunicacion.fxml"));
            Node comunicacionView = loader.load();
            ComunicacionController comunicacionController = loader.getController();
            comunicacionController.setIndexController(this);
            rootBorderPane.setCenter(comunicacionView);
            comunicacionController.precargarParaFactura(invoice, pdfAdjunto);
        } catch (IOException e) { e.printStackTrace(); }
    }

    @FXML
    private void abrirInformes() {
        try {
            if (vistaInicio == null) vistaInicio = rootBorderPane.getCenter();
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/es/simonsg/pmsuite/informes.fxml"));
            Node informesView = loader.load();
            InformesController informesController = loader.getController();
            informesController.setIndexController(this);
            rootBorderPane.setCenter(informesView);
        } catch (IOException e) { e.printStackTrace(); }
    }

    @FXML
    private void abrirMaestros() {
        try {
            if (vistaInicio == null) vistaInicio = rootBorderPane.getCenter();
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/es/simonsg/pmsuite/maestros.fxml"));
            Node maestrosView = loader.load();
            MaestrosController maestrosController = loader.getController();
            maestrosController.setIndexController(this);
            rootBorderPane.setCenter(maestrosView);
        } catch (IOException e) { e.printStackTrace(); }
    }

    public void mostrarInicio() {
        if (vistaInicio != null) {
            rootBorderPane.setCenter(vistaInicio);
        }
    }

    private void actualizarFechaHora() {
        LocalDateTime ahora = LocalDateTime.now();
        fecha_label.setText(ahora.format(formatter));
    }
}

package es.simonsg.pmsuite.controller;

import es.simonsg.pmsuite.dao.FacturaDAO;
import es.simonsg.pmsuite.dao.ReservaDAO;
import es.simonsg.pmsuite.dao.HabitacionDAO;
import es.simonsg.pmsuite.model.Factura;
import es.simonsg.pmsuite.model.LineaFactura;
import es.simonsg.pmsuite.model.Reserva;

import java.math.BigDecimal;
import java.sql.SQLException;

import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.util.Duration;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;
import javafx.scene.shape.Rectangle;

import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.util.*;
import java.util.stream.Collectors;

public class PlanningController {

    // ── FXML ──────────────────────────────────────────────────────────────────
    @FXML private Label          updateLabel;
    @FXML private Label          subtitleLabel;
    @FXML private Label          labelMes;
    @FXML private Label          overbookingAlert;
    @FXML private ComboBox<String> filtroTipo;
    @FXML private ComboBox<String> filtroEstado;
    @FXML private HBox           leyendaBar;
    @FXML private BorderPane     rackPane;

    // ── Constantes de layout ──────────────────────────────────────────────────
    private static final double ROOM_COL_W   = 160;
    private static final double MIN_DAY_COL_W = 36.0;
    private static final double HEADER_H     = 52;
    private static final double ROW_H        = 38;
    private static final double FLOOR_H      = 26;
    private double DAY_COL_W = 46.0;

    // ── Estado ────────────────────────────────────────────────────────────────
    private YearMonth mesActual = YearMonth.now();
    private List<HabRack> habitaciones;
    private List<ResRack> reservas;
    private IndexController  indexController;

    // Referencias dinámicas para sync de scroll
    private Pane dateHeaderContent;
    private Pane roomsContent;
    private ScrollPane mainScroll;

    // Para destacarHabitacion
    private Map<Integer, Double> lastRoomY;
    private Pane                 lastContentPane;
    private double               lastTotalH;

    // ── Drag & drop ───────────────────────────────────────────────────────────
    private double dragStartX, dragStartY;
    private double blockOrigX, blockOrigY;
    private ResRack draggedReserva;
    private LocalDate dragFirstDay;
    private List<GridRow> dragGridRows;
    private Map<Integer, Double> dragRoomY;
    private int dragNumDays;

    // ── Context menu tracking ─────────────────────────────────────────────────
    private ContextMenu activeContextMenu;

    // ── Layout rebuild flag ───────────────────────────────────────────────────
    private boolean rackBuilding = false;

    // ═════════════════════════════════════════════════════════════════════════
    //  INIT
    // ═════════════════════════════════════════════════════════════════════════

    @FXML
    public void initialize() {
        initData();
        initFilters();
        initLegend();
        actualizarLabel();
        buildRoomRack();

        // Rebuild once with actual viewport width after initial layout pass
        javafx.beans.value.ChangeListener<Number>[] firstLayout = new javafx.beans.value.ChangeListener[1];
        firstLayout[0] = (obs, old, newW) -> {
            if (old.doubleValue() == 0 && newW.doubleValue() > 0) {
                rackPane.widthProperty().removeListener(firstLayout[0]);
                Platform.runLater(this::buildRoomRack);
            }
        };
        rackPane.widthProperty().addListener(firstLayout[0]);
    }

    private void initData() {
        habitaciones = cargarHabitacionesDB();
        reservas     = cargarReservasDB();
    }

    private void initFilters() {
        // Tipos
        List<String> tipos = new ArrayList<>();
        tipos.add("Todos los tipos");
        for (TipoRack t : TipoRack.values()) tipos.add(t.getNombre());
        filtroTipo.getItems().setAll(tipos);
        filtroTipo.getSelectionModel().selectFirst();

        // Estados
        filtroEstado.getItems().setAll(
                "Todos los estados", "Libre", "Ocupada", "Bloqueada", "Fuera de servicio"
        );
        filtroEstado.getSelectionModel().selectFirst();
    }

    private void initLegend() {
        leyendaBar.getChildren().clear();
        addLegendItem(leyendaBar, "#2A9D8F", "Confirmada");
        addLegendItem(leyendaBar, "#F59E0B", "Provisional");
        addLegendItem(leyendaBar, "#16A34A", "Check-in realizado");
        addLegendItem(leyendaBar, "#9CA3AF", "Check-out");
        addLegendItem(leyendaBar, "#DC2626", "Bloqueada");
        addLegendItem(leyendaBar, "#7C3AED", "Sobrereserva");
    }

    private void addLegendItem(HBox bar, String color, String texto) {
        Region dot = new Region();
        dot.setPrefSize(12, 12);
        dot.setMaxSize(12, 12);
        dot.setStyle("-fx-background-color: " + color + "; -fx-background-radius: 3;");
        Label lbl = new Label(texto);
        lbl.setStyle("-fx-font-size: 11px; -fx-text-fill: #4B5563;");
        HBox item = new HBox(5, dot, lbl);
        item.setAlignment(Pos.CENTER_LEFT);
        bar.getChildren().add(item);
    }

    private void actualizarLabel() {
        updateLabel.setText("Actualizado: " +
                LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm")));
        String mes = mesActual.getMonth()
                .getDisplayName(TextStyle.FULL, new Locale("es", "ES"));
        mes = mes.substring(0, 1).toUpperCase() + mes.substring(1);
        labelMes.setText(mes + " " + mesActual.getYear());
        subtitleLabel.setText("Room Rack · " + mes + " " + mesActual.getYear());
    }

    // ═════════════════════════════════════════════════════════════════════════
    //  DB DATA LOADING
    // ═════════════════════════════════════════════════════════════════════════

    private List<HabRack> cargarHabitacionesDB() {
        try {
            List<es.simonsg.pmsuite.model.Habitacion> rooms = HabitacionDAO.obtenerTodas();
            if (rooms.isEmpty()) return crearHabitaciones();
            List<HabRack> list = new ArrayList<>();
            for (es.simonsg.pmsuite.model.Habitacion r : rooms) {
                int num = parseRoomNumber(r.getNumero());
                if (num <= 0) continue;
                list.add(new HabRack(num, mapTipo(r.getTipoHabitacion()), r.getPlanta(), mapEstado(r.getEstado())));
            }
            return list.isEmpty() ? crearHabitaciones() : list;
        } catch (SQLException e) {
            e.printStackTrace();
            return crearHabitaciones();
        }
    }

    private List<ResRack> cargarReservasDB() {
        try {
            LocalDate from  = mesActual.atDay(1);
            LocalDate to    = mesActual.atEndOfMonth();
            List<es.simonsg.pmsuite.model.Reserva> dbRes = ReservaDAO.obtenerPorRango(from, to);

            List<ResRack> list = new ArrayList<>();
            for (es.simonsg.pmsuite.model.Reserva r : dbRes) {
                if (r.getHabitacion() == null) continue;
                int roomNum = parseRoomNumber(r.getHabitacion().getNumero());
                if (roomNum <= 0) continue;
                String guest = r.getHuesped() != null
                        ? r.getHuesped().getApellidos() + ", " + r.getHuesped().getNombre()
                        : "Desconocido";
                list.add(new ResRack(r.getNumeroReserva(), roomNum, guest,
                        r.getFechaEntrada(), r.getFechaSalida(), mapEstadoResRack(r.getEstado())));
            }

            // Bloques sintéticos para habitaciones bloqueadas / fuera de servicio
            LocalDate monthStart = mesActual.atDay(1);
            LocalDate monthEnd   = mesActual.atEndOfMonth().plusDays(1);
            for (HabRack h : habitaciones) {
                if (h.getEstado() == EstadoHabRack.BLOQUEADA
                        || h.getEstado() == EstadoHabRack.FUERA_SERVICIO) {
                    String motivo = h.getEstado() == EstadoHabRack.FUERA_SERVICIO
                            ? "Fuera de servicio" : "Bloqueada";
                    list.add(new ResRack("BLQ-" + h.getNumero(), h.getNumero(),
                            motivo, monthStart, monthEnd, EstadoResRack.BLOQUEADA));
                }
            }

            return list.isEmpty() ? crearReservas() : list;
        } catch (SQLException e) {
            e.printStackTrace();
            return crearReservas();
        }
    }

    private int parseRoomNumber(String number) {
        try { return Integer.parseInt(number); }
        catch (NumberFormatException e) { return -1; }
    }

    private TipoRack mapTipo(es.simonsg.pmsuite.model.TipoHabitacion rt) {
        if (rt == null) return TipoRack.DBL_STANDARD;
        return switch (rt.getCodigo()) {
            case "SGL-ECO" -> TipoRack.SGL_ECONOMY;
            case "SGL-SUP" -> TipoRack.SGL_STANDARD;
            case "DBL-STD" -> TipoRack.DBL_STANDARD;
            case "DBL-MAR" -> TipoRack.DBL_VISTA_MAR;
            case "DBL-SUP" -> TipoRack.DBL_SUPERIOR;
            case "SJR"     -> TipoRack.SUITE_JUNIOR;
            case "STE-PRE" -> TipoRack.SUITE_PREMIUM;
            default        -> TipoRack.DBL_STANDARD;
        };
    }

    private EstadoHabRack mapEstado(es.simonsg.pmsuite.model.Habitacion.Estado status) {
        if (status == null) return EstadoHabRack.LIBRE;
        return switch (status) {
            case DISPONIBLE      -> EstadoHabRack.LIBRE;
            case OCUPADA       -> EstadoHabRack.OCUPADA;
            case BLOQUEADA        -> EstadoHabRack.BLOQUEADA;
            case LIMPIEZA       -> EstadoHabRack.BLOQUEADA;
            case FUERA_SERVICIO -> EstadoHabRack.FUERA_SERVICIO;
        };
    }

    private EstadoResRack mapEstadoResRack(Reserva.Estado status) {
        if (status == null) return EstadoResRack.PROVISIONAL;
        return switch (status) {
            case CONFIRMADA   -> EstadoResRack.CONFIRMADA;
            case PENDIENTE     -> EstadoResRack.PROVISIONAL;
            case REGISTRADA  -> EstadoResRack.CHECKIN_REALIZADO;
            case CHECKOUT_REALIZADO -> EstadoResRack.CHECKOUT_REALIZADO;
            default          -> EstadoResRack.PROVISIONAL;
        };
    }

    // ═════════════════════════════════════════════════════════════════════════
    //  MOCK DATA (fallback when DB empty)
    // ═════════════════════════════════════════════════════════════════════════

    private List<HabRack> crearHabitaciones() {
        List<HabRack> list = new ArrayList<>();
        // Planta 1
        list.add(new HabRack(101, TipoRack.SGL_ECONOMY,  1, EstadoHabRack.OCUPADA));
        list.add(new HabRack(102, TipoRack.SGL_ECONOMY,  1, EstadoHabRack.OCUPADA));
        list.add(new HabRack(103, TipoRack.DBL_STANDARD, 1, EstadoHabRack.OCUPADA));
        list.add(new HabRack(104, TipoRack.DBL_STANDARD, 1, EstadoHabRack.OCUPADA));
        list.add(new HabRack(105, TipoRack.DBL_STANDARD, 1, EstadoHabRack.LIBRE));
        list.add(new HabRack(106, TipoRack.DBL_STANDARD, 1, EstadoHabRack.LIBRE));
        // Planta 2
        list.add(new HabRack(201, TipoRack.DBL_STANDARD, 2, EstadoHabRack.OCUPADA));
        list.add(new HabRack(202, TipoRack.DBL_STANDARD, 2, EstadoHabRack.LIBRE));
        list.add(new HabRack(203, TipoRack.DBL_VISTA_MAR,2, EstadoHabRack.OCUPADA));
        list.add(new HabRack(204, TipoRack.DBL_VISTA_MAR,2, EstadoHabRack.BLOQUEADA));
        list.add(new HabRack(205, TipoRack.DBL_VISTA_MAR,2, EstadoHabRack.LIBRE));
        list.add(new HabRack(206, TipoRack.DBL_SUPERIOR, 2, EstadoHabRack.OCUPADA));
        list.add(new HabRack(207, TipoRack.DBL_SUPERIOR, 2, EstadoHabRack.LIBRE));
        // Planta 3
        list.add(new HabRack(301, TipoRack.DBL_SUPERIOR, 3, EstadoHabRack.OCUPADA));
        list.add(new HabRack(302, TipoRack.DBL_SUPERIOR, 3, EstadoHabRack.BLOQUEADA));
        list.add(new HabRack(303, TipoRack.SUITE_JUNIOR, 3, EstadoHabRack.OCUPADA));
        list.add(new HabRack(304, TipoRack.SUITE_JUNIOR, 3, EstadoHabRack.LIBRE));
        list.add(new HabRack(305, TipoRack.SUITE_JUNIOR, 3, EstadoHabRack.OCUPADA));
        list.add(new HabRack(306, TipoRack.SUITE_JUNIOR, 3, EstadoHabRack.FUERA_SERVICIO));
        // Planta 4
        list.add(new HabRack(401, TipoRack.SUITE_JUNIOR,  4, EstadoHabRack.LIBRE));
        list.add(new HabRack(402, TipoRack.SUITE_PREMIUM, 4, EstadoHabRack.OCUPADA));
        list.add(new HabRack(403, TipoRack.SUITE_PREMIUM, 4, EstadoHabRack.LIBRE));
        return list;
    }

    private List<ResRack> crearReservas() {
        int y = YearMonth.now().getYear();
        int m = YearMonth.now().getMonthValue();

        List<ResRack> list = new ArrayList<>();
        list.add(new ResRack("R001", 101, "García Ruiz, Luis",   ld(y,m,5),  ld(y,m,12), EstadoResRack.CHECKIN_REALIZADO));
        list.add(new ResRack("R002", 101, "Ferrer Mas, Pablo",   ld(y,m,14), ld(y,m,20), EstadoResRack.CONFIRMADA));
        // Sobrereserva en hab. 102
        list.add(new ResRack("R003", 102, "Smith, John A.",      ld(y,m,7),  ld(y,m,13), EstadoResRack.CHECKIN_REALIZADO));
        list.add(new ResRack("R004", 102, "López Sanz, Carmen",  ld(y,m,10), ld(y,m,16), EstadoResRack.CONFIRMADA));
        // Hab. 103
        list.add(new ResRack("R005", 103, "Müller, Hans K.",     ld(y,m,1),  ld(y,m,6),  EstadoResRack.CHECKOUT_REALIZADO));
        list.add(new ResRack("R006", 103, "Tanaka, Kenji",       ld(y,m,10), ld(y,m,18), EstadoResRack.CHECKIN_REALIZADO));
        // Hab. 104
        list.add(new ResRack("R007", 104, "Wilson, Thomas",      ld(y,m,5),  ld(y,m,12), EstadoResRack.CHECKIN_REALIZADO));
        // Hab. 105
        list.add(new ResRack("R008", 105, "Dubois, Marie",       ld(y,m,15), ld(y,m,22), EstadoResRack.CONFIRMADA));
        // Hab. 106
        list.add(new ResRack("R009", 106, "Rossi, Marco",        ld(y,m,20), ld(y,m,26), EstadoResRack.PROVISIONAL));
        // Hab. 201
        list.add(new ResRack("R010", 201, "Brown, Sarah J.",     ld(y,m,8),  ld(y,m,14), EstadoResRack.CHECKIN_REALIZADO));
        // Hab. 202
        list.add(new ResRack("R011", 202, "Anderson, Emma",      ld(y,m,3),  ld(y,m,8),  EstadoResRack.CHECKOUT_REALIZADO));
        list.add(new ResRack("R012", 202, "Pérez Gómez, Juan",   ld(y,m,13), ld(y,m,20), EstadoResRack.CONFIRMADA));
        // Hab. 203
        list.add(new ResRack("R013", 203, "Chen, Wei",           ld(y,m,8),  ld(y,m,15), EstadoResRack.CHECKIN_REALIZADO));
        // Hab. 204 - Bloqueada
        list.add(new ResRack("R014", 204, "Mantenimiento",       ld(y,m,10), ld(y,m,17), EstadoResRack.BLOQUEADA));
        // Hab. 205
        list.add(new ResRack("R015", 205, "Novak, Karel",        ld(y,m,1),  ld(y,m,5),  EstadoResRack.CHECKOUT_REALIZADO));
        list.add(new ResRack("R016", 205, "Santos, Ana L.",      ld(y,m,20), ld(y,m,27), EstadoResRack.PROVISIONAL));
        // Hab. 206
        list.add(new ResRack("R017", 206, "Hernández, Rosa M.",  ld(y,m,5),  ld(y,m,14), EstadoResRack.CHECKIN_REALIZADO));
        // Hab. 207
        list.add(new ResRack("R018", 207, "Kim, Jae-won",        ld(y,m,22), ld(y,m,mesActual.lengthOfMonth()), EstadoResRack.CONFIRMADA));
        // Hab. 301
        list.add(new ResRack("R019", 301, "Oliveira, Bruno",     ld(y,m,10), ld(y,m,20), EstadoResRack.CHECKIN_REALIZADO));
        // Hab. 302 - Bloqueada (limpieza)
        list.add(new ResRack("R020", 302, "Limpieza profunda",   ld(y,m,5),  ld(y,m,14), EstadoResRack.BLOQUEADA));
        // Hab. 303
        list.add(new ResRack("R021", 303, "Williams, Jack",      ld(y,m,8),  ld(y,m,18), EstadoResRack.CONFIRMADA));
        // Hab. 304
        list.add(new ResRack("R022", 304, "García-Soto, María",  ld(y,m,15), ld(y,m,25), EstadoResRack.CONFIRMADA));
        // Hab. 305
        list.add(new ResRack("R023", 305, "Johnson, David",      ld(y,m,7),  ld(y,m,14), EstadoResRack.CHECKIN_REALIZADO));
        // Hab. 306 - Fuera de servicio (bloqueo de todo el mes)
        list.add(new ResRack("R024", 306, "Fuera de servicio",   ld(y,m,1),  ld(y,m,mesActual.lengthOfMonth()), EstadoResRack.BLOQUEADA));
        // Hab. 401
        list.add(new ResRack("R025", 401, "Ferrari, Luca",       ld(y,m,12), ld(y,m,22), EstadoResRack.PROVISIONAL));
        // Hab. 402
        list.add(new ResRack("R026", 402, "Müller, Klaus",       ld(y,m,3),  ld(y,m,10), EstadoResRack.CHECKIN_REALIZADO));
        list.add(new ResRack("R027", 402, "Thompson, Rachel",    ld(y,m,10), ld(y,m,17), EstadoResRack.CONFIRMADA));
        // Hab. 403
        list.add(new ResRack("R028", 403, "Williams, Sarah",     ld(y,m,22), ld(y,m,mesActual.lengthOfMonth()), EstadoResRack.CONFIRMADA));
        return list;
    }

    private LocalDate ld(int y, int m, int d) {
        int maxDay = YearMonth.of(y, m).lengthOfMonth();
        return LocalDate.of(y, m, Math.min(d, maxDay));
    }

    // ═════════════════════════════════════════════════════════════════════════
    //  CONSTRUCCIÓN DEL ROOM RACK
    // ═════════════════════════════════════════════════════════════════════════

    @FXML
    public void buildRoomRack() {
        actualizarLabel();
        rackPane.getChildren().clear();
        rackPane.setTop(null);
        rackPane.setLeft(null);
        rackPane.setCenter(null);

        LocalDate firstDay  = mesActual.atDay(1);
        int       numDays   = mesActual.lengthOfMonth();

        // Adapt column width to fill the available viewport width
        double rackW = rackPane.getWidth();
        if (rackW > ROOM_COL_W + 100) {
            DAY_COL_W = Math.max(MIN_DAY_COL_W, (rackW - ROOM_COL_W - 20) / numDays);
        }

        double    totalW    = numDays * DAY_COL_W;

        List<GridRow> gridRows  = buildGridRows();
        double        totalH    = gridRows.stream().mapToDouble(GridRow::getHeight).sum();
        Map<Integer, Double> roomY = calcRoomY(gridRows);
        lastRoomY  = roomY;
        lastTotalH = totalH;

        // Detectar sobrereservas
        Set<String> sobres = detectarSobrereservas();

        // ── Corner cell ─────────────────────────────────────────────────────
        Label corner = new Label("Habitación");
        corner.setPrefSize(ROOM_COL_W, HEADER_H);
        corner.setAlignment(Pos.CENTER_LEFT);
        corner.setPadding(new Insets(0, 0, 0, 12));
        corner.setStyle("-fx-font-weight: bold; -fx-font-size: 12px; " +
                "-fx-text-fill: #1A2E2B; -fx-background-color: #F1F5F9; " +
                "-fx-border-color: #CBD5E1; -fx-border-width: 0 1 1 0;");

        // ── Date header ─────────────────────────────────────────────────────
        dateHeaderContent = buildDateHeader(firstDay, numDays);
        Pane headerWrapper = new Pane(dateHeaderContent);
        headerWrapper.setPrefHeight(HEADER_H);
        HBox.setHgrow(headerWrapper, Priority.ALWAYS);
        Rectangle headerClip = new Rectangle(0, HEADER_H);
        headerWrapper.layoutBoundsProperty().addListener((o, ol, bounds) ->
                headerClip.setWidth(bounds.getWidth()));
        headerWrapper.setClip(headerClip);

        HBox topBar = new HBox(corner, headerWrapper);
        topBar.setStyle("-fx-background-color: #F1F5F9;");

        // ── Habitacion labels ─────────────────────────────────────────────────────
        roomsContent = buildRoomsContent(gridRows);
        Pane roomsWrapper = new Pane(roomsContent);
        roomsWrapper.setPrefWidth(ROOM_COL_W);
        Rectangle roomsClip = new Rectangle(ROOM_COL_W, 0);
        roomsWrapper.heightProperty().addListener((o, ol, newH) ->
                roomsClip.setHeight(newH.doubleValue()));
        roomsWrapper.setClip(roomsClip);
        roomsWrapper.setStyle("-fx-background-color: #F1F5F9;");

        // ── Content pane ─────────────────────────────────────────────────────
        Pane content = buildContentPane(gridRows, firstDay, numDays, totalW, totalH, roomY, sobres);
        lastContentPane = content;

        // ── Main ScrollPane ──────────────────────────────────────────────────
        mainScroll = new ScrollPane(content);
        mainScroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.ALWAYS);
        mainScroll.setVbarPolicy(ScrollPane.ScrollBarPolicy.ALWAYS);
        mainScroll.setFitToHeight(false);
        mainScroll.setFitToWidth(false);
        mainScroll.setStyle("-fx-background-color: white; -fx-border-color: transparent;");
        mainScroll.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
        HBox.setHgrow(mainScroll, Priority.ALWAYS);
        VBox.setVgrow(mainScroll, Priority.ALWAYS);

        // ── Scroll sync ──────────────────────────────────────────────────────
        mainScroll.hvalueProperty().addListener((obs, old, newVal) -> {
            double vw = mainScroll.getViewportBounds().getWidth();
            if (vw <= 0) return;
            double scrolled = newVal.doubleValue() * (totalW - vw);
            dateHeaderContent.setTranslateX(-scrolled);
        });
        mainScroll.vvalueProperty().addListener((obs, old, newVal) -> {
            double vh = mainScroll.getViewportBounds().getHeight();
            if (vh <= 0) return;
            double scrolled = newVal.doubleValue() * (totalH - vh);
            roomsContent.setTranslateY(-scrolled);
        });

        // Scroll to today on load
        Platform.runLater(() -> scrollToToday(firstDay, numDays));

        // ── Assembly ─────────────────────────────────────────────────────────
        HBox mainRow = new HBox(roomsWrapper, mainScroll);
        VBox.setVgrow(mainRow, Priority.ALWAYS);

        VBox centerBox = new VBox(topBar, mainRow);
        VBox.setVgrow(mainRow, Priority.ALWAYS);
        centerBox.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
        rackPane.setCenter(centerBox);

        // Overbooking alert
        if (!sobres.isEmpty()) {
            overbookingAlert.setText("⚠ " + sobres.size() / 2 + " sobrereserva(s) detectada(s)");
            overbookingAlert.setVisible(true);
        } else {
            overbookingAlert.setVisible(false);
        }
    }

    private void scrollToToday(LocalDate firstDay, int numDays) {
        LocalDate today = LocalDate.now();
        if (today.getYear() == firstDay.getYear() &&
                today.getMonthValue() == firstDay.getMonthValue()) {
            double totalW = numDays * DAY_COL_W;
            double vw = mainScroll.getViewportBounds().getWidth();
            if (totalW <= vw) return;
            int todayIdx = today.getDayOfMonth() - 4;
            double pos = (todayIdx * DAY_COL_W) / (totalW - vw);
            mainScroll.setHvalue(Math.max(0, Math.min(1, pos)));
        }
    }

    // ── Grid rows ────────────────────────────────────────────────────────────

    private List<GridRow> buildGridRows() {
        List<HabRack> filtered = getFilteredRooms();
        List<GridRow> rows = new ArrayList<>();
        int currentFloor = -1;
        for (HabRack h : filtered) {
            if (h.getPlanta() != currentFloor) {
                currentFloor = h.getPlanta();
                rows.add(new GridRow("PLANTA " + currentFloor));
            }
            rows.add(new GridRow(h));
        }
        return rows;
    }

    private List<HabRack> getFilteredRooms() {
        String tipo   = filtroTipo   != null ? filtroTipo.getValue()   : null;
        String estado = filtroEstado != null ? filtroEstado.getValue() : null;

        return habitaciones.stream()
                .filter(h -> tipo   == null || tipo.startsWith("Todos")   || h.getTipo().getNombre().equals(tipo))
                .filter(h -> estado == null || estado.startsWith("Todos") || matchEstado(h, estado))
                .sorted(Comparator.comparingInt(HabRack::getNumero))
                .collect(Collectors.toList());
    }

    private boolean matchEstado(HabRack h, String str) {
        return switch (str) {
            case "Libre"             -> h.getEstado() == EstadoHabRack.LIBRE;
            case "Ocupada"           -> h.getEstado() == EstadoHabRack.OCUPADA;
            case "Bloqueada"         -> h.getEstado() == EstadoHabRack.BLOQUEADA;
            case "Fuera de servicio" -> h.getEstado() == EstadoHabRack.FUERA_SERVICIO;
            default -> true;
        };
    }

    private Map<Integer, Double> calcRoomY(List<GridRow> rows) {
        Map<Integer, Double> map = new LinkedHashMap<>();
        double y = 0;
        for (GridRow row : rows) {
            if (!row.isFloorHeader && row.habitacion != null) {
                map.put(row.habitacion.getNumero(), y);
            }
            y += row.getHeight();
        }
        return map;
    }

    // ── Date header ──────────────────────────────────────────────────────────

    private Pane buildDateHeader(LocalDate firstDay, int numDays) {
        double totalW = numDays * DAY_COL_W;
        Pane pane = new Pane();
        pane.setPrefSize(totalW, HEADER_H);

        LocalDate today = LocalDate.now();

        for (int d = 0; d < numDays; d++) {
            LocalDate day = firstDay.plusDays(d);
            boolean isToday   = day.equals(today);
            boolean isWeekend = (day.getDayOfWeek() == DayOfWeek.SATURDAY ||
                    day.getDayOfWeek() == DayOfWeek.SUNDAY);

            double x = d * DAY_COL_W;

            Rectangle bg = new Rectangle(x, 0, DAY_COL_W, HEADER_H);
            if (isToday) {
                bg.setFill(Color.web("#2A9D8F"));
            } else if (isWeekend) {
                bg.setFill(Color.web("#EFF6FF"));
            } else {
                bg.setFill(Color.TRANSPARENT);
            }
            pane.getChildren().add(bg);

            String dayName = day.getDayOfWeek()
                    .getDisplayName(TextStyle.SHORT, new Locale("es", "ES"));
            dayName = dayName.substring(0,1).toUpperCase() + dayName.substring(1);

            Label numLbl = new Label(String.valueOf(day.getDayOfMonth()));
            numLbl.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: "
                    + (isToday ? "white" : (isWeekend ? "#3B82F6" : "#1A2E2B")) + ";");
            numLbl.setPrefWidth(DAY_COL_W);
            numLbl.setAlignment(Pos.CENTER);
            numLbl.setLayoutX(x);
            numLbl.setLayoutY(4);

            Label nameLbl = new Label(dayName);
            nameLbl.setStyle("-fx-font-size: 9px; -fx-text-fill: "
                    + (isToday ? "rgba(255,255,255,0.85)" : "#9CA3AF") + ";");
            nameLbl.setPrefWidth(DAY_COL_W);
            nameLbl.setAlignment(Pos.CENTER);
            nameLbl.setLayoutX(x);
            nameLbl.setLayoutY(26);

            pane.getChildren().addAll(numLbl, nameLbl);

            // vertical separator
            Line line = new Line(x, 0, x, HEADER_H);
            line.setStroke(Color.web("#CBD5E1"));
            line.setStrokeWidth(0.5);
            pane.getChildren().add(line);
        }

        // bottom border
        Line bottom = new Line(0, HEADER_H - 1, totalW, HEADER_H - 1);
        bottom.setStroke(Color.web("#CBD5E1"));
        bottom.setStrokeWidth(1);
        pane.getChildren().add(bottom);

        return pane;
    }

    // ── Habitacion labels ──────────────────────────────────────────────────────────

    private Pane buildRoomsContent(List<GridRow> rows) {
        double totalH = rows.stream().mapToDouble(GridRow::getHeight).sum();
        Pane pane = new Pane();
        pane.setPrefSize(ROOM_COL_W, totalH);

        double y = 0;
        for (GridRow row : rows) {
            if (row.isFloorHeader) {
                Rectangle bg = new Rectangle(0, y, ROOM_COL_W, FLOOR_H);
                bg.setFill(Color.web("#374151"));
                Label lbl = new Label(row.floorLabel);
                lbl.setStyle("-fx-text-fill: white; -fx-font-size: 10px; -fx-font-weight: bold; -fx-letter-spacing: 1;");
                lbl.setLayoutX(10);
                lbl.setLayoutY(y + (FLOOR_H - 14) / 2.0);
                pane.getChildren().addAll(bg, lbl);
            } else {
                HabRack h = row.habitacion;
                boolean fueraServicio = h.getEstado() == EstadoHabRack.FUERA_SERVICIO;
                boolean bloqueada     = h.getEstado() == EstadoHabRack.BLOQUEADA;

                String bgColor = fueraServicio ? "#FEF2F2"
                        : bloqueada     ? "#FFF7ED"
                        : "#FFFFFF";

                Rectangle bg = new Rectangle(0, y, ROOM_COL_W, ROW_H);
                bg.setFill(Color.web(bgColor));
                pane.getChildren().add(bg);

                // Número de habitación
                Label numLbl = new Label(String.valueOf(h.getNumero()));
                numLbl.setStyle("-fx-font-size: 13px; -fx-font-weight: bold; -fx-text-fill: #1A2E2B;");
                numLbl.setLayoutX(10);
                numLbl.setLayoutY(y + 4);

                // Tipo
                Label tipoLbl = new Label(h.getTipo().getNombre());
                tipoLbl.setStyle("-fx-font-size: 10px; -fx-text-fill: #6B7280;");
                tipoLbl.setLayoutX(10);
                tipoLbl.setLayoutY(y + 21);

                // Indicador de estado
                Region dot = new Region();
                dot.setPrefSize(8, 8);
                dot.setMaxSize(8, 8);
                String dotColor = switch (h.getEstado()) {
                    case LIBRE          -> "#16A34A";
                    case OCUPADA        -> "#2A9D8F";
                    case BLOQUEADA      -> "#F59E0B";
                    case FUERA_SERVICIO -> "#DC2626";
                };
                dot.setStyle("-fx-background-color: " + dotColor + "; -fx-background-radius: 4;");
                dot.setLayoutX(ROOM_COL_W - 16);
                dot.setLayoutY(y + (ROW_H - 8) / 2.0);

                pane.getChildren().addAll(numLbl, tipoLbl, dot);

                // separator
                Line line = new Line(0, y + ROW_H, ROOM_COL_W, y + ROW_H);
                line.setStroke(Color.web("#E5E7EB"));
                line.setStrokeWidth(0.5);
                pane.getChildren().add(line);

                // right border
                Line rightLine = new Line(ROOM_COL_W - 1, y, ROOM_COL_W - 1, y + ROW_H);
                rightLine.setStroke(Color.web("#CBD5E1"));
                rightLine.setStrokeWidth(1);
                pane.getChildren().add(rightLine);
            }
            y += row.getHeight();
        }
        return pane;
    }

    // ── Content grid ─────────────────────────────────────────────────────────

    private Pane buildContentPane(List<GridRow> rows, LocalDate firstDay, int numDays,
                                  double totalW, double totalH,
                                  Map<Integer, Double> roomY, Set<String> sobres) {
        Pane pane = new Pane();
        pane.setPrefSize(totalW, totalH);

        LocalDate today = LocalDate.now();
        boolean todayInMonth = today.getYear() == firstDay.getYear() &&
                today.getMonthValue() == firstDay.getMonthValue();

        // ── Weekend columns ───────────────────────────────────────────────
        for (int d = 0; d < numDays; d++) {
            LocalDate day = firstDay.plusDays(d);
            if (day.getDayOfWeek() == DayOfWeek.SATURDAY ||
                    day.getDayOfWeek() == DayOfWeek.SUNDAY) {
                Rectangle r = new Rectangle(d * DAY_COL_W, 0, DAY_COL_W, totalH);
                r.setFill(Color.web("#F8FAFC"));
                pane.getChildren().add(r);
            }
        }

        // ── Today column ─────────────────────────────────────────────────
        if (todayInMonth) {
            int idx = today.getDayOfMonth() - 1;
            Rectangle r = new Rectangle(idx * DAY_COL_W, 0, DAY_COL_W, totalH);
            r.setFill(Color.web("#ECFDF5", 0.7));
            pane.getChildren().add(r);
        }

        // ── Row backgrounds ──────────────────────────────────────────────
        double y = 0;
        int rowIdx = 0;
        for (GridRow row : rows) {
            if (row.isFloorHeader) {
                Rectangle r = new Rectangle(0, y, totalW, FLOOR_H);
                r.setFill(Color.web("#4B5563", 0.15));
                pane.getChildren().add(r);
            } else {
                if (row.habitacion != null) {
                    EstadoHabRack est = row.habitacion.getEstado();
                    Color bg = switch (est) {
                        case FUERA_SERVICIO -> Color.web("#FEF2F2", 0.4);
                        case BLOQUEADA      -> Color.web("#FFF7ED", 0.4);
                        default             -> (rowIdx % 2 == 0) ? Color.WHITE : Color.web("#F9FAFB");
                    };
                    Rectangle r = new Rectangle(0, y, totalW, ROW_H);
                    r.setFill(bg);
                    pane.getChildren().add(r);
                    rowIdx++;
                }
            }
            y += row.getHeight();
        }

        // ── Vertical day lines ────────────────────────────────────────────
        for (int d = 0; d <= numDays; d++) {
            double x = d * DAY_COL_W;
            Line line = new Line(x, 0, x, totalH);
            boolean isWeekChange = (d < numDays) && (firstDay.plusDays(d).getDayOfWeek() == DayOfWeek.MONDAY);
            line.setStroke(isWeekChange ? Color.web("#D1D5DB") : Color.web("#E5E7EB"));
            line.setStrokeWidth(isWeekChange ? 1.0 : 0.5);
            pane.getChildren().add(line);
        }

        // ── Horizontal row lines ──────────────────────────────────────────
        y = 0;
        for (GridRow row : rows) {
            Line line = new Line(0, y, totalW, y);
            line.setStroke(Color.web("#E5E7EB"));
            line.setStrokeWidth(0.5);
            pane.getChildren().add(line);
            y += row.getHeight();
        }

        // ── Today line (vertical marker) ──────────────────────────────────
        if (todayInMonth) {
            double x = (today.getDayOfMonth() - 1) * DAY_COL_W + DAY_COL_W / 2.0;
            Line todayLine = new Line(x, 0, x, totalH);
            todayLine.setStroke(Color.web("#2A9D8F"));
            todayLine.setStrokeWidth(1.5);
            todayLine.getStrokeDashArray().addAll(4.0, 3.0);
            pane.getChildren().add(todayLine);
        }

        // ── Reserva blocks ────────────────────────────────────────────
        addReservationBlocks(pane, rows, firstDay, numDays, roomY, sobres);

        // ── Context menu for blocking ─────────────────────────────────────
        setupContextMenu(pane, rows, firstDay, numDays, roomY);

        return pane;
    }

    private void addReservationBlocks(Pane pane, List<GridRow> rows, LocalDate firstDay,
                                      int numDays, Map<Integer, Double> roomY,
                                      Set<String> sobres) {
        LocalDate endOfMonth = firstDay.plusDays(numDays);

        // Group reservations by room
        Map<Integer, List<ResRack>> byRoom = new HashMap<>();
        for (ResRack r : reservas) {
            byRoom.computeIfAbsent(r.getNumeroHabitacion(), k -> new ArrayList<>()).add(r);
        }

        for (Map.Entry<Integer, List<ResRack>> entry : byRoom.entrySet()) {
            int roomNum = entry.getKey();
            Double yPos = roomY.get(roomNum);
            if (yPos == null) continue;

            List<ResRack> roomReservas = entry.getValue();
            // Sort by checkin to detect overlaps and stack them
            roomReservas.sort(Comparator.comparing(ResRack::getCheckIn));

            // Track stacking level (for overbooking)
            for (int i = 0; i < roomReservas.size(); i++) {
                ResRack r = roomReservas.get(i);
                boolean isOverbooking = sobres.contains(r.getId());

                // Clamp to visible month range
                LocalDate start = r.getCheckIn().isBefore(firstDay) ? firstDay : r.getCheckIn();
                LocalDate end   = r.getCheckOut().isAfter(endOfMonth) ? endOfMonth : r.getCheckOut();
                if (!start.isBefore(end)) continue;

                int startDay = (int) (start.toEpochDay() - firstDay.toEpochDay());
                int endDay   = (int) (end.toEpochDay()   - firstDay.toEpochDay());

                double x = startDay * DAY_COL_W + 1;
                double w = (endDay - startDay) * DAY_COL_W - 2;

                // Stack overbooking blocks
                boolean stacked = false;
                if (isOverbooking) {
                    for (int j = 0; j < i; j++) {
                        ResRack prev = roomReservas.get(j);
                        if (overlaps(r, prev)) { stacked = true; break; }
                    }
                }

                double blockY  = stacked ? yPos + ROW_H / 2.0 + 1 : yPos + 3;
                double blockH  = stacked ? ROW_H / 2.0 - 4 : ROW_H - 6;

                StackPane block = createReservationBlock(r, w, blockH, isOverbooking);
                block.setLayoutX(x);
                block.setLayoutY(blockY);

                // Setup drag
                final LocalDate fDay = firstDay;
                final int nDays = numDays;
                final Map<Integer, Double> rY = roomY;
                final List<GridRow> gRows = rows;
                setupDragAndDrop(pane, block, r, blockH, fDay, nDays, rY, gRows);

                pane.getChildren().add(block);
            }
        }
    }

    private StackPane createReservationBlock(ResRack r, double w, double h, boolean isOverbooking) {
        String color = isOverbooking ? "#7C3AED" : switch (r.getEstado()) {
            case CONFIRMADA         -> "#2A9D8F";
            case PROVISIONAL        -> "#F59E0B";
            case CHECKIN_REALIZADO  -> "#16A34A";
            case CHECKOUT_REALIZADO -> "#9CA3AF";
            case BLOQUEADA          -> "#DC2626";
            case SOBRERESERVA       -> "#7C3AED";
        };

        Rectangle bg = new Rectangle(w, h);
        bg.setFill(Color.web(color, 0.85));
        bg.setArcWidth(6);
        bg.setArcHeight(6);

        // Left accent
        Rectangle accent = new Rectangle(3, h);
        accent.setFill(Color.web(color));
        accent.setArcWidth(3);
        accent.setArcHeight(3);

        String guest = r.getHuesped();
        if (guest.length() > 18) guest = guest.substring(0, 16) + "…";

        Label lbl = new Label(guest);
        lbl.setStyle("-fx-text-fill: white; -fx-font-size: 10px; -fx-font-weight: bold; -fx-padding: 0 4 0 6;");
        lbl.setMaxWidth(w - 8);
        lbl.setWrapText(false);

        StackPane sp = new StackPane(bg, accent, lbl);
        sp.setPrefSize(w, h);
        sp.setMaxSize(w, h);
        StackPane.setAlignment(accent, Pos.CENTER_LEFT);
        StackPane.setAlignment(lbl, Pos.CENTER_LEFT);

        // Tooltip
        Tooltip tip = new Tooltip(
                r.getHuesped() + "\n" +
                        "Entrada: " + r.getCheckIn().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")) + "\n" +
                        "Salida:  " + r.getCheckOut().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")) + "\n" +
                        "Noches: " + r.getNoches() + "\n" +
                        "Estado: " + estadoLabel(r.getEstado()) +
                        (isOverbooking ? "\n⚠ SOBRERESERVA" : "")
        );
        tip.setStyle("-fx-font-size: 11px;");
        Tooltip.install(sp, tip);

        sp.setStyle("-fx-cursor: hand;");

        // Click en bloque BLOQUEADA → abre Mantenimiento filtrado por esa habitación
        if (r.getEstado() == EstadoResRack.BLOQUEADA) {
            sp.setOnMouseClicked(e -> {
                if (indexController != null) {
                    indexController.abrirMantenimientoConNumeroHabitacion(
                            r.getNumeroHabitacion(),
                            "Hab. " + r.getNumeroHabitacion()
                    );
                }
            });
        }

        return sp;
    }

    // ── Drag & drop ──────────────────────────────────────────────────────────

    private void setupDragAndDrop(Pane contentPane, StackPane block, ResRack reserva,
                                  double blockH, LocalDate firstDay, int numDays,
                                  Map<Integer, Double> roomY, List<GridRow> gridRows) {
        // Don't allow dragging blocked reservations
        if (reserva.getEstado() == EstadoResRack.BLOQUEADA) return;

        long duracion = reserva.getNoches();

        block.setOnMousePressed(e -> {
            dragStartX  = e.getSceneX();
            dragStartY  = e.getSceneY();
            blockOrigX  = block.getLayoutX();
            blockOrigY  = block.getLayoutY();
            draggedReserva = reserva;
            dragFirstDay   = firstDay;
            dragGridRows   = gridRows;
            dragRoomY      = roomY;
            dragNumDays    = numDays;
            block.toFront();
            block.setOpacity(0.75);
            e.consume();
        });

        block.setOnMouseDragged(e -> {
            double dx = e.getSceneX() - dragStartX;
            double dy = e.getSceneY() - dragStartY;
            block.setLayoutX(blockOrigX + dx);
            block.setLayoutY(blockOrigY + dy);
            e.consume();
        });

        block.setOnMouseReleased(e -> {
            block.setOpacity(1.0);

            double dx = e.getSceneX() - dragStartX;
            double dy = e.getSceneY() - dragStartY;

            // Click sin arrastrar → mostrar detalle de reserva
            if (Math.abs(dx) < 6 && Math.abs(dy) < 6) {
                block.setLayoutX(blockOrigX);
                block.setLayoutY(blockOrigY);
                mostrarDetalleReserva(reserva);
                e.consume();
                return;
            }

            // Day shift from MOUSE DELTA — not from block.getLayoutX().
            // This is critical for cross-month reservations whose block is clamped
            // to X=0 (firstDay): using getLayoutX() would compute a new checkIn
            // relative to firstDay even on a purely vertical drag.
            int dayShift = (int) Math.round(dx / DAY_COL_W);

            // Find nearest room from block Y center
            double blockCenterY = block.getLayoutY() + blockH / 2.0;
            int newRoomNum = getNearestRoom(blockCenterY, gridRows, roomY);

            boolean datesChanged = dayShift != 0;
            boolean roomChanged  = newRoomNum != -1 && newRoomNum != reserva.getNumeroHabitacion();

            if (datesChanged || roomChanged) {
                LocalDate newCheckIn  = reserva.getCheckIn().plusDays(dayShift);
                LocalDate newCheckOut = reserva.getCheckOut().plusDays(dayShift);
                int finalRoom = roomChanged ? newRoomNum : reserva.getNumeroHabitacion();

                // Persist to DB (only for real DB reservations, not synthetic blocks)
                String resId = reserva.getId();
                if (!resId.startsWith("BLQ-") && !resId.startsWith("R0")) {
                    try {
                        ReservaDAO.moverReserva(resId, finalRoom, newCheckIn, newCheckOut);
                    } catch (SQLException ex) {
                        ex.printStackTrace();
                    }
                }

                ResRack updated = new ResRack(reserva.getId(), finalRoom, reserva.getHuesped(),
                        newCheckIn, newCheckOut, reserva.getEstado());
                int idx = reservas.indexOf(reserva);
                if (idx >= 0) reservas.set(idx, updated);
                buildRoomRack();
            } else {
                block.setLayoutX(blockOrigX);
                block.setLayoutY(blockOrigY);
            }
            e.consume();
        });
    }

    private int getNearestRoom(double centerY, List<GridRow> gridRows, Map<Integer, Double> roomY) {
        int    best    = -1;
        double minDist = Double.MAX_VALUE;
        for (Map.Entry<Integer, Double> entry : roomY.entrySet()) {
            double rowCenter = entry.getValue() + ROW_H / 2.0;
            double dist = Math.abs(centerY - rowCenter);
            if (dist < minDist) {
                minDist = dist;
                best    = entry.getKey();
            }
        }
        return best;
    }

    // ── Context menu (bloquear) ───────────────────────────────────────────────

    private void setupContextMenu(Pane contentPane, List<GridRow> gridRows,
                                  LocalDate firstDay, int numDays,
                                  Map<Integer, Double> roomY) {
        // Close any open context menu when clicking the content pane
        contentPane.addEventFilter(javafx.scene.input.MouseEvent.MOUSE_PRESSED, e -> {
            if (activeContextMenu != null && activeContextMenu.isShowing()) {
                activeContextMenu.hide();
                activeContextMenu = null;
            }
        });

        contentPane.setOnContextMenuRequested(e -> {
            // Close previous menu before opening a new one
            if (activeContextMenu != null && activeContextMenu.isShowing()) {
                activeContextMenu.hide();
            }

            int dayIdx  = (int) (e.getX() / DAY_COL_W);
            int roomNum = getNearestRoom(e.getY(), gridRows, roomY);
            if (roomNum == -1 || dayIdx < 0 || dayIdx >= numDays) return;

            LocalDate fecha = firstDay.plusDays(dayIdx);

            ContextMenu menu = new ContextMenu();

            MenuItem bloquear = new MenuItem("🔒  Bloquear habitación " + roomNum);
            bloquear.setOnAction(ev -> {
                String id = "BLQ-" + roomNum + "-" + fecha;
                if (reservas.stream().noneMatch(r -> r.getId().equals(id))) {
                    reservas.add(new ResRack(id, roomNum, "Bloqueado",
                            fecha, fecha.plusDays(1), EstadoResRack.BLOQUEADA));
                    buildRoomRack();
                }
            });

            MenuItem desbloquear = new MenuItem("🔓  Desbloquear habitación " + roomNum);
            desbloquear.setOnAction(ev -> {
                reservas.removeIf(r ->
                        r.getNumeroHabitacion() == roomNum &&
                                r.getEstado() == EstadoResRack.BLOQUEADA &&
                                !r.getCheckIn().isAfter(fecha) &&
                                !r.getCheckOut().isBefore(fecha));
                buildRoomRack();
            });

            menu.getItems().addAll(bloquear, desbloquear);
            activeContextMenu = menu;
            menu.show(contentPane, e.getScreenX(), e.getScreenY());
            e.consume();
        });
    }

    // ── Overbooking detection ─────────────────────────────────────────────────

    private Set<String> detectarSobrereservas() {
        Set<String> ids = new HashSet<>();
        for (int i = 0; i < reservas.size(); i++) {
            for (int j = i + 1; j < reservas.size(); j++) {
                ResRack a = reservas.get(i);
                ResRack b = reservas.get(j);
                if (a.getNumeroHabitacion() == b.getNumeroHabitacion() &&
                        a.getEstado() != EstadoResRack.BLOQUEADA &&
                        b.getEstado() != EstadoResRack.BLOQUEADA &&
                        overlaps(a, b)) {
                    ids.add(a.getId());
                    ids.add(b.getId());
                }
            }
        }
        return ids;
    }

    private boolean overlaps(ResRack a, ResRack b) {
        return a.getCheckIn().isBefore(b.getCheckOut()) &&
                b.getCheckIn().isBefore(a.getCheckOut());
    }

    // ── Navigation ────────────────────────────────────────────────────────────

    @FXML private void irMesSiguiente() { mesActual = mesActual.plusMonths(1);  buildRoomRack(); }
    @FXML private void irMesAnterior()  { mesActual = mesActual.minusMonths(1); buildRoomRack(); }
    @FXML private void irHoy()          { mesActual = YearMonth.now();          buildRoomRack(); }
    @FXML private void aplicarFiltros() { buildRoomRack(); }

    @FXML
    private void nuevaReserva() {
        Alert dlg = new Alert(Alert.AlertType.INFORMATION);
        dlg.setTitle("Nueva Reserva");
        dlg.setHeaderText("Módulo Nueva Reserva");
        dlg.setContentText("El formulario de creación de reservas se implementará en el módulo de Recepción.");
        dlg.showAndWait();
    }

    @FXML
    private void accionBloquear() {
        Alert dlg = new Alert(Alert.AlertType.INFORMATION);
        dlg.setTitle("Bloquear habitación");
        dlg.setHeaderText("Bloqueo manual");
        dlg.setContentText("Haz clic derecho sobre una celda del calendario para bloquear o desbloquear una habitación.");
        dlg.showAndWait();
    }

    // ── Detalle de reserva ───────────────────────────────────────────────────

    private void mostrarDetalleReserva(ResRack rv) {
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd/MM/yyyy");

        Dialog<ButtonType> dlg = new Dialog<>();
        dlg.setTitle("Reserva – " + rv.getId());
        dlg.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);
        dlg.getDialogPane().setStyle("-fx-font-family: 'Segoe UI'; -fx-min-width: 460;");

        VBox root = new VBox(10);
        root.setPadding(new Insets(8, 12, 4, 12));

        // ── Header ──────────────────────────────────────────────────────
        Label idLbl = new Label(rv.getId());
        idLbl.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #1A2E2B;");
        String scBg = switch (rv.getEstado()) {
            case CONFIRMADA        -> "#2A9D8F";
            case PROVISIONAL       -> "#F59E0B";
            case CHECKIN_REALIZADO -> "#16A34A";
            default                -> "#9CA3AF";
        };
        Label stLbl = new Label(estadoLabel(rv.getEstado()));
        stLbl.setStyle("-fx-background-color: " + scBg + "; -fx-text-fill: white; " +
                "-fx-padding: 2 10 2 10; -fx-background-radius: 10; -fx-font-size: 11px;");
        HBox hdr = new HBox(10, idLbl, stLbl);
        hdr.setAlignment(Pos.CENTER_LEFT);
        root.getChildren().add(hdr);

        // ── Info básica ─────────────────────────────────────────────────
        String guestStr = rv.getHuesped();
        String roomStr  = String.valueOf(rv.getNumeroHabitacion());
        String datesStr = rv.getCheckIn().format(fmt) + "  →  " + rv.getCheckOut().format(fmt)
                + "  (" + rv.getNoches() + " noche" + (rv.getNoches() == 1 ? "" : "s") + ")";

        // Enriquecer con datos de BD
        Factura inv = null;
        List<LineaFactura> lines = List.of();
        try {
            Reserva full = ReservaDAO.buscarPorNumero(rv.getId());
            if (full != null) {
                if (full.getHuesped() != null)
                    guestStr = full.getHuesped().getApellidos() + ", " + full.getHuesped().getNombre();
                if (full.getHabitacion() != null)
                    roomStr = full.getHabitacion().getNumero();
                inv   = FacturaDAO.getPorReserva(full.getId());
                lines = FacturaDAO.getLineasPorReserva(full.getId());
            }
        } catch (Exception e) { e.printStackTrace(); }

        root.getChildren().addAll(
                detalleRow("Huésped",    guestStr),
                detalleRow("Estancia",   datesStr),
                detalleRow("Habitación", roomStr)
        );

        // ── Facturación ─────────────────────────────────────────────────
        if (inv != null) {
            root.getChildren().add(new Separator());
            Label facHdr = seccionTitulo("Facturación");
            root.getChildren().add(facHdr);
            root.getChildren().addAll(
                    detalleRow("Total",     formatEurBD(inv.getImporteTotal())),
                    detalleRow("Pagado",    formatEurBD(inv.getImportePagado())),
                    detalleRow("Pendiente", formatEurBD(inv.getImportePendiente()))
            );
        }

        // ── Extras ──────────────────────────────────────────────────────
        root.getChildren().add(new Separator());
        root.getChildren().add(seccionTitulo("Extras consumidos"));

        List<LineaFactura> extras = lines.stream()
                .filter(il -> !il.getDescripcion().toLowerCase().startsWith("alojamiento"))
                .collect(Collectors.toList());

        if (extras.isEmpty()) {
            Label vacioLbl = new Label("Sin extras registrados");
            vacioLbl.setStyle("-fx-text-fill: #9CA3AF; -fx-font-style: italic; -fx-font-size: 12px;");
            root.getChildren().add(vacioLbl);
        } else {
            VBox extBox = new VBox(4);
            for (LineaFactura il : extras) extBox.getChildren().add(extraRow(il));
            root.getChildren().add(extBox);
        }

        dlg.getDialogPane().setContent(root);
        dlg.getDialogPane().setPrefWidth(480);
        dlg.showAndWait();
    }

    private HBox detalleRow(String label, String value) {
        Label lbl = new Label(label + ":");
        lbl.setStyle("-fx-text-fill: #6B7280; -fx-font-size: 12px; -fx-min-width: 90;");
        Label val = new Label(value);
        val.setStyle("-fx-text-fill: #1A2E2B; -fx-font-size: 12px; -fx-font-weight: bold;");
        val.setWrapText(true);
        HBox row = new HBox(8, lbl, val);
        row.setAlignment(Pos.CENTER_LEFT);
        return row;
    }

    private Label seccionTitulo(String text) {
        Label lbl = new Label(text.toUpperCase());
        lbl.setStyle("-fx-font-size: 10px; -fx-font-weight: bold; -fx-text-fill: #9CA3AF;");
        return lbl;
    }

    private HBox extraRow(LineaFactura il) {
        String cat = categoriaExtras(il.getDescripcion());
        String color = switch (cat) {
            case "Minibar"     -> "#7C3AED";
            case "Restaurante" -> "#EA580C";
            case "Parking"     -> "#2563EB";
            case "Spa"         -> "#0D9488";
            default            -> "#6B7280";
        };
        Label catLbl  = new Label(cat);
        catLbl.setStyle("-fx-text-fill: " + color + "; -fx-font-size: 11px; -fx-font-weight: bold; -fx-min-width: 86;");

        Label descLbl = new Label(il.getDescripcion());
        descLbl.setStyle("-fx-text-fill: #374151; -fx-font-size: 12px;");
        HBox.setHgrow(descLbl, Priority.ALWAYS);

        BigDecimal qty = il.getCantidad() != null ? il.getCantidad() : BigDecimal.ONE;
        String qtyStr  = qty.stripTrailingZeros().toPlainString();
        Label qtyLbl   = new Label(qtyStr + " × " + formatEurBD(il.getPrecioUnitario()));
        qtyLbl.setStyle("-fx-text-fill: #9CA3AF; -fx-font-size: 11px; -fx-min-width: 110;");

        Label totLbl = new Label(formatEurBD(il.getPrecioTotal()));
        totLbl.setStyle("-fx-text-fill: #1A2E2B; -fx-font-size: 12px; -fx-font-weight: bold; -fx-min-width: 65; -fx-alignment: CENTER_RIGHT;");

        HBox row = new HBox(6, catLbl, descLbl, qtyLbl, totLbl);
        row.setAlignment(Pos.CENTER_LEFT);
        row.setStyle("-fx-background-color: #F9FAFB; -fx-padding: 5 8 5 8; -fx-background-radius: 4;");
        return row;
    }

    private String categoriaExtras(String desc) {
        String d = desc.toLowerCase();
        if (d.contains("minibar"))                                                 return "Minibar";
        if (d.contains("restaur") || d.contains("desayuno") || d.contains("cena")
                || d.contains("almuerzo") || d.contains("room service"))           return "Restaurante";
        if (d.contains("parking") || d.contains("garaje"))                        return "Parking";
        if (d.contains("spa") || d.contains("masaje") || d.contains("tratamiento"))return "Spa";
        return "Otros";
    }

    private String formatEurBD(BigDecimal v) {
        if (v == null) v = BigDecimal.ZERO;
        return String.format(Locale.ROOT, "%.2f €", v.doubleValue()).replace(".", ",");
    }

    @FXML
    private void volverInicio() {
        if (indexController != null) indexController.mostrarInicio();
    }

    public void setIndexController(IndexController controller) {
        this.indexController = controller;
    }

    /**
     * Desplaza el rack hasta la habitación indicada y la destaca con una animación
     * de parpadeo ámbar de 2 segundos (llamado desde Panel Central → overbooking).
     */
    public void destacarHabitacion(int roomNumber) {
        if (lastRoomY == null || mainScroll == null) return;
        Double roomYPos = lastRoomY.get(roomNumber);
        if (roomYPos == null) return;

        // Pequeño retardo para que el viewport esté calculado
        Timeline setup = new Timeline(new KeyFrame(Duration.millis(150), ev -> {
            // Scroll vertical hasta la fila de la habitación
            double vh = mainScroll.getViewportBounds().getHeight();
            if (lastTotalH > vh) {
                double pos = (roomYPos - vh / 2.0) / (lastTotalH - vh);
                mainScroll.setVvalue(Math.max(0, Math.min(1, pos)));
            }

            // Flash ámbar en la columna de habitaciones (siempre visible)
            Rectangle flashLeft = new Rectangle(0, roomYPos, ROOM_COL_W, ROW_H);
            flashLeft.setFill(Color.web("#F59E0B", 0.55));
            flashLeft.setOpacity(0);
            roomsContent.getChildren().add(flashLeft);

            // Flash ámbar en el panel de contenido (zona del calendario)
            Rectangle flashContent = null;
            if (lastContentPane != null) {
                flashContent = new Rectangle(0, roomYPos,
                        Math.max(5000, lastContentPane.getPrefWidth()), ROW_H);
                flashContent.setFill(Color.web("#F59E0B", 0.35));
                flashContent.setOpacity(0);
                lastContentPane.getChildren().add(flashContent);
            }
            final Rectangle fc = flashContent;

            // Animación doble-parpadeo: 0→1 en 250ms, mantiene, baja, sube, mantiene, baja
            Timeline tl = new Timeline(
                    new KeyFrame(Duration.ZERO,
                            new KeyValue(flashLeft.opacityProperty(), 0.0)),
                    new KeyFrame(Duration.millis(250),
                            new KeyValue(flashLeft.opacityProperty(), 1.0)),
                    new KeyFrame(Duration.millis(800),
                            new KeyValue(flashLeft.opacityProperty(), 1.0)),
                    new KeyFrame(Duration.millis(1000),
                            new KeyValue(flashLeft.opacityProperty(), 0.0)),
                    new KeyFrame(Duration.millis(1200),
                            new KeyValue(flashLeft.opacityProperty(), 1.0)),
                    new KeyFrame(Duration.millis(1750),
                            new KeyValue(flashLeft.opacityProperty(), 1.0)),
                    new KeyFrame(Duration.millis(2000),
                            new KeyValue(flashLeft.opacityProperty(), 0.0))
            );
            if (fc != null) {
                tl.getKeyFrames().addAll(
                        new KeyFrame(Duration.ZERO,
                                new KeyValue(fc.opacityProperty(), 0.0)),
                        new KeyFrame(Duration.millis(250),
                                new KeyValue(fc.opacityProperty(), 1.0)),
                        new KeyFrame(Duration.millis(800),
                                new KeyValue(fc.opacityProperty(), 1.0)),
                        new KeyFrame(Duration.millis(1000),
                                new KeyValue(fc.opacityProperty(), 0.0)),
                        new KeyFrame(Duration.millis(1200),
                                new KeyValue(fc.opacityProperty(), 1.0)),
                        new KeyFrame(Duration.millis(1750),
                                new KeyValue(fc.opacityProperty(), 1.0)),
                        new KeyFrame(Duration.millis(2000),
                                new KeyValue(fc.opacityProperty(), 0.0))
                );
            }
            tl.setOnFinished(e -> {
                roomsContent.getChildren().remove(flashLeft);
                if (fc != null && lastContentPane != null)
                    lastContentPane.getChildren().remove(fc);
            });
            tl.play();
        }));
        setup.play();
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private String estadoLabel(EstadoResRack e) {
        return switch (e) {
            case CONFIRMADA         -> "Confirmada";
            case PROVISIONAL        -> "Provisional";
            case CHECKIN_REALIZADO  -> "Check-in realizado";
            case CHECKOUT_REALIZADO -> "Check-out";
            case BLOQUEADA          -> "Bloqueada";
            case SOBRERESERVA       -> "Sobrereserva";
        };
    }

    // ── Inner types: Planning-specific rack types ─────────────────────────────

    enum TipoRack {
        SGL_ECONOMY("SGL Eco"), SGL_STANDARD("SGL Std"), DBL_STANDARD("DBL Std"),
        DBL_VISTA_MAR("DBL Mar"), DBL_SUPERIOR("DBL Sup"),
        SUITE_JUNIOR("Suite Jr"), SUITE_PREMIUM("Suite Pre");

        private final String nombre;
        TipoRack(String nombre) { this.nombre = nombre; }
        public String getNombre() { return nombre; }
    }

    enum EstadoHabRack { LIBRE, OCUPADA, BLOQUEADA, FUERA_SERVICIO }

    enum EstadoResRack {
        CONFIRMADA, PROVISIONAL, CHECKIN_REALIZADO, CHECKOUT_REALIZADO, BLOQUEADA, SOBRERESERVA
    }

    private static class HabRack {
        private final int            numero;
        private final TipoRack       tipo;
        private final int            planta;
        private final EstadoHabRack  estado;

        HabRack(int numero, TipoRack tipo, int planta, EstadoHabRack estado) {
            this.numero = numero;
            this.tipo   = tipo;
            this.planta = planta;
            this.estado = estado;
        }

        public int           getNumero() { return numero; }
        public TipoRack      getTipo()   { return tipo;   }
        public int           getPlanta() { return planta; }
        public EstadoHabRack getEstado() { return estado; }
    }

    private static class ResRack {
        private final String         id;
        private final int            numeroHabitacion;
        private final String         huesped;
        private final LocalDate      checkIn;
        private final LocalDate      checkOut;
        private final EstadoResRack  estado;

        ResRack(String id, int numeroHabitacion, String huesped,
                LocalDate checkIn, LocalDate checkOut, EstadoResRack estado) {
            this.id               = id;
            this.numeroHabitacion = numeroHabitacion;
            this.huesped          = huesped;
            this.checkIn          = checkIn;
            this.checkOut         = checkOut;
            this.estado           = estado;
        }

        public String        getId()               { return id;               }
        public int           getNumeroHabitacion() { return numeroHabitacion; }
        public String        getHuesped()          { return huesped;          }
        public LocalDate     getCheckIn()          { return checkIn;          }
        public LocalDate     getCheckOut()         { return checkOut;         }
        public EstadoResRack getEstado()           { return estado;           }
        public long          getNoches() {
            return java.time.temporal.ChronoUnit.DAYS.between(checkIn, checkOut);
        }
    }

    // ── Inner class: GridRow ──────────────────────────────────────────────────

    private static class GridRow {
        final boolean     isFloorHeader;
        final String      floorLabel;
        final HabRack habitacion;

        GridRow(String floorLabel) {
            this.isFloorHeader = true;
            this.floorLabel    = floorLabel;
            this.habitacion    = null;
        }

        GridRow(HabRack habitacion) {
            this.isFloorHeader = false;
            this.floorLabel    = null;
            this.habitacion    = habitacion;
        }

        double getHeight() { return isFloorHeader ? FLOOR_H : ROW_H; }
    }
}
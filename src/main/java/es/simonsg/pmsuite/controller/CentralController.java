package es.simonsg.pmsuite.controller;

import es.simonsg.pmsuite.dao.MantenimientoDAO;
import es.simonsg.pmsuite.dao.ReservaDAO;
import es.simonsg.pmsuite.dao.HabitacionDAO;
import es.simonsg.pmsuite.db.GestorBD;
import es.simonsg.pmsuite.model.SolicitudMantenimiento;
import es.simonsg.pmsuite.model.Reserva;
import es.simonsg.pmsuite.model.Habitacion;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.chart.PieChart;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TextInputDialog;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.util.List;
import java.util.Locale;

public class CentralController {

    @FXML private Label updateLabel;
    @FXML private Label fechaLabel;

    // KPI
    @FXML private Label kpiOcupacion;
    @FXML private Label kpiAdr;
    @FXML private Label kpiRevpar;
    @FXML private Label kpiIngresos;
    @FXML private Label kpiOcupacionSub;
    @FXML private Label kpiIngresosPendiente;

    // Estado habitaciones
    @FXML private Label  statOcupadas;
    @FXML private Label  statLibres;
    @FXML private Label  statFuera;
    @FXML private Label  statBloqueadas;
    @FXML private Region barOcupadas;
    @FXML private Region barLibres;
    @FXML private Region barFuera;
    @FXML private Label  barOcupadasLabel;
    @FXML private Label  barLibresLabel;
    @FXML private Label  barFueraLabel;

    // Llegadas / Salidas
    @FXML private Label llegadasSubtitle;
    @FXML private Label llegadasBadge;
    @FXML private VBox  llegadasList;
    @FXML private Label salidasSubtitle;
    @FXML private Label salidasBadge;
    @FXML private VBox  salidasList;

    // Fuera de servicio
    @FXML private VBox  fueraServicioList;
    @FXML private Label fueraServicioTotal;

    // Alertas
    @FXML private Label alertasCriticasBadge;
    @FXML private VBox  alertasList;

    // Overbooking
    @FXML private Label overbookingBadge;
    @FXML private VBox  overbookingContainer;

    // Gráfico
    @FXML private PieChart ocupacionChart;

    private IndexController indexController;

    @FXML
    public void initialize() {
        actualizarFechaHora();
        cargarDatos();
    }

    private void actualizarFechaHora() {
        LocalDateTime ahora = LocalDateTime.now();
        updateLabel.setText("Actualizado: " + ahora.format(DateTimeFormatter.ofPattern("HH:mm")));
        String diaSemana = ahora.getDayOfWeek().getDisplayName(TextStyle.FULL, new Locale("es", "ES"));
        String capital = diaSemana.substring(0, 1).toUpperCase() + diaSemana.substring(1);
        fechaLabel.setText("Resumen general del hotel · " + capital + ", "
                + ahora.getDayOfMonth() + " de "
                + ahora.getMonth().getDisplayName(TextStyle.FULL, new Locale("es", "ES"))
                + " de " + ahora.getYear());
    }

    private void cargarDatos() {
        try {
            List<Habitacion> rooms = HabitacionDAO.obtenerTodas();
            cargarKPIs(rooms);
            cargarEstadoHabitaciones(rooms);
            cargarGrafico(rooms);
            cargarFueraServicio(rooms);
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            cargarLlegadas();
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            cargarSalidas();
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            cargarAlertas();
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            cargarOverbooking();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // KPIs

    private void cargarKPIs(List<Habitacion> rooms) {
        int total    = rooms.size();
        long ocupadas = rooms.stream().filter(r -> r.getEstado() == Habitacion.Estado.OCUPADA).count();
        double pctOc  = total > 0 ? (double) ocupadas / total * 100 : 0;
        double adr    = getADR();
        double revpar = adr * (pctOc / 100);
        BigDecimal ingresos  = getRevenueHoy();
        BigDecimal pendiente = getPendingRevenue();

        kpiOcupacion.setText(String.format("%.0f%%", pctOc));
        kpiAdr.setText(formatEur(adr));
        kpiRevpar.setText(formatEur(revpar));
        kpiIngresos.setText(formatEurBD(ingresos));
        kpiOcupacionSub.setText(ocupadas + " de " + total + " habitaciones");
        kpiIngresosPendiente.setText("Pendiente: " + formatEurBD(pendiente));
    }

    // Estado habitaciones

    private void cargarEstadoHabitaciones(List<Habitacion> rooms) {
        long ocupadas   = rooms.stream().filter(r -> r.getEstado() == Habitacion.Estado.OCUPADA).count();
        long libres     = rooms.stream().filter(r ->
                r.getEstado() == Habitacion.Estado.DISPONIBLE || r.getEstado() == Habitacion.Estado.LIMPIEZA).count();
        long fuera      = rooms.stream().filter(r -> r.getEstado() == Habitacion.Estado.FUERA_SERVICIO).count();
        long bloqueadas = rooms.stream().filter(r -> r.getEstado() == Habitacion.Estado.BLOQUEADA).count();
        int  total      = rooms.size();

        statOcupadas.setText(String.valueOf(ocupadas));
        statLibres.setText(String.valueOf(libres));
        statFuera.setText(String.valueOf(fuera));
        statBloqueadas.setText(String.valueOf(bloqueadas));

        if (total > 0) {
            double pctOc = (double) ocupadas / total * 100;
            double pctLi = (double) libres    / total * 100;
            double pctFu = (double) fuera     / total * 100;
            barLibres.setPrefWidth(Math.max(4, pctLi * 2));
            barFuera.setPrefWidth(Math.max(4, pctFu * 2));
            barOcupadasLabel.setText(String.format("Ocupadas %.0f%%", pctOc));
            barLibresLabel.setText(String.format("Libres %.0f%%", pctLi));
            barFueraLabel.setText(String.format("F. Servicio %.0f%%", pctFu));
        }
    }

    // Gráfico

    private void cargarGrafico(List<Habitacion> rooms) {
        long ocupadas = rooms.stream().filter(r -> r.getEstado() == Habitacion.Estado.OCUPADA).count();
        long libres   = rooms.stream().filter(r ->
                r.getEstado() == Habitacion.Estado.DISPONIBLE || r.getEstado() == Habitacion.Estado.LIMPIEZA).count();
        long fuera    = rooms.stream().filter(r -> r.getEstado() == Habitacion.Estado.FUERA_SERVICIO).count();

        ObservableList<PieChart.Data> data = FXCollections.observableArrayList(
                new PieChart.Data("Ocupadas",    Math.max(1, ocupadas)),
                new PieChart.Data("Libres",      Math.max(1, libres)),
                new PieChart.Data("F. Servicio", Math.max(0, fuera))
        );
        ocupacionChart.setData(data);
        Platform.runLater(() -> {
            if (data.get(0).getNode() != null) data.get(0).getNode().setStyle("-fx-pie-color: #2A9D8F;");
            if (data.get(1).getNode() != null) data.get(1).getNode().setStyle("-fx-pie-color: #DCFCE7;");
            if (data.get(2).getNode() != null) data.get(2).getNode().setStyle("-fx-pie-color: #E74C3C;");
        });
    }

    // Llegadas

    private void cargarLlegadas() throws Exception {
        List<Reserva> llegadas = ReservaDAO.getLlegadasHoy();
        long checkins = llegadas.stream()
                .filter(r -> r.getEstado() == Reserva.Estado.REGISTRADA).count();

        llegadasSubtitle.setText(llegadas.size() + " reserva"
                + (llegadas.size() == 1 ? "" : "s") + " prevista" + (llegadas.size() == 1 ? "" : "s"));
        llegadasBadge.setText(checkins + " realizad" + (checkins == 1 ? "a" : "as"));

        llegadasList.getChildren().clear();
        if (llegadas.isEmpty()) {
            Label empty = new Label("Sin llegadas previstas para hoy");
            empty.getStyleClass().add("card-subtitle");
            llegadasList.getChildren().add(empty);
        } else {
            for (Reserva r : llegadas.subList(0, Math.min(5, llegadas.size()))) {
                llegadasList.getChildren().add(crearItemLlegada(r));
            }
            if (llegadas.size() > 5) {
                Label more = new Label("+ Ver las " + llegadas.size() + " llegadas...");
                more.getStyleClass().add("link-label");
                llegadasList.getChildren().add(more);
            }
        }
    }

    // Salidas

    private void cargarSalidas() throws Exception {
        List<Reserva> salidas = ReservaDAO.getSalidasHoy();
        long checkouts = salidas.stream()
                .filter(r -> r.getEstado() == Reserva.Estado.CHECKOUT_REALIZADO).count();

        salidasSubtitle.setText(salidas.size() + " reserva"
                + (salidas.size() == 1 ? "" : "s") + " prevista" + (salidas.size() == 1 ? "" : "s"));
        salidasBadge.setText(checkouts + " realizad" + (checkouts == 1 ? "a" : "as"));

        salidasList.getChildren().clear();
        if (salidas.isEmpty()) {
            Label empty = new Label("Sin salidas previstas para hoy");
            empty.getStyleClass().add("card-subtitle");
            salidasList.getChildren().add(empty);
        } else {
            for (Reserva r : salidas.subList(0, Math.min(5, salidas.size()))) {
                salidasList.getChildren().add(crearItemSalida(r));
            }
            if (salidas.size() > 5) {
                Label more = new Label("+ Ver las " + salidas.size() + " salidas...");
                more.getStyleClass().add("link-label");
                salidasList.getChildren().add(more);
            }
        }
    }

    // Fuera de servicio

    private void cargarFueraServicio(List<Habitacion> rooms) throws Exception {
        List<SolicitudMantenimiento> mantenimiento = MantenimientoDAO.obtenerActivas();
        fueraServicioList.getChildren().clear();

        for (Habitacion r : rooms) {
            if (r.getEstado() != Habitacion.Estado.FUERA_SERVICIO && r.getEstado() != Habitacion.Estado.BLOQUEADA)
                continue;

            String motivo = mantenimiento.stream()
                    .filter(m -> m.getHabitacion() != null && m.getHabitacion().getId() == r.getId())
                    .map(SolicitudMantenimiento::getTitulo)
                    .findFirst()
                    .orElse(r.getEstado() == Habitacion.Estado.FUERA_SERVICIO ? "Fuera de servicio" : "Bloqueada");

            Label badge = new Label(r.getNumero());
            badge.getStyleClass().add("room-badge-danger");

            Label titulo = new Label(motivo);
            titulo.getStyleClass().add("item-name");
            Label sub = new Label(r.getEstado().toSpanish());
            sub.getStyleClass().add("item-sub");
            VBox info = new VBox(1, titulo, sub);
            HBox.setHgrow(info, Priority.ALWAYS);

            HBox item = new HBox(10, badge, info);
            item.getStyleClass().add("list-item");
            item.setAlignment(Pos.CENTER_LEFT);
            fueraServicioList.getChildren().add(item);
        }

        if (fueraServicioList.getChildren().isEmpty()) {
            Label empty = new Label("Todas las habitaciones operativas");
            empty.getStyleClass().add("card-subtitle");
            fueraServicioList.getChildren().add(empty);
        }

        long fueraCount = rooms.stream().filter(r -> r.getEstado() == Habitacion.Estado.FUERA_SERVICIO).count();
        long bloqCount  = rooms.stream().filter(r -> r.getEstado() == Habitacion.Estado.BLOQUEADA).count();
        fueraServicioTotal.setText("Total: " + fueraCount + " fuera de servicio, " + bloqCount + " bloqueadas");
    }

    // Alertas

    private void cargarAlertas() throws Exception {
        List<SolicitudMantenimiento> activas = MantenimientoDAO.obtenerActivas();
        long urgentes = activas.stream().filter(m ->
                m.getPrioridad() == SolicitudMantenimiento.Prioridad.URGENTE ||
                        m.getPrioridad() == SolicitudMantenimiento.Prioridad.ALTA).count();

        alertasCriticasBadge.setText(urgentes + " crítica" + (urgentes == 1 ? "" : "s"));
        alertasCriticasBadge.setVisible(urgentes > 0);

        alertasList.getChildren().clear();
        if (activas.isEmpty()) {
            Region dot = new Region();
            dot.setPrefSize(8, 8);
            dot.setMaxSize(8, 8);
            dot.getStyleClass().add("dot-green");
            Label lbl = new Label("Sin alertas activas · Todas las habitaciones en buen estado");
            lbl.getStyleClass().add("item-name");
            HBox empty = new HBox(10, dot, lbl);
            empty.setAlignment(Pos.CENTER_LEFT);
            alertasList.getChildren().add(empty);
        } else {
            for (SolicitudMantenimiento m : activas.subList(0, Math.min(4, activas.size()))) {
                String dotColor = switch (m.getPrioridad()) {
                    case URGENTE, ALTA  -> "dot-red";
                    case MEDIA        -> "dot-amber";
                    case BAJA           -> "dot-green";
                };
                alertasList.getChildren().add(crearItemAlerta(m, dotColor));
            }
        }
    }

    // Overbooking

    private void cargarOverbooking() throws Exception {
        String countSql = """
                SELECT COUNT(DISTINCT r1.id)
                FROM reservations r1
                JOIN reservations r2 ON r1.room_id = r2.room_id AND r1.id < r2.id
                WHERE r1.room_id IS NOT NULL
                  AND r1.status IN ('CONFIRMED','CHECKED_IN','PENDING')
                  AND r2.status IN ('CONFIRMED','CHECKED_IN','PENDING')
                  AND r1.check_in_date  < r2.check_out_date
                  AND r2.check_in_date  < r1.check_out_date
                """;
        int count = 0;
        try (Connection conn = GestorBD.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(countSql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) count = rs.getInt(1);
        }

        overbookingBadge.setText(count + " activo" + (count == 1 ? "" : "s"));
        overbookingContainer.getChildren().clear();

        if (count == 0) {
            Label ok = new Label("Sin overbookings activos en los próximos 7 días.");
            ok.getStyleClass().add("card-subtitle");
            overbookingContainer.getChildren().add(ok);
            return;
        }

        String detailSql = """
                SELECT rm.number AS room_num, g.first_name, g.last_name,
                       r2.check_in_date, r2.reservation_number
                FROM reservations r1
                JOIN reservations r2 ON r1.room_id = r2.room_id AND r1.id < r2.id
                JOIN rooms rm ON rm.id = r1.room_id
                JOIN guests g ON g.id = r2.guest_id
                WHERE r1.room_id IS NOT NULL
                  AND r1.status IN ('CONFIRMED','CHECKED_IN','PENDING')
                  AND r2.status IN ('CONFIRMED','CHECKED_IN','PENDING')
                  AND r1.check_in_date  < r2.check_out_date
                  AND r2.check_in_date  < r1.check_out_date
                LIMIT 3
                """;
        try (Connection conn = GestorBD.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(detailSql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                String roomNum   = rs.getString("room_num");
                String guestName = rs.getString("last_name") + ", " + rs.getString("first_name");
                java.time.LocalDate ci = rs.getDate("check_in_date").toLocalDate();

                Label tagLbl  = new Label("SOBRERESERVA");
                tagLbl.getStyleClass().add("tag-danger");

                // Clickable room label → navigates to Planning and highlights the room
                Label roomLbl = new Label("Hab. " + roomNum + " · "
                        + ci.format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy")));
                roomLbl.getStyleClass().add("item-name");
                roomLbl.setStyle("-fx-cursor: hand; -fx-underline: true;");
                final String finalRoomNum = roomNum;
                roomLbl.setOnMouseClicked(e -> {
                    if (indexController != null) {
                        try {
                            int num = Integer.parseInt(finalRoomNum);
                            indexController.abrirPlanningConHabitacion(num);
                        } catch (NumberFormatException ignored) {}
                    }
                });

                HBox header = new HBox(8, tagLbl, roomLbl);
                header.setAlignment(Pos.CENTER_LEFT);

                Label guestLbl = new Label("Conflicto con reserva de: " + guestName);
                guestLbl.getStyleClass().add("item-sub");

                VBox card = new VBox(6, header, guestLbl);
                card.getStyleClass().add("incident-card");
                overbookingContainer.getChildren().add(card);
            }
        }
    }

    // Helpers de UI

    private HBox crearItemLlegada(Reserva r) {
        String roomNum = r.getHabitacion() != null ? r.getHabitacion().getNumero() : "?";
        String nombre  = r.getHuesped() != null
                ? r.getHuesped().getApellidos() + ", " + r.getHuesped().getNombre() : "Desconocido";
        long   noches  = r.getNoches();
        String tipo    = r.getHabitacion() != null && r.getHabitacion().getTipoHabitacion() != null
                ? r.getHabitacion().getTipoHabitacion().getNombre() : "";

        Label badgeRoom = new Label(roomNum);
        badgeRoom.getStyleClass().add("room-badge-teal");

        Label nameLbl = new Label(nombre);  nameLbl.getStyleClass().add("item-name");
        Label subLbl  = new Label(noches + " noche" + (noches == 1 ? "" : "s")
                + (tipo.isEmpty() ? "" : " · " + tipo));
        subLbl.getStyleClass().add("item-sub");
        VBox info = new VBox(1, nameLbl, subLbl);
        HBox.setHgrow(info, Priority.ALWAYS);

        boolean done = r.getEstado() == Reserva.Estado.REGISTRADA;
        Label badge  = new Label(done ? "Check-in" : "Pendiente");
        badge.getStyleClass().add(done ? "badge-success" : "badge-warning");

        HBox item = new HBox(10, badgeRoom, info, badge);
        item.getStyleClass().add("list-item");
        item.setAlignment(Pos.CENTER_LEFT);

        if (!done) {
            ContextMenu menu = new ContextMenu();
            MenuItem checkinItem = new MenuItem("✓  Realizar Check-in");
            checkinItem.setOnAction(e -> realizarCheckin(r));
            menu.getItems().add(checkinItem);
            item.setOnContextMenuRequested(e -> menu.show(item, e.getScreenX(), e.getScreenY()));
        }

        return item;
    }

    private HBox crearItemSalida(Reserva r) {
        String roomNum = r.getHabitacion() != null ? r.getHabitacion().getNumero() : "?";
        String nombre  = r.getHuesped() != null
                ? r.getHuesped().getApellidos() + ", " + r.getHuesped().getNombre() : "Desconocido";
        long   noches  = r.getNoches();
        String tipo    = r.getHabitacion() != null && r.getHabitacion().getTipoHabitacion() != null
                ? r.getHabitacion().getTipoHabitacion().getNombre() : "";

        Label badgeRoom = new Label(roomNum);
        badgeRoom.getStyleClass().add("room-badge-gray");

        Label nameLbl = new Label(nombre);  nameLbl.getStyleClass().add("item-name");
        Label subLbl  = new Label(noches + " noche" + (noches == 1 ? "" : "s")
                + (tipo.isEmpty() ? "" : " · " + tipo));
        subLbl.getStyleClass().add("item-sub");
        VBox info = new VBox(1, nameLbl, subLbl);
        HBox.setHgrow(info, Priority.ALWAYS);

        boolean done = r.getEstado() == Reserva.Estado.CHECKOUT_REALIZADO;
        Label badge  = new Label(done ? "Check-out" : "Pendiente");
        badge.getStyleClass().add(done ? "badge-success" : "badge-warning");

        HBox item = new HBox(10, badgeRoom, info, badge);
        item.getStyleClass().add("list-item");
        item.setAlignment(Pos.CENTER_LEFT);

        if (r.getEstado() == Reserva.Estado.REGISTRADA) {
            ContextMenu menu = new ContextMenu();
            MenuItem checkoutItem = new MenuItem("↩  Realizar Check-out");
            checkoutItem.setOnAction(e -> realizarCheckout(r));
            menu.getItems().add(checkoutItem);
            item.setOnContextMenuRequested(e -> menu.show(item, e.getScreenX(), e.getScreenY()));
        }

        return item;
    }

    private HBox crearItemAlerta(SolicitudMantenimiento m, String dotColor) {
        Region dot = new Region();
        dot.setPrefSize(8, 8);
        dot.setMaxSize(8, 8);
        dot.getStyleClass().add(dotColor);

        Label tituloLbl = new Label(m.getTitulo());
        tituloLbl.getStyleClass().add("item-name");
        String roomInfo = m.getHabitacion() != null ? "Hab. " + m.getHabitacion().getNumero() + " · " : "";
        Label subLbl = new Label(roomInfo + m.getPrioridad().toSpanish());
        subLbl.getStyleClass().add("item-sub");
        VBox info = new VBox(1, tituloLbl, subLbl);
        HBox.setHgrow(info, Priority.ALWAYS);

        Button closeBtn = new Button("×");
        String closeNormal = "-fx-background-color: transparent; -fx-text-fill: #9CA3AF; -fx-font-size: 14px; -fx-cursor: hand; -fx-padding: 0 4 0 4; -fx-min-width: 20;";
        String closeHover  = "-fx-background-color: transparent; -fx-text-fill: #EF4444; -fx-font-size: 14px; -fx-cursor: hand; -fx-padding: 0 4 0 4; -fx-min-width: 20;";
        closeBtn.setStyle(closeNormal);
        closeBtn.setOnMouseEntered(e -> closeBtn.setStyle(closeHover));
        closeBtn.setOnMouseExited(e  -> closeBtn.setStyle(closeNormal));
        closeBtn.setOnAction(e -> {
            try {
                MantenimientoDAO.actualizarEstado(m.getId(), SolicitudMantenimiento.Estado.COMPLETADA);
                cargarAlertas();
            } catch (Exception ex) { ex.printStackTrace(); }
        });

        HBox item = new HBox(10, dot, info, closeBtn);
        item.setAlignment(Pos.CENTER_LEFT);

        // Right-click menu
        ContextMenu menu = new ContextMenu();

        MenuItem completarItem = new MenuItem("✓  Completar alerta");
        completarItem.setOnAction(e -> {
            try {
                MantenimientoDAO.actualizarEstado(m.getId(), SolicitudMantenimiento.Estado.COMPLETADA);
                cargarAlertas();
            } catch (Exception ex) { ex.printStackTrace(); }
        });

        MenuItem cancelarItem = new MenuItem("✕  Cancelar alerta");
        cancelarItem.setOnAction(e -> {
            try {
                MantenimientoDAO.actualizarEstado(m.getId(), SolicitudMantenimiento.Estado.CANCELADA);
                cargarAlertas();
            } catch (Exception ex) { ex.printStackTrace(); }
        });

        menu.getItems().addAll(completarItem, cancelarItem);

        if (m.getHabitacion() != null) {
            MenuItem desbloquearItem = new MenuItem("🔓  Desbloquear hab. " + m.getHabitacion().getNumero());
            desbloquearItem.setOnAction(e -> {
                try {
                    HabitacionDAO.actualizarEstado(m.getHabitacion().getId(), Habitacion.Estado.DISPONIBLE);
                    MantenimientoDAO.actualizarEstado(m.getId(), SolicitudMantenimiento.Estado.COMPLETADA);
                    cargarDatos();
                } catch (Exception ex) { ex.printStackTrace(); }
            });
            menu.getItems().add(desbloquearItem);
        }

        item.setOnContextMenuRequested(e -> menu.show(item, e.getScreenX(), e.getScreenY()));
        return item;
    }

    // Acciones interactivas

    private void realizarCheckin(Reserva r) {
        String nombre  = r.getHuesped() != null
                ? r.getHuesped().getApellidos() + ", " + r.getHuesped().getNombre() : "Desconocido";
        String habNum  = r.getHabitacion() != null ? r.getHabitacion().getNumero() : "?";

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirmar Check-in");
        confirm.setHeaderText("Check-in – Hab. " + habNum);
        confirm.setContentText("¿Realizar check-in de " + nombre + "?");
        confirm.showAndWait().ifPresent(bt -> {
            if (bt == ButtonType.OK) {
                try {
                    ReservaDAO.realizarCheckin(r.getId());
                    cargarDatos();
                } catch (Exception ex) { ex.printStackTrace(); }
            }
        });
    }

    private void realizarCheckout(Reserva r) {
        String nombre  = r.getHuesped() != null
                ? r.getHuesped().getApellidos() + ", " + r.getHuesped().getNombre() : "Desconocido";
        String habNum  = r.getHabitacion() != null ? r.getHabitacion().getNumero() : "?";
        String defImporte = r.getPrecioTotal() != null
                ? r.getPrecioTotal().toPlainString() : "0";

        TextInputDialog dlg = new TextInputDialog(defImporte);
        dlg.setTitle("Confirmar Check-out");
        dlg.setHeaderText("Check-out – " + nombre + " · Hab. " + habNum);
        dlg.setContentText("Importe final (€):");
        dlg.showAndWait().ifPresent(txt -> {
            try {
                BigDecimal amount = new BigDecimal(txt.trim().replace(",", "."));
                ReservaDAO.realizarCheckout(r.getId(), amount);
                cargarDatos();
            } catch (NumberFormatException e) {
                Alert err = new Alert(Alert.AlertType.ERROR);
                err.setHeaderText("Importe no válido");
                err.setContentText("Introduce un número válido, por ejemplo: 245,00");
                err.showAndWait();
            } catch (Exception ex) { ex.printStackTrace(); }
        });
    }

    // Queries financieras
    private double getADR() {
        try (Connection conn = GestorBD.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement("""
                     SELECT COALESCE(AVG(total_price /
                            GREATEST(1, (check_out_date - check_in_date)::int)), 0)
                     FROM reservations
                     WHERE status = 'CHECKED_IN' AND total_price > 0
                     """);
             ResultSet rs = ps.executeQuery()) {
            return rs.next() ? rs.getDouble(1) : 0;
        } catch (Exception e) { return 0; }
    }

    private BigDecimal getRevenueHoy() {
        try (Connection conn = GestorBD.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(
                     "SELECT COALESCE(SUM(amount),0) FROM payments WHERE paid_at::date = CURRENT_DATE");
             ResultSet rs = ps.executeQuery()) {
            return rs.next() ? rs.getBigDecimal(1) : BigDecimal.ZERO;
        } catch (Exception e) { return BigDecimal.ZERO; }
    }

    private BigDecimal getPendingRevenue() {
        try (Connection conn = GestorBD.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(
                     "SELECT COALESCE(SUM(total_amount - paid_amount),0) " +
                             "FROM invoices WHERE status IN ('PENDING','PARTIALLY_PAID')");
             ResultSet rs = ps.executeQuery()) {
            return rs.next() ? rs.getBigDecimal(1) : BigDecimal.ZERO;
        } catch (Exception e) { return BigDecimal.ZERO; }
    }

    // ── Format ────────────────────────────────────────────────────────────────

    private String formatEur(double val) {
        return String.format(Locale.ROOT, "%.2f €", val).replace(".", ",");
    }

    private String formatEurBD(BigDecimal val) {
        if (val == null) val = BigDecimal.ZERO;
        return String.format(Locale.ROOT, "%.2f €", val.doubleValue()).replace(".", ",");
    }

    // ── Navegación ────────────────────────────────────────────────────────────

    public void setIndexController(IndexController controller) {
        this.indexController = controller;
    }

    @FXML
    private void volverInicio() {
        if (indexController != null) indexController.mostrarInicio();
    }
}

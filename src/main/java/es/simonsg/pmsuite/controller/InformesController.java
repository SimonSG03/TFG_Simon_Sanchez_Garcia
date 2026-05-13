package es.simonsg.pmsuite.controller;

import es.simonsg.pmsuite.dao.FacturaDAO;
import es.simonsg.pmsuite.dao.PagoDAO;
import es.simonsg.pmsuite.dao.ReservaDAO;
import es.simonsg.pmsuite.dao.HabitacionDAO;
import es.simonsg.pmsuite.model.Huesped;
import es.simonsg.pmsuite.model.Factura;
import es.simonsg.pmsuite.model.Pago;
import es.simonsg.pmsuite.model.Reserva;
import es.simonsg.pmsuite.model.Habitacion;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;

import java.io.*;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class InformesController {

    private IndexController indexController;

    // ── Navegacion
    @FXML private Button btnNavResumen;
    @FXML private Button btnNavOcupacion;
    @FXML private Button btnNavIngresos;
    @FXML private Button btnNavADR;
    @FXML private Button btnNavHuespedes;
    @FXML private Button btnNavExportar;

    // ── Paneles
    @FXML private VBox pnlResumen;
    @FXML private VBox pnlOcupacion;
    @FXML private VBox pnlIngresos;
    @FXML private VBox pnlADR;
    @FXML private VBox pnlHuespedes;
    @FXML private VBox pnlExportar;

    // ── Panel 1: Resumen
    @FXML private DatePicker dpResDesde;
    @FXML private DatePicker dpResHasta;
    @FXML private Label lblResOcupacion;
    @FXML private Label lblResFacturado;
    @FXML private Label lblResCobrado;
    @FXML private Label lblResNoches;
    @FXML private TableView<Reserva>           tblResumen;
    @FXML private TableColumn<Reserva, String> colResNum;
    @FXML private TableColumn<Reserva, String> colResHuesped;
    @FXML private TableColumn<Reserva, String> colResHab;
    @FXML private TableColumn<Reserva, String> colResEntrada;
    @FXML private TableColumn<Reserva, String> colResSalida;
    @FXML private TableColumn<Reserva, String> colResNoches;
    @FXML private TableColumn<Reserva, String> colResTotal;

    // ── Panel 2: Ocupación
    @FXML private DatePicker dpOcuDesde;
    @FXML private DatePicker dpOcuHasta;
    @FXML private Label lblOcuPct;
    @FXML private Label lblOcuNoches;
    @FXML private Label lblOcuHabs;
    @FXML private Label lblOcuADR;
    @FXML private TableView<Reserva>           tblOcupacion;
    @FXML private TableColumn<Reserva, String> colOcuHab;
    @FXML private TableColumn<Reserva, String> colOcuTipo;
    @FXML private TableColumn<Reserva, String> colOcuEntrada;
    @FXML private TableColumn<Reserva, String> colOcuSalida;
    @FXML private TableColumn<Reserva, String> colOcuNoches;
    @FXML private TableColumn<Reserva, String> colOcuTotal;

    // ── Panel 3: Ingresos
    @FXML private DatePicker dpIngDesde;
    @FXML private DatePicker dpIngHasta;
    @FXML private ComboBox<String> cmbIngEstado;
    @FXML private Label lblIngFacturado;
    @FXML private Label lblIngCobrado;
    @FXML private Label lblIngPendiente;
    @FXML private Label lblIngCount;
    @FXML private TableView<Factura>           tblIngresos;
    @FXML private TableColumn<Factura, String> colIngNum;
    @FXML private TableColumn<Factura, String> colIngFecha;
    @FXML private TableColumn<Factura, String> colIngHuesped;
    @FXML private TableColumn<Factura, String> colIngTotal;
    @FXML private TableColumn<Factura, String> colIngCobrado;
    @FXML private TableColumn<Factura, String> colIngEstado;

    // ── Panel 4: ADR / RevPAR
    @FXML private DatePicker dpADRDesde;
    @FXML private DatePicker dpADRHasta;
    @FXML private Label lblADR;
    @FXML private Label lblRevPAR;
    @FXML private Label lblADRNoches;
    @FXML private Label lblADRTotal;
    @FXML private TableView<Reserva>           tblADR;
    @FXML private TableColumn<Reserva, String> colADRHab;
    @FXML private TableColumn<Reserva, String> colADRTipo;
    @FXML private TableColumn<Reserva, String> colADREntrada;
    @FXML private TableColumn<Reserva, String> colADRSalida;
    @FXML private TableColumn<Reserva, String> colADRNoches;
    @FXML private TableColumn<Reserva, String> colADRTotal;
    @FXML private TableColumn<Reserva, String> colADRPorNoche;

    // ── Panel 5: Huéspedes
    @FXML private DatePicker dpHueDesde;
    @FXML private DatePicker dpHueHasta;
    @FXML private Label lblHueTotales;
    @FXML private Label lblHueReservas;
    @FXML private Label lblHueNoches;
    @FXML private Label lblHueGastoMedio;
    @FXML private TableView<GuestStat>           tblHuespedes;
    @FXML private TableColumn<GuestStat, String> colHueNombre;
    @FXML private TableColumn<GuestStat, String> colHueEmail;
    @FXML private TableColumn<GuestStat, String> colHuePais;
    @FXML private TableColumn<GuestStat, String> colHueReservas;
    @FXML private TableColumn<GuestStat, String> colHueNoches;
    @FXML private TableColumn<GuestStat, String> colHueTotal;

    // ── Panel 6: Exportación
    @FXML private DatePicker dpExpDesde;
    @FXML private DatePicker dpExpHasta;
    @FXML private ComboBox<String> cmbExpTipo;
    @FXML private Label lblExpStatus;



    private record GuestStat(Huesped guest, int reservas, long noches, BigDecimal total) {}

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DateTimeFormatter DT_FMT   = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    private List<VBox>   allPanels;
    private List<Button> allNavBtns;

    public void setIndexController(IndexController ic) { this.indexController = ic; }

    @FXML
    public void initialize() {
        LocalDate hoy    = LocalDate.now();
        LocalDate inicio = hoy.withDayOfMonth(1);

        Stream.of(dpResDesde, dpOcuDesde, dpIngDesde, dpADRDesde, dpHueDesde, dpExpDesde)
                .forEach(dp -> dp.setValue(inicio));
        Stream.of(dpResHasta, dpOcuHasta, dpIngHasta, dpADRHasta, dpHueHasta, dpExpHasta)
                .forEach(dp -> dp.setValue(hoy));

        cmbIngEstado.setItems(FXCollections.observableArrayList(
                "Todas", "Pendiente", "Pagada", "Parcial", "Cancelada"));
        cmbIngEstado.setValue("Todas");

        cmbExpTipo.setItems(FXCollections.observableArrayList("Facturas", "Reservas", "Pagos"));
        cmbExpTipo.setValue("Facturas");

        configurarColumnas();

        allPanels  = List.of(pnlResumen, pnlOcupacion, pnlIngresos, pnlADR, pnlHuespedes, pnlExportar);
        allNavBtns = List.of(btnNavResumen, btnNavOcupacion, btnNavIngresos, btnNavADR, btnNavHuespedes, btnNavExportar);

        mostrarPanel(pnlResumen, btnNavResumen);
        cargarResumen();
    }

    // ── Navigation

    private void mostrarPanel(VBox panel, Button btn) {
        allPanels.forEach(p  -> { p.setVisible(false); p.setManaged(false); });
        allNavBtns.forEach(b -> b.getStyleClass().remove("nav-btn-active"));
        panel.setVisible(true);
        panel.setManaged(true);
        btn.getStyleClass().add("nav-btn-active");
    }

    @FXML private void navResumen()   { mostrarPanel(pnlResumen,   btnNavResumen);   cargarResumen(); }
    @FXML private void navOcupacion() { mostrarPanel(pnlOcupacion, btnNavOcupacion); cargarOcupacion(); }
    @FXML private void navIngresos()  { mostrarPanel(pnlIngresos,  btnNavIngresos);  cargarIngresos(); }
    @FXML private void navADR()       { mostrarPanel(pnlADR,       btnNavADR);       cargarADR(); }
    @FXML private void navHuespedes() { mostrarPanel(pnlHuespedes, btnNavHuespedes); cargarHuespedes(); }
    @FXML private void navExportar()  { mostrarPanel(pnlExportar,  btnNavExportar); }

    @FXML
    private void volverInicio() {
        if (indexController != null) indexController.mostrarInicio();
    }

    // ── Panel 1: Resumen

    @FXML
    private void cargarResumen() {
        LocalDate desde = dpResDesde.getValue();
        LocalDate hasta = dpResHasta.getValue();
        if (desde == null || hasta == null) return;
        try {
            List<Reserva> reservas   = ReservaDAO.obtenerPorRango(desde, hasta);
            List<Factura>     facturas   = FacturaDAO.getPorRango(desde, hasta);
            int               totalHabs  = HabitacionDAO.obtenerTodas().size();

            long   dias        = ChronoUnit.DAYS.between(desde, hasta) + 1;
            long   nightsAvail = totalHabs * dias;
            long   nightsSold  = calcNightsSold(reservas, desde, hasta);
            double ocupPct     = nightsAvail > 0 ? (nightsSold * 100.0 / nightsAvail) : 0;

            BigDecimal facturado = sum(facturas, Factura::getImporteTotal);
            BigDecimal cobrado   = sum(facturas, Factura::getImportePagado);

            lblResOcupacion.setText(String.format("%.1f%%", ocupPct));
            lblResFacturado.setText(String.format("%.2f €", facturado));
            lblResCobrado  .setText(String.format("%.2f €", cobrado));
            lblResNoches   .setText(String.valueOf(nightsSold));

            tblResumen.setItems(FXCollections.observableArrayList(reservas));
        } catch (SQLException e) {
            error("Error cargando resumen: " + e.getMessage());
        }
    }

    // ── Panel 2: Ocupación

    @FXML
    private void cargarOcupacion() {
        LocalDate desde = dpOcuDesde.getValue();
        LocalDate hasta = dpOcuHasta.getValue();
        if (desde == null || hasta == null) return;
        try {
            List<Reserva> reservas  = ReservaDAO.obtenerPorRango(desde, hasta);
            int               totalHabs = HabitacionDAO.obtenerTodas().size();
            long dias        = ChronoUnit.DAYS.between(desde, hasta) + 1;
            long nightsAvail = totalHabs * dias;
            long nightsSold  = calcNightsSold(reservas, desde, hasta);
            double ocupPct   = nightsAvail > 0 ? (nightsSold * 100.0 / nightsAvail) : 0;

            BigDecimal totalRev = sumRes(reservas);
            BigDecimal adr = nightsSold > 0
                    ? totalRev.divide(BigDecimal.valueOf(nightsSold), 2, RoundingMode.HALF_UP)
                    : BigDecimal.ZERO;

            lblOcuPct   .setText(String.format("%.1f%%", ocupPct));
            lblOcuNoches.setText(String.valueOf(nightsSold));
            lblOcuHabs  .setText(totalHabs + " hab.");
            lblOcuADR   .setText(String.format("%.2f €", adr));

            tblOcupacion.setItems(FXCollections.observableArrayList(reservas));
        } catch (SQLException e) {
            error("Error cargando ocupación: " + e.getMessage());
        }
    }

    // ── Panel 3: Ingresos

    @FXML
    private void cargarIngresos() {
        LocalDate desde = dpIngDesde.getValue();
        LocalDate hasta = dpIngHasta.getValue();
        if (desde == null || hasta == null) return;
        try {
            List<Factura> todas = FacturaDAO.getPorRango(desde, hasta);
            String filtro = cmbIngEstado.getValue();
            List<Factura> facturas = "Todas".equals(filtro)
                    ? todas
                    : todas.stream().filter(i -> filtro.equals(i.getEstado().toSpanish())).toList();

            BigDecimal facturado = sum(facturas, Factura::getImporteTotal);
            BigDecimal cobrado   = sum(facturas, Factura::getImportePagado);
            BigDecimal pendiente = facturado.subtract(cobrado).max(BigDecimal.ZERO);

            lblIngFacturado.setText(String.format("%.2f €", facturado));
            lblIngCobrado  .setText(String.format("%.2f €", cobrado));
            lblIngPendiente.setText(String.format("%.2f €", pendiente));
            lblIngCount    .setText(facturas.size() + " facturas");

            tblIngresos.setItems(FXCollections.observableArrayList(facturas));
        } catch (SQLException e) {
            error("Error cargando ingresos: " + e.getMessage());
        }
    }

    // ── Panel 4: ADR / RevPAR

    @FXML
    private void cargarADR() {
        LocalDate desde = dpADRDesde.getValue();
        LocalDate hasta = dpADRHasta.getValue();
        if (desde == null || hasta == null) return;
        try {
            List<Reserva> reservas  = ReservaDAO.obtenerPorRango(desde, hasta);
            int               totalHabs = HabitacionDAO.obtenerTodas().size();
            long dias        = ChronoUnit.DAYS.between(desde, hasta) + 1;
            long nightsAvail = totalHabs * dias;
            long nightsSold  = calcNightsSold(reservas, desde, hasta);

            BigDecimal totalRev = sumRes(reservas);
            BigDecimal adr = nightsSold > 0
                    ? totalRev.divide(BigDecimal.valueOf(nightsSold), 2, RoundingMode.HALF_UP)
                    : BigDecimal.ZERO;
            BigDecimal revpar = nightsAvail > 0
                    ? totalRev.divide(BigDecimal.valueOf(nightsAvail), 2, RoundingMode.HALF_UP)
                    : BigDecimal.ZERO;

            lblADR      .setText(String.format("%.2f €", adr));
            lblRevPAR   .setText(String.format("%.2f €", revpar));
            lblADRNoches.setText(String.valueOf(nightsSold));
            lblADRTotal .setText(String.format("%.2f €", totalRev));

            tblADR.setItems(FXCollections.observableArrayList(reservas));
        } catch (SQLException e) {
            error("Error cargando ADR/RevPAR: " + e.getMessage());
        }
    }

    // ── Panel 5: Huéspedes

    @FXML
    private void cargarHuespedes() {
        LocalDate desde = dpHueDesde.getValue();
        LocalDate hasta = dpHueHasta.getValue();
        if (desde == null || hasta == null) return;
        try {
            List<Reserva> reservas = ReservaDAO.obtenerPorRango(desde, hasta);

            Map<Integer, List<Reserva>> byGuest = reservas.stream()
                    .collect(Collectors.groupingBy(r -> r.getHuesped().getId()));

            List<GuestStat> stats = byGuest.values().stream().map(list -> {
                Huesped      g      = list.get(0).getHuesped();
                int        numRes = list.size();
                long       noches = list.stream().mapToLong(Reserva::getNoches).sum();
                BigDecimal total  = list.stream()
                        .map(r -> r.getPrecioTotal() != null ? r.getPrecioTotal() : BigDecimal.ZERO)
                        .reduce(BigDecimal.ZERO, BigDecimal::add);
                return new GuestStat(g, numRes, noches, total);
            }).sorted(Comparator.comparing(GuestStat::total).reversed()).toList();

            int        totalGuests = stats.size();
            int        totalRes    = stats.stream().mapToInt(GuestStat::reservas).sum();
            long       totalNoches = stats.stream().mapToLong(GuestStat::noches).sum();
            BigDecimal gastoMedio  = totalGuests > 0
                    ? stats.stream().map(GuestStat::total).reduce(BigDecimal.ZERO, BigDecimal::add)
                    .divide(BigDecimal.valueOf(totalGuests), 2, RoundingMode.HALF_UP)
                    : BigDecimal.ZERO;

            lblHueTotales  .setText(String.valueOf(totalGuests));
            lblHueReservas .setText(String.valueOf(totalRes));
            lblHueNoches   .setText(String.valueOf(totalNoches));
            lblHueGastoMedio.setText(String.format("%.2f €", gastoMedio));

            tblHuespedes.setItems(FXCollections.observableArrayList(stats));
        } catch (SQLException e) {
            error("Error cargando huéspedes: " + e.getMessage());
        }
    }

    // ── Panel 6: Exportación

    @FXML
    private void exportarCSV() {
        LocalDate desde = dpExpDesde.getValue();
        LocalDate hasta = dpExpHasta.getValue();
        if (desde == null || hasta == null) return;

        FileChooser fc = new FileChooser();
        fc.setTitle("Exportar informe");
        fc.setInitialFileName("informe_" + desde + "_" + hasta + ".csv");
        fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("CSV", "*.csv"));
        File file = fc.showSaveDialog(lblExpStatus.getScene().getWindow());
        if (file == null) return;

        try {
            switch (cmbExpTipo.getValue()) {
                case "Facturas" -> exportarFacturas(file, desde, hasta);
                case "Reservas" -> exportarReservas(file, desde, hasta);
                case "Pagos"    -> exportarPagos(file, desde, hasta);
            }
            lblExpStatus.setText("Exportado: " + file.getAbsolutePath());
            lblExpStatus.setStyle("-fx-text-fill: #27AE60; -fx-font-weight: bold;");
        } catch (Exception e) {
            lblExpStatus.setText("Error al exportar: " + e.getMessage());
            lblExpStatus.setStyle("-fx-text-fill: #E74C3C; -fx-font-weight: bold;");
        }
    }

    private void exportarFacturas(File f, LocalDate desde, LocalDate hasta) throws Exception {
        List<Factura> list = FacturaDAO.getPorRango(desde, hasta);
        try (PrintWriter pw = new PrintWriter(new OutputStreamWriter(new FileOutputStream(f), "UTF-8"))) {
            pw.println("Número;Fecha;Huésped;Total;Cobrado;Pendiente;Estado;Método");
            for (Factura inv : list) {
                String nombre = inv.tieneDatosFacturacion() ? inv.getNombreFacturacion()
                        : (inv.getHuesped() != null ? inv.getHuesped().getNombreCompleto() : "");
                pw.printf("%s;%s;%s;%.2f;%.2f;%.2f;%s;%s%n",
                        inv.getNumeroFactura(),
                        inv.getFechaEmision() != null ? inv.getFechaEmision().format(DATE_FMT) : "",
                        nombre,
                        inv.getImporteTotal(),
                        inv.getImportePagado(),
                        inv.getImportePendiente(),
                        inv.getEstado().toSpanish(),
                        inv.getMetodoPago() != null ? inv.getMetodoPago().toSpanish() : "");
            }
        }
    }

    private void exportarReservas(File f, LocalDate desde, LocalDate hasta) throws Exception {
        List<Reserva> list = ReservaDAO.obtenerPorRango(desde, hasta);
        try (PrintWriter pw = new PrintWriter(new OutputStreamWriter(new FileOutputStream(f), "UTF-8"))) {
            pw.println("Número;Huésped;Habitación;Entrada;Salida;Noches;Total;Estado");
            for (Reserva r : list) {
                pw.printf("%s;%s;%s;%s;%s;%d;%.2f;%s%n",
                        r.getNumeroReserva(),
                        r.getHuesped() != null ? r.getHuesped().getNombreCompleto() : "",
                        r.getHabitacion()  != null ? r.getHabitacion().getNumero()    : "",
                        r.getFechaEntrada().format(DATE_FMT),
                        r.getFechaSalida().format(DATE_FMT),
                        r.getNoches(),
                        r.getPrecioTotal() != null ? r.getPrecioTotal() : BigDecimal.ZERO,
                        r.getEstado().toSpanish());
            }
        }
    }

    private void exportarPagos(File f, LocalDate desde, LocalDate hasta) throws Exception {
        List<Pago> list = PagoDAO.getPorRango(desde, hasta);
        try (PrintWriter pw = new PrintWriter(new OutputStreamWriter(new FileOutputStream(f), "UTF-8"))) {
            pw.println("Fecha;Factura;Huésped;Importe;Método;Referencia");
            for (Pago p : list) {
                pw.printf("%s;%s;%s;%.2f;%s;%s%n",
                        p.getFechaPago() != null ? p.getFechaPago().format(DT_FMT) : "",
                        p.getNumeroFactura()  != null ? p.getNumeroFactura()  : String.valueOf(p.getIdFactura()),
                        p.getNombreHuesped()      != null ? p.getNombreHuesped()      : "",
                        p.getImporte(),
                        p.getMetodo()         != null ? p.getMetodo().toSpanish() : "",
                        p.getReferencia()      != null ? p.getReferencia()      : "");
            }
        }
    }

    // ── Column configuration

    private void configurarColumnas() {
        // Panel 1
        colResNum     .setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getNumeroReserva()));
        colResHuesped .setCellValueFactory(c -> new SimpleStringProperty(
                c.getValue().getHuesped() != null ? c.getValue().getHuesped().getNombreCompleto() : "-"));
        colResHab     .setCellValueFactory(c -> new SimpleStringProperty(
                c.getValue().getHabitacion()  != null ? c.getValue().getHabitacion().getNumero()    : "-"));
        colResEntrada .setCellValueFactory(c -> new SimpleStringProperty(
                c.getValue().getFechaEntrada().format(DATE_FMT)));
        colResSalida  .setCellValueFactory(c -> new SimpleStringProperty(
                c.getValue().getFechaSalida().format(DATE_FMT)));
        colResNoches  .setCellValueFactory(c -> new SimpleStringProperty(String.valueOf(c.getValue().getNoches())));
        colResTotal   .setCellValueFactory(c -> new SimpleStringProperty(
                c.getValue().getPrecioTotal() != null ? String.format("%.2f €", c.getValue().getPrecioTotal()) : "-"));

        // Panel 2
        colOcuHab    .setCellValueFactory(c -> new SimpleStringProperty(
                c.getValue().getHabitacion() != null ? c.getValue().getHabitacion().getNumero() : "-"));
        colOcuTipo   .setCellValueFactory(c -> {
            Habitacion rm = c.getValue().getHabitacion();
            return new SimpleStringProperty(rm != null && rm.getTipoHabitacion() != null ? rm.getTipoHabitacion().getNombre() : "-");
        });
        colOcuEntrada.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getFechaEntrada().format(DATE_FMT)));
        colOcuSalida .setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getFechaSalida().format(DATE_FMT)));
        colOcuNoches .setCellValueFactory(c -> new SimpleStringProperty(String.valueOf(c.getValue().getNoches())));
        colOcuTotal  .setCellValueFactory(c -> new SimpleStringProperty(
                c.getValue().getPrecioTotal() != null ? String.format("%.2f €", c.getValue().getPrecioTotal()) : "-"));

        // Panel 3
        colIngNum    .setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getNumeroFactura()));
        colIngFecha  .setCellValueFactory(c -> new SimpleStringProperty(
                c.getValue().getFechaEmision() != null ? c.getValue().getFechaEmision().format(DATE_FMT) : "-"));
        colIngHuesped.setCellValueFactory(c -> {
            Factura inv = c.getValue();
            if (inv.tieneDatosFacturacion()) return new SimpleStringProperty(inv.getNombreFacturacion());
            return new SimpleStringProperty(inv.getHuesped() != null ? inv.getHuesped().getNombreCompleto() : "-");
        });
        colIngTotal  .setCellValueFactory(c -> new SimpleStringProperty(
                String.format("%.2f €", c.getValue().getImporteTotal())));
        colIngCobrado.setCellValueFactory(c -> new SimpleStringProperty(
                String.format("%.2f €", c.getValue().getImportePagado())));
        colIngEstado .setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getEstado().toSpanish()));

        // Panel 4
        colADRHab    .setCellValueFactory(c -> new SimpleStringProperty(
                c.getValue().getHabitacion() != null ? c.getValue().getHabitacion().getNumero() : "-"));
        colADRTipo   .setCellValueFactory(c -> {
            Habitacion rm = c.getValue().getHabitacion();
            return new SimpleStringProperty(rm != null && rm.getTipoHabitacion() != null ? rm.getTipoHabitacion().getNombre() : "-");
        });
        colADREntrada .setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getFechaEntrada().format(DATE_FMT)));
        colADRSalida  .setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getFechaSalida().format(DATE_FMT)));
        colADRNoches  .setCellValueFactory(c -> new SimpleStringProperty(String.valueOf(c.getValue().getNoches())));
        colADRTotal   .setCellValueFactory(c -> new SimpleStringProperty(
                c.getValue().getPrecioTotal() != null ? String.format("%.2f €", c.getValue().getPrecioTotal()) : "-"));
        colADRPorNoche.setCellValueFactory(c -> {
            BigDecimal t = c.getValue().getPrecioTotal();
            long       n = c.getValue().getNoches();
            if (t != null && n > 0)
                return new SimpleStringProperty(String.format("%.2f €",
                        t.divide(BigDecimal.valueOf(n), 2, RoundingMode.HALF_UP)));
            return new SimpleStringProperty("-");
        });

        // Panel 5
        colHueNombre  .setCellValueFactory(c -> new SimpleStringProperty(c.getValue().guest().getNombreCompleto()));
        colHueEmail   .setCellValueFactory(c -> new SimpleStringProperty(
                c.getValue().guest().getEmail() != null ? c.getValue().guest().getEmail() : "-"));
        colHuePais    .setCellValueFactory(c -> {
            Huesped g = c.getValue().guest();
            String pais = g.getPais() != null ? g.getPais()
                    : g.getNacionalidad() != null ? g.getNacionalidad() : "-";
            return new SimpleStringProperty(pais);
        });
        colHueReservas.setCellValueFactory(c -> new SimpleStringProperty(String.valueOf(c.getValue().reservas())));
        colHueNoches  .setCellValueFactory(c -> new SimpleStringProperty(String.valueOf(c.getValue().noches())));
        colHueTotal   .setCellValueFactory(c -> new SimpleStringProperty(String.format("%.2f €", c.getValue().total())));
    }

    // ── Helpers

    private long calcNightsSold(List<Reserva> reservas, LocalDate from, LocalDate to) {
        LocalDate toExcl = to.plusDays(1);
        return reservas.stream().mapToLong(r -> {
            LocalDate s = r.getFechaEntrada().isBefore(from)    ? from    : r.getFechaEntrada();
            LocalDate e = r.getFechaSalida().isAfter(toExcl)  ? toExcl  : r.getFechaSalida();
            return Math.max(0, ChronoUnit.DAYS.between(s, e));
        }).sum();
    }

    private BigDecimal sumRes(List<Reserva> list) {
        return list.stream()
                .map(r -> r.getPrecioTotal() != null ? r.getPrecioTotal() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    @FunctionalInterface private interface InvoiceGetter { BigDecimal get(Factura i); }

    private BigDecimal sum(List<Factura> list, InvoiceGetter getter) {
        return list.stream()
                .map(getter::get)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private void error(String msg) {
        new Alert(Alert.AlertType.ERROR, msg, ButtonType.OK).showAndWait();
    }
}

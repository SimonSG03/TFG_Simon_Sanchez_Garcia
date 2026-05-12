package es.simonsg.pmsuite.controller;

import es.simonsg.pmsuite.dao.*;
import es.simonsg.pmsuite.db.GestorBD;
import es.simonsg.pmsuite.model.*;
import es.simonsg.pmsuite.model.Paquete;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.Alert;
import javafx.scene.layout.VBox;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;

public class PreciosController {

    // ── Navigation ─────────────────────────────────────────────────────────────
    @FXML private Button btnNavTemporadas, btnNavTarifas, btnNavPreciosDia;
    @FXML private Button btnNavPromociones, btnNavPaquetes, btnNavYield;

    // ── Panels ─────────────────────────────────────────────────────────────────
    @FXML private VBox pnlTemporadas, pnlTarifas, pnlPreciosDia;
    @FXML private VBox pnlPromociones, pnlPaquetes, pnlYield;

    // ── Panel Temporadas ────────────────────────────────────────────────────────
    @FXML private TableView<Temporada> tblTemporadas;
    @FXML private TableColumn<Temporada,String>     colTempNombre;
    @FXML private TableColumn<Temporada,String>     colTempInicio;
    @FXML private TableColumn<Temporada,String>     colTempFin;
    @FXML private TableColumn<Temporada,String>     colTempMultipl;
    @FXML private TextField   txtTempNombre, txtTempMultipl;
    @FXML private DatePicker  dpTempInicio, dpTempFin;

    // ── Panel Tarifas ───────────────────────────────────────────────────────────
    @FXML private TableView<PlanTarifa> tblTarifas;
    @FXML private TableColumn<PlanTarifa,String>  colTarTipo, colTarTemporada, colTarNombre;
    @FXML private TableColumn<PlanTarifa,String>  colTarPrecio, colTarMin, colTarMax, colTarDesayuno;
    @FXML private ComboBox<TipoHabitacion>  cmbTarTipo;
    @FXML private ComboBox<Temporada>    cmbTarTemporada;
    @FXML private TextField           txtTarNombre, txtTarPrecio;
    @FXML private Spinner<Integer>    spnTarMin, spnTarMax;
    @FXML private CheckBox            chkTarDesayuno;

    // ── Panel Precios por Día ───────────────────────────────────────────────────
    @FXML private TableView<TarifaDiaria> tblPreciosDia;
    @FXML private TableColumn<TarifaDiaria,String> colDiaTipo, colDiaFecha, colDiaPrecio, colDiaNota;
    @FXML private ComboBox<TipoHabitacion>  cmbDiaTipo;
    @FXML private DatePicker          dpDiaDesde, dpDiaHasta, dpDiaFecha;
    @FXML private TextField           txtDiaPrecio, txtDiaNota;

    // ── Panel Promociones ───────────────────────────────────────────────────────
    @FXML private TableView<Promocion> tblPromociones;
    @FXML private TableColumn<Promocion,String>  colPromoNombre, colPromoCodigo;
    @FXML private TableColumn<Promocion,String>  colPromoTipo, colPromoDescuento;
    @FXML private TableColumn<Promocion,String>  colPromoInicio, colPromoFin, colPromoActivo;
    @FXML private TextField   txtPromoNombre, txtPromoCodigo, txtPromoValor, txtPromoNota;
    @FXML private ComboBox<String> cmbPromoTipo;
    @FXML private DatePicker  dpPromoInicio, dpPromoFin;
    @FXML private CheckBox    chkPromoActivo;

    // ── Panel Paquetes ──────────────────────────────────────────────────────────
    @FXML private TableView<Paquete> tblPaquetes;
    @FXML private TableColumn<Paquete,String>  colPaqNombre, colPaqDescripcion;
    @FXML private TableColumn<Paquete,String>  colPaqPrecio, colPaqActivo;
    @FXML private TextField   txtPaqNombre, txtPaqDescripcion, txtPaqPrecio;
    @FXML private TextArea    txtPaqContenido;
    @FXML private CheckBox    chkPaqActivo;

    // ── Panel Yield ─────────────────────────────────────────────────────────────
    @FXML private TableView<String[]> tblYield;
    @FXML private TableColumn<String[],String> colYieldFecha, colYieldOcupacion, colYieldHabLibres;
    @FXML private TableColumn<String[],String> colYieldPrecioBase, colYieldAjuste, colYieldPrecioFinal;
    @FXML private TextField txtYieldBajaUmbral, txtYieldBajaDesc;
    @FXML private TextField txtYieldAltaUmbral, txtYieldAltaPremium;
    @FXML private TextField txtYieldDias;

    private IndexController indexController;
    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    // ── SELECTED ────────────────────────────────────────────────────────────────
    private Temporada    selectedSeason;
    private PlanTarifa  selectedRatePlan;
    private TarifaDiaria selectedDailyRate;
    private Promocion selectedPromotion;
    private Paquete   selectedPackage;

    // ────────────────────────────────────────────────────────────────────────────
    @FXML
    public void initialize() {
        configurarColumnas();
        configurarCombos();
        configurarSeleccion();
        mostrarTemporadas();
    }

    public void setIndexController(IndexController ic) { this.indexController = ic; }

    @FXML private void volverInicio() {
        if (indexController != null) indexController.mostrarInicio();
    }

    // ── Panel navigation ────────────────────────────────────────────────────────
    @FXML public void mostrarTemporadas()  { showPanel(pnlTemporadas, btnNavTemporadas);  cargarTemporadas(); }
    @FXML public void mostrarTarifas()     { showPanel(pnlTarifas,    btnNavTarifas);     cargarTarifas();    }
    @FXML public void mostrarPreciosDia()  { showPanel(pnlPreciosDia, btnNavPreciosDia);  cargarPreciosDia(); }
    @FXML public void mostrarPromociones() { showPanel(pnlPromociones, btnNavPromociones); cargarPromociones();}
    @FXML public void mostrarPaquetes()    { showPanel(pnlPaquetes,   btnNavPaquetes);    cargarPaquetes();   }
    @FXML public void mostrarYield()       { showPanel(pnlYield,      btnNavYield);       calcularYield();    }

    private void showPanel(VBox panel, Button btn) {
        VBox[] panels = {pnlTemporadas, pnlTarifas, pnlPreciosDia, pnlPromociones, pnlPaquetes, pnlYield};
        Button[] btns  = {btnNavTemporadas, btnNavTarifas, btnNavPreciosDia, btnNavPromociones, btnNavPaquetes, btnNavYield};
        for (int i = 0; i < panels.length; i++) {
            boolean active = panels[i] == panel;
            panels[i].setVisible(active);
            panels[i].setManaged(active);
            btns[i].getStyleClass().removeAll("nav-btn-active");
            if (active) btns[i].getStyleClass().add("nav-btn-active");
        }
    }

    // ────────────────────────────────────────────────────────────────────────────
    // CONFIGURACIÓN
    // ────────────────────────────────────────────────────────────────────────────
    private void configurarColumnas() {
        // Temporadas
        colTempNombre .setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getNombre()));
        colTempInicio .setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getFechaInicio().format(FMT)));
        colTempFin    .setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getFechaFin().format(FMT)));
        colTempMultipl.setCellValueFactory(c -> new SimpleStringProperty("×" + c.getValue().getMultiplicador()));

        // Tarifas
        colTarTipo     .setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getTipoHabitacion() != null ? c.getValue().getTipoHabitacion().getNombre() : ""));
        colTarTemporada.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getTemporada() != null ? c.getValue().getTemporada().getNombre() : "Base"));
        colTarNombre   .setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getNombre()));
        colTarPrecio   .setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getPrecioPorNoche() != null ? c.getValue().getPrecioPorNoche().toPlainString() + " €" : ""));
        colTarMin      .setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getNochesMinimas() + " noche(s)"));
        colTarMax      .setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getNochesMaximas() != null && c.getValue().getNochesMaximas() > 0 ? c.getValue().getNochesMaximas() + " noche(s)" : "—"));
        colTarDesayuno .setCellValueFactory(c -> new SimpleStringProperty(c.getValue().isIncluyeDesayuno() ? "Sí" : "No"));

        // Precios por día
        colDiaTipo  .setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getTipoHabitacion() != null ? c.getValue().getTipoHabitacion().getNombre() : ""));
        colDiaFecha .setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getFecha().format(FMT)));
        colDiaPrecio.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getPrecio() != null ? c.getValue().getPrecio().toPlainString() + " €" : ""));
        colDiaNota  .setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getNotas() != null ? c.getValue().getNotas() : ""));

        // Promociones
        colPromoNombre   .setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getNombre()));
        colPromoCodigo   .setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getCodigo()));
        colPromoTipo     .setCellValueFactory(c -> new SimpleStringProperty("PERCENT".equals(c.getValue().getTipoDescuento()) ? "%" : "€"));
        colPromoDescuento.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getValorDescuento().toPlainString() + ("PERCENT".equals(c.getValue().getTipoDescuento()) ? "%" : " €")));
        colPromoInicio   .setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getFechaInicio().format(FMT)));
        colPromoFin      .setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getFechaFin().format(FMT)));
        colPromoActivo   .setCellValueFactory(c -> new SimpleStringProperty(c.getValue().isActivo() ? "✓" : "—"));

        // Paquetes
        colPaqNombre     .setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getNombre()));
        colPaqDescripcion.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getDescripcion() != null ? c.getValue().getDescripcion() : ""));
        colPaqPrecio     .setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getPrecioBase() != null ? c.getValue().getPrecioBase().toPlainString() + " €" : ""));
        colPaqActivo     .setCellValueFactory(c -> new SimpleStringProperty(c.getValue().isActivo() ? "✓" : "—"));

        // Yield
        colYieldFecha      .setCellValueFactory(c -> new SimpleStringProperty(c.getValue()[0]));
        colYieldOcupacion  .setCellValueFactory(c -> new SimpleStringProperty(c.getValue()[1]));
        colYieldHabLibres  .setCellValueFactory(c -> new SimpleStringProperty(c.getValue()[2]));
        colYieldPrecioBase .setCellValueFactory(c -> new SimpleStringProperty(c.getValue()[3]));
        colYieldAjuste     .setCellValueFactory(c -> new SimpleStringProperty(c.getValue()[4]));
        colYieldPrecioFinal.setCellValueFactory(c -> new SimpleStringProperty(c.getValue()[5]));
    }

    private void configurarCombos() {
        // Spinners con SpinnerValueFactory
        spnTarMin.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 365, 1));
        spnTarMax.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 365, 0));

        cmbPromoTipo.setItems(FXCollections.observableArrayList("% Porcentaje", "€ Importe fijo"));
        cmbPromoTipo.getSelectionModel().selectFirst();

        // Cargar tipos de habitación en combos
        try {
            List<TipoHabitacion> tipos = getRoomTypes();
            cmbTarTipo.setItems(FXCollections.observableArrayList(tipos));
            cmbDiaTipo.setItems(FXCollections.observableArrayList(tipos));
        } catch (Exception e) { e.printStackTrace(); }

        // Cargar temporadas en combo de tarifas (con opción "Sin temporada")
        recargarTemporadasCombo();

        // Rango de fechas por defecto en precios por día
        dpDiaDesde.setValue(LocalDate.now());
        dpDiaHasta.setValue(LocalDate.now().plusDays(30));
    }

    private void recargarTemporadasCombo() {
        try {
            List<Temporada> seasons = TemporadaDAO.obtenerTodas();
            ObservableList<Temporada> items = FXCollections.observableArrayList();
            items.add(null); // "Sin temporada (base)"
            items.addAll(seasons);
            cmbTarTemporada.setItems(items);
            cmbTarTemporada.setConverter(new javafx.util.StringConverter<>() {
                @Override public String toString(Temporada s) { return s == null ? "Sin temporada (base)" : s.getNombre(); }
                @Override public Temporada fromString(String s) { return null; }
            });
        } catch (Exception e) { e.printStackTrace(); }
    }

    private void configurarSeleccion() {
        tblTemporadas.getSelectionModel().selectedItemProperty().addListener((o, ov, s) -> {
            selectedSeason = s;
            if (s != null) {
                txtTempNombre.setText(s.getNombre());
                dpTempInicio.setValue(s.getFechaInicio());
                dpTempFin.setValue(s.getFechaFin());
                txtTempMultipl.setText(s.getMultiplicador().toPlainString());
            }
        });

        tblTarifas.getSelectionModel().selectedItemProperty().addListener((o, ov, rp) -> {
            selectedRatePlan = rp;
            if (rp != null) {
                cmbTarTipo.setValue(rp.getTipoHabitacion());
                cmbTarTemporada.setValue(rp.getTemporada());
                txtTarNombre.setText(rp.getNombre());
                txtTarPrecio.setText(rp.getPrecioPorNoche().toPlainString());
                spnTarMin.getValueFactory().setValue(rp.getNochesMinimas());
                spnTarMax.getValueFactory().setValue(rp.getNochesMaximas() != null ? rp.getNochesMaximas() : 0);
                chkTarDesayuno.setSelected(rp.isIncluyeDesayuno());
            }
        });

        tblPreciosDia.getSelectionModel().selectedItemProperty().addListener((o, ov, dr) -> {
            selectedDailyRate = dr;
            if (dr != null) {
                cmbDiaTipo.setValue(dr.getTipoHabitacion());
                dpDiaFecha.setValue(dr.getFecha());
                txtDiaPrecio.setText(dr.getPrecio().toPlainString());
                txtDiaNota.setText(dr.getNotas() != null ? dr.getNotas() : "");
            }
        });

        tblPromociones.getSelectionModel().selectedItemProperty().addListener((o, ov, p) -> {
            selectedPromotion = p;
            if (p != null) {
                txtPromoNombre.setText(p.getNombre());
                txtPromoCodigo.setText(p.getCodigo());
                cmbPromoTipo.getSelectionModel().select("PERCENT".equals(p.getTipoDescuento()) ? 0 : 1);
                txtPromoValor.setText(p.getValorDescuento().toPlainString());
                dpPromoInicio.setValue(p.getFechaInicio());
                dpPromoFin.setValue(p.getFechaFin());
                chkPromoActivo.setSelected(p.isActivo());
                txtPromoNota.setText(p.getNotas() != null ? p.getNotas() : "");
            }
        });

        tblPaquetes.getSelectionModel().selectedItemProperty().addListener((o, ov, pk) -> {
            selectedPackage = pk;
            if (pk != null) {
                txtPaqNombre.setText(pk.getNombre());
                txtPaqDescripcion.setText(pk.getDescripcion() != null ? pk.getDescripcion() : "");
                txtPaqPrecio.setText(pk.getPrecioBase().toPlainString());
                txtPaqContenido.setText(pk.getContenido() != null ? pk.getContenido() : "");
                chkPaqActivo.setSelected(pk.isActivo());
            }
        });
    }

    // ────────────────────────────────────────────────────────────────────────────
    // CARGAR DATOS
    // ────────────────────────────────────────────────────────────────────────────
    private void cargarTemporadas() {
        try { tblTemporadas.setItems(FXCollections.observableArrayList(TemporadaDAO.obtenerTodas())); }
        catch (Exception e) { mostrarError("Error cargando temporadas", e.getMessage()); }
    }

    private void cargarTarifas() {
        try { tblTarifas.setItems(FXCollections.observableArrayList(PlanTarifaDAO.obtenerTodos())); }
        catch (Exception e) { mostrarError("Error cargando tarifas", e.getMessage()); }
    }

    private void cargarPreciosDia() {
        try {
            LocalDate desde = dpDiaDesde.getValue() != null ? dpDiaDesde.getValue() : LocalDate.now();
            LocalDate hasta = dpDiaHasta.getValue() != null ? dpDiaHasta.getValue() : LocalDate.now().plusDays(30);
            tblPreciosDia.setItems(FXCollections.observableArrayList(TarifaDiariaDAO.obtenerPorRango(desde, hasta)));
        } catch (Exception e) { mostrarError("Error cargando precios por día", e.getMessage()); }
    }

    private void cargarPromociones() {
        try { tblPromociones.setItems(FXCollections.observableArrayList(PromocionDAO.obtenerTodas())); }
        catch (Exception e) { mostrarError("Error cargando promociones", e.getMessage()); }
    }

    private void cargarPaquetes() {
        try { tblPaquetes.setItems(FXCollections.observableArrayList(PaqueteDAO.obtenerTodos())); }
        catch (Exception e) { mostrarError("Error cargando paquetes", e.getMessage()); }
    }

    // ────────────────────────────────────────────────────────────────────────────
    // ACCIONES: TEMPORADAS
    // ────────────────────────────────────────────────────────────────────────────
    @FXML private void guardarTemporada() {
        try {
            String nombre = txtTempNombre.getText().trim();
            LocalDate ini  = dpTempInicio.getValue();
            LocalDate fin  = dpTempFin.getValue();
            String multStr = txtTempMultipl.getText().trim();
            if (nombre.isEmpty() || ini == null || fin == null || multStr.isEmpty()) {
                mostrarAviso("Rellena todos los campos."); return;
            }
            BigDecimal mult = new BigDecimal(multStr);
            if (selectedSeason != null && selectedSeason.getId() > 0) {
                selectedSeason.setNombre(nombre); selectedSeason.setFechaInicio(ini);
                selectedSeason.setFechaFin(fin);  selectedSeason.setMultiplicador(mult);
                TemporadaDAO.actualizar(selectedSeason);
            } else {
                Temporada s = new Temporada(0, nombre, ini, fin, mult);
                TemporadaDAO.guardar(s);
            }
            limpiarTemporada(); cargarTemporadas(); recargarTemporadasCombo();
        } catch (Exception e) { mostrarError("Error guardando temporada", e.getMessage()); }
    }

    @FXML private void eliminarTemporada() {
        if (selectedSeason == null) { mostrarAviso("Selecciona una temporada."); return; }
        try { TemporadaDAO.eliminar(selectedSeason.getId()); limpiarTemporada(); cargarTemporadas(); recargarTemporadasCombo(); }
        catch (Exception e) { mostrarError("Error eliminando temporada", e.getMessage()); }
    }

    @FXML private void limpiarTemporada() {
        selectedSeason = null;
        txtTempNombre.clear(); dpTempInicio.setValue(null);
        dpTempFin.setValue(null); txtTempMultipl.clear();
        tblTemporadas.getSelectionModel().clearSelection();
    }

    // ────────────────────────────────────────────────────────────────────────────
    // ACCIONES: TARIFAS
    // ────────────────────────────────────────────────────────────────────────────
    @FXML private void guardarTarifa() {
        try {
            TipoHabitacion tipo = cmbTarTipo.getValue();
            String nombre = txtTarNombre.getText().trim();
            String precioStr = txtTarPrecio.getText().trim();
            if (tipo == null || precioStr.isEmpty()) { mostrarAviso("Tipo de habitación y precio son obligatorios."); return; }
            BigDecimal precio = new BigDecimal(precioStr);
            int minN = spnTarMin.getValue();
            int maxN = spnTarMax.getValue();

            if (selectedRatePlan != null && selectedRatePlan.getId() > 0) {
                selectedRatePlan.setTipoHabitacion(tipo);
                selectedRatePlan.setTemporada(cmbTarTemporada.getValue());
                selectedRatePlan.setNombre(nombre.isEmpty() ? null : nombre);
                selectedRatePlan.setPrecioPorNoche(precio);
                selectedRatePlan.setNochesMinimas(minN);
                selectedRatePlan.setNochesMaximas(maxN > 0 ? maxN : null);
                selectedRatePlan.setIncluyeDesayuno(chkTarDesayuno.isSelected());
                PlanTarifaDAO.actualizar(selectedRatePlan);
            } else {
                PlanTarifa rp = new PlanTarifa();
                rp.setTipoHabitacion(tipo); rp.setTemporada(cmbTarTemporada.getValue());
                rp.setNombre(nombre.isEmpty() ? null : nombre);
                rp.setPrecioPorNoche(precio); rp.setNochesMinimas(minN);
                rp.setNochesMaximas(maxN > 0 ? maxN : null);
                rp.setIncluyeDesayuno(chkTarDesayuno.isSelected());
                PlanTarifaDAO.guardar(rp);
            }
            limpiarTarifa(); cargarTarifas();
        } catch (Exception e) { mostrarError("Error guardando tarifa", e.getMessage()); }
    }

    @FXML private void eliminarTarifa() {
        if (selectedRatePlan == null) { mostrarAviso("Selecciona una tarifa."); return; }
        try { PlanTarifaDAO.eliminar(selectedRatePlan.getId()); limpiarTarifa(); cargarTarifas(); }
        catch (Exception e) { mostrarError("Error eliminando tarifa", e.getMessage()); }
    }

    @FXML private void limpiarTarifa() {
        selectedRatePlan = null;
        cmbTarTipo.setValue(null); cmbTarTemporada.setValue(null);
        txtTarNombre.clear(); txtTarPrecio.clear();
        spnTarMin.getValueFactory().setValue(1); spnTarMax.getValueFactory().setValue(0);
        chkTarDesayuno.setSelected(false);
        tblTarifas.getSelectionModel().clearSelection();
    }

    // ────────────────────────────────────────────────────────────────────────────
    // ACCIONES: PRECIOS POR DÍA
    // ────────────────────────────────────────────────────────────────────────────
    @FXML private void filtrarPreciosDia() { cargarPreciosDia(); }

    @FXML private void guardarPrecioDia() {
        try {
            TipoHabitacion tipo = cmbDiaTipo.getValue();
            LocalDate fecha = dpDiaFecha.getValue();
            String precioStr = txtDiaPrecio.getText().trim();
            if (tipo == null || fecha == null || precioStr.isEmpty()) {
                mostrarAviso("Tipo, fecha y precio son obligatorios."); return;
            }
            TarifaDiaria dr = new TarifaDiaria();
            dr.setTipoHabitacion(tipo); dr.setFecha(fecha);
            dr.setPrecio(new BigDecimal(precioStr));
            dr.setNotas(txtDiaNota.getText().trim().isEmpty() ? null : txtDiaNota.getText().trim());
            TarifaDiariaDAO.guardar(dr);
            limpiarPrecioDia(); cargarPreciosDia();
        } catch (Exception e) { mostrarError("Error guardando precio por día", e.getMessage()); }
    }

    @FXML private void eliminarPrecioDia() {
        if (selectedDailyRate == null) { mostrarAviso("Selecciona un registro."); return; }
        try { TarifaDiariaDAO.eliminar(selectedDailyRate.getId()); limpiarPrecioDia(); cargarPreciosDia(); }
        catch (Exception e) { mostrarError("Error eliminando precio por día", e.getMessage()); }
    }

    @FXML private void limpiarPrecioDia() {
        selectedDailyRate = null;
        cmbDiaTipo.setValue(null); dpDiaFecha.setValue(null);
        txtDiaPrecio.clear(); txtDiaNota.clear();
        tblPreciosDia.getSelectionModel().clearSelection();
    }

    // ────────────────────────────────────────────────────────────────────────────
    // ACCIONES: PROMOCIONES
    // ────────────────────────────────────────────────────────────────────────────
    @FXML private void guardarPromocion() {
        try {
            String nombre = txtPromoNombre.getText().trim();
            String codigo = txtPromoCodigo.getText().trim().toUpperCase();
            String valorStr = txtPromoValor.getText().trim();
            LocalDate ini = dpPromoInicio.getValue();
            LocalDate fin = dpPromoFin.getValue();
            if (nombre.isEmpty() || codigo.isEmpty() || valorStr.isEmpty() || ini == null || fin == null) {
                mostrarAviso("Nombre, código, valor y fechas son obligatorios."); return;
            }
            String tipo = cmbPromoTipo.getSelectionModel().getSelectedIndex() == 0 ? "PERCENT" : "FIXED";
            Promocion p = selectedPromotion != null && selectedPromotion.getId() > 0 ? selectedPromotion : new Promocion();
            p.setNombre(nombre); p.setCodigo(codigo); p.setTipoDescuento(tipo);
            p.setValorDescuento(new BigDecimal(valorStr));
            p.setFechaInicio(ini); p.setFechaFin(fin);
            p.setActivo(chkPromoActivo.isSelected());
            p.setNotas(txtPromoNota.getText().trim().isEmpty() ? null : txtPromoNota.getText().trim());
            if (p.getId() > 0) PromocionDAO.actualizar(p);
            else PromocionDAO.guardar(p);
            limpiarPromocion(); cargarPromociones();
        } catch (Exception e) { mostrarError("Error guardando promoción", e.getMessage()); }
    }

    @FXML private void eliminarPromocion() {
        if (selectedPromotion == null) { mostrarAviso("Selecciona una promoción."); return; }
        try { PromocionDAO.eliminar(selectedPromotion.getId()); limpiarPromocion(); cargarPromociones(); }
        catch (Exception e) { mostrarError("Error eliminando promoción", e.getMessage()); }
    }

    @FXML private void limpiarPromocion() {
        selectedPromotion = null;
        txtPromoNombre.clear(); txtPromoCodigo.clear(); txtPromoValor.clear(); txtPromoNota.clear();
        cmbPromoTipo.getSelectionModel().selectFirst();
        dpPromoInicio.setValue(null); dpPromoFin.setValue(null);
        chkPromoActivo.setSelected(true);
        tblPromociones.getSelectionModel().clearSelection();
    }

    // ────────────────────────────────────────────────────────────────────────────
    // ACCIONES: PAQUETES
    // ────────────────────────────────────────────────────────────────────────────
    @FXML private void guardarPaquete() {
        try {
            String nombre = txtPaqNombre.getText().trim();
            String precioStr = txtPaqPrecio.getText().trim();
            if (nombre.isEmpty() || precioStr.isEmpty()) {
                mostrarAviso("Nombre y precio son obligatorios."); return;
            }
            Paquete pk = selectedPackage != null && selectedPackage.getId() > 0 ? selectedPackage : new Paquete();
            pk.setNombre(nombre);
            pk.setDescripcion(txtPaqDescripcion.getText().trim().isEmpty() ? null : txtPaqDescripcion.getText().trim());
            pk.setPrecioBase(new BigDecimal(precioStr));
            pk.setContenido(txtPaqContenido.getText().trim().isEmpty() ? null : txtPaqContenido.getText().trim());
            pk.setActivo(chkPaqActivo.isSelected());
            if (pk.getId() > 0) PaqueteDAO.actualizar(pk);
            else PaqueteDAO.guardar(pk);
            limpiarPaquete(); cargarPaquetes();
        } catch (Exception e) { mostrarError("Error guardando paquete", e.getMessage()); }
    }

    @FXML private void eliminarPaquete() {
        if (selectedPackage == null) { mostrarAviso("Selecciona un paquete."); return; }
        try { PaqueteDAO.eliminar(selectedPackage.getId()); limpiarPaquete(); cargarPaquetes(); }
        catch (Exception e) { mostrarError("Error eliminando paquete", e.getMessage()); }
    }

    @FXML private void limpiarPaquete() {
        selectedPackage = null;
        txtPaqNombre.clear(); txtPaqDescripcion.clear(); txtPaqPrecio.clear();
        txtPaqContenido.clear(); chkPaqActivo.setSelected(true);
        tblPaquetes.getSelectionModel().clearSelection();
    }

    // ────────────────────────────────────────────────────────────────────────────
    // YIELD MANAGEMENT
    // ────────────────────────────────────────────────────────────────────────────
    @FXML public void calcularYield() {
        try {
            int dias = parseSafe(txtYieldDias.getText(), 30);
            double bajaUmbral = parseSafeDouble(txtYieldBajaUmbral.getText(), 50.0);
            double bajaDesc   = parseSafeDouble(txtYieldBajaDesc.getText(),   10.0);
            double altaUmbral  = parseSafeDouble(txtYieldAltaUmbral.getText(),  80.0);
            double altaPremium = parseSafeDouble(txtYieldAltaPremium.getText(), 20.0);

            // Precio base promedio de todos los tipos
            List<TipoHabitacion> tipos = getRoomTypes();
            double precioBase = tipos.stream()
                    .mapToDouble(rt -> rt.getPrecioBase().doubleValue())
                    .average().orElse(100.0);

            // Total habitaciones
            int totalHabs = contarTotalHabitaciones();

            ObservableList<String[]> rows = FXCollections.observableArrayList();
            LocalDate today = LocalDate.now();

            for (int i = 0; i < dias; i++) {
                LocalDate fecha = today.plusDays(i);
                int ocupadas = contarHabitacionesOcupadasEn(fecha);
                double pct = totalHabs > 0 ? (double) ocupadas / totalHabs * 100.0 : 0;
                int libres = totalHabs - ocupadas;

                String ajuste;
                double precioFinal;
                if (pct < bajaUmbral) {
                    double desc = precioBase * bajaDesc / 100.0;
                    precioFinal = precioBase - desc;
                    ajuste = String.format(Locale.US, "-%.1f%%", bajaDesc);
                } else if (pct > altaUmbral) {
                    double prem = precioBase * altaPremium / 100.0;
                    precioFinal = precioBase + prem;
                    ajuste = String.format(Locale.US, "+%.1f%%", altaPremium);
                } else {
                    precioFinal = precioBase;
                    ajuste = "—";
                }

                rows.add(new String[]{
                        fecha.format(FMT),
                        String.format(Locale.US, "%.1f%%", pct),
                        String.valueOf(libres),
                        String.format(Locale.US, "%.2f €", precioBase),
                        ajuste,
                        String.format(Locale.US, "%.2f €", precioFinal)
                });
            }
            tblYield.setItems(rows);
        } catch (Exception e) { mostrarError("Error calculando yield", e.getMessage()); }
    }

    @FXML private void aplicarYield() {
        ObservableList<String[]> items = tblYield.getItems();
        if (items.isEmpty()) { mostrarAviso("Primero calcula el yield."); return; }
        try {
            List<TipoHabitacion> tipos = getRoomTypes();
            int aplicados = 0;
            for (String[] row : items) {
                String ajuste = row[4];
                if ("—".equals(ajuste)) continue;
                LocalDate fecha = LocalDate.parse(row[0], FMT);
                double precioFinal = Double.parseDouble(row[5].replace(" €", "").replace(",", "."));
                for (TipoHabitacion rt : tipos) {
                    // Ajustar proporcionalmente respecto al precio base del tipo
                    double factor = precioFinal / tipos.stream().mapToDouble(t -> t.getPrecioBase().doubleValue()).average().orElse(100.0);
                    double precioPorTipo = rt.getPrecioBase().doubleValue() * factor;
                    TarifaDiaria dr = new TarifaDiaria();
                    dr.setTipoHabitacion(rt); dr.setFecha(fecha);
                    dr.setPrecio(BigDecimal.valueOf(precioPorTipo).setScale(2, RoundingMode.HALF_UP));
                    dr.setNotas("Yield auto");
                    TarifaDiariaDAO.guardar(dr);
                    aplicados++;
                }
            }
            mostrarInfo("Yield aplicado", aplicados + " precios diarios generados.");
        } catch (Exception e) { mostrarError("Error aplicando yield", e.getMessage()); }
    }

    // ────────────────────────────────────────────────────────────────────────────
    // HELPERS
    // ────────────────────────────────────────────────────────────────────────────
    private List<TipoHabitacion> getRoomTypes() throws SQLException {
        String sql = "SELECT id, code, name, max_occupancy, base_price FROM room_types ORDER BY name";
        java.util.List<TipoHabitacion> list = new java.util.ArrayList<>();
        try (Connection conn = GestorBD.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                TipoHabitacion rt = new TipoHabitacion();
                rt.setId(rs.getInt("id")); rt.setCodigo(rs.getString("code"));
                rt.setNombre(rs.getString("name")); rt.setCapacidadMaxima(rs.getInt("max_occupancy"));
                rt.setPrecioBase(rs.getBigDecimal("base_price"));
                list.add(rt);
            }
        }
        return list;
    }

    private int contarTotalHabitaciones() throws SQLException {
        String sql = "SELECT COUNT(*) FROM rooms";
        try (Connection conn = GestorBD.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            return rs.next() ? rs.getInt(1) : 0;
        }
    }

    private int contarHabitacionesOcupadasEn(LocalDate fecha) throws SQLException {
        String sql = """
                SELECT COUNT(DISTINCT room_id) FROM reservations
                WHERE check_in_date <= ? AND check_out_date > ?
                  AND status NOT IN ('CANCELLED','NO_SHOW')
                """;
        try (Connection conn = GestorBD.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setDate(1, Date.valueOf(fecha));
            ps.setDate(2, Date.valueOf(fecha));
            ResultSet rs = ps.executeQuery();
            return rs.next() ? rs.getInt(1) : 0;
        }
    }

    private int parseSafe(String s, int def) {
        try { return Integer.parseInt(s.trim()); } catch (Exception e) { return def; }
    }

    private double parseSafeDouble(String s, double def) {
        try { return Double.parseDouble(s.trim()); } catch (Exception e) { return def; }
    }

    private void mostrarAviso(String msg) {
        new Alert(Alert.AlertType.WARNING, msg, ButtonType.OK).showAndWait();
    }

    private void mostrarError(String titulo, String msg) {
        Alert a = new Alert(Alert.AlertType.ERROR);
        a.setTitle(titulo); a.setContentText(msg); a.showAndWait();
    }

    private void mostrarInfo(String titulo, String msg) {
        Alert a = new Alert(Alert.AlertType.INFORMATION);
        a.setTitle(titulo); a.setContentText(msg); a.showAndWait();
    }
}

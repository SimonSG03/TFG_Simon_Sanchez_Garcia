package es.simonsg.pmsuite.controller;

import es.simonsg.pmsuite.dao.*;
import es.simonsg.pmsuite.model.*;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.util.StringConverter;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class MaestrosController {

    private IndexController indexController;

    public void setIndexController(IndexController ic) { this.indexController = ic; }

    // Paneles
    @FXML private VBox pnlHotel;
    @FXML private VBox pnlTiposHab;
    @FXML private VBox pnlHabs;
    @FXML private VBox pnlUsuarios;
    @FXML private VBox pnlTemporadas;

    private List<VBox> allPanels;

    // Sidebar buttons
    @FXML private Button btnNavHotel;
    @FXML private Button btnNavTiposHab;
    @FXML private Button btnNavHabs;
    @FXML private Button btnNavUsuarios;
    @FXML private Button btnNavTemporadas;

    // PANEL HOTEL

    @FXML private TextField txtHotelNombre;
    @FXML private TextField txtHotelDireccion;
    @FXML private TextField txtHotelCiudad;
    @FXML private TextField txtHotelCp;
    @FXML private TextField txtHotelPais;
    @FXML private TextField txtHotelTelefono;
    @FXML private TextField txtHotelEmail;
    @FXML private TextField txtHotelWeb;
    @FXML private TextField txtHotelNif;
    @FXML private TextField txtHotelIban;

    // PANEL TIPOS HABITACIÓN

    @FXML private TableView<TipoHabitacion>             tblTiposHab;
    @FXML private TableColumn<TipoHabitacion, String>   colThCod;
    @FXML private TableColumn<TipoHabitacion, String>   colThNombre;
    @FXML private TableColumn<TipoHabitacion, String>   colThCapacidad;
    @FXML private TableColumn<TipoHabitacion, String>   colThPrecio;
    @FXML private VBox                            vboxThForm;
    @FXML private TextField                       txtThCod;
    @FXML private TextField                       txtThNombre;
    @FXML private TextArea                        txtaThDesc;
    @FXML private TextField                       txtThCapacidad;
    @FXML private TextField                       txtThPrecio;
    @FXML private TextArea                        txtaThAmenities;
    @FXML private Button                          btnThEliminar;
    private TipoHabitacion tipoHabEdicion = null;

    // PANEL HABITACIONES

    @FXML private TableView<Habitacion>             tblHabs;
    @FXML private TableColumn<Habitacion, String>   colHabNum;
    @FXML private TableColumn<Habitacion, String>   colHabPlanta;
    @FXML private TableColumn<Habitacion, String>   colHabTipo;
    @FXML private TableColumn<Habitacion, String>   colHabEstado;
    @FXML private VBox                        vboxHabForm;
    @FXML private TextField                   txtHabNum;
    @FXML private TextField                   txtHabPlanta;
    @FXML private ComboBox<TipoHabitacion>          cmbHabTipo;
    @FXML private ComboBox<Habitacion.Estado>       cmbHabEstado;
    @FXML private TextArea                    txtaHabNotas;
    @FXML private Button                      btnHabEliminar;
    private Habitacion habEdicion = null;

    // PANEL USUARIOS

    @FXML private TableView<Usuario>             tblUsuarios;
    @FXML private TableColumn<Usuario, String>   colUsrNombre;
    @FXML private TableColumn<Usuario, String>   colUsrUsername;
    @FXML private TableColumn<Usuario, String>   colUsrRol;
    @FXML private TableColumn<Usuario, String>   colUsrActivo;
    @FXML private VBox                        vboxUsrForm;
    @FXML private TextField                   txtUsrNombre;
    @FXML private TextField                   txtUsrUsername;
    @FXML private TextField                   txtUsrEmail;
    @FXML private PasswordField               txtUsrPassword;
    @FXML private ComboBox<Usuario.Rol>         cmbUsrRol;
    @FXML private CheckBox                    chkUsrActivo;
    @FXML private Button                      btnUsrEliminar;
    private Usuario usuarioEdicion = null;

    // PANEL TEMPORADAS

    @FXML private TableView<Temporada>           tblTemporadas;
    @FXML private TableColumn<Temporada, String> colTmpNombre;
    @FXML private TableColumn<Temporada, String> colTmpDesde;
    @FXML private TableColumn<Temporada, String> colTmpHasta;
    @FXML private TableColumn<Temporada, String> colTmpMult;
    @FXML private VBox                        vboxTmpForm;
    @FXML private TextField                   txtTmpNombre;
    @FXML private DatePicker                  dpTmpDesde;
    @FXML private DatePicker                  dpTmpHasta;
    @FXML private TextField                   txtTmpMult;
    @FXML private Button                      btnTmpEliminar;
    private Temporada tmpEdicion = null;

    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy");


    // INICIALIZACIÓN

    @FXML
    public void initialize() {
        allPanels = List.of(pnlHotel, pnlTiposHab, pnlHabs, pnlUsuarios, pnlTemporadas);

        setupTablaTiposHab();
        setupTablaHabs();
        setupTablaUsuarios();
        setupTablaTemporadas();

        // Abrir Hotel por defecto
        navHotel();
    }

    // Navegación
    @FXML private void navHotel()      { mostrarPanel(pnlHotel,      btnNavHotel);      cargarHotel(); }
    @FXML private void navTiposHab()   { mostrarPanel(pnlTiposHab,   btnNavTiposHab);   cargarTiposHab(); }
    @FXML private void navHabs()       { mostrarPanel(pnlHabs,        btnNavHabs);      cargarHabs(); }
    @FXML private void navUsuarios()   { mostrarPanel(pnlUsuarios,   btnNavUsuarios);   cargarUsuarios(); }
    @FXML private void navTemporadas() { mostrarPanel(pnlTemporadas, btnNavTemporadas); cargarTemporadas(); }
    @FXML private void volverInicio()  { if (indexController != null) indexController.mostrarInicio(); }

    private void mostrarPanel(VBox panel, Button btn) {
        for (VBox p : allPanels) { p.setVisible(false); p.setManaged(false); }
        panel.setVisible(true); panel.setManaged(true);
        for (Button b : new Button[]{btnNavHotel, btnNavTiposHab, btnNavHabs, btnNavUsuarios, btnNavTemporadas})
            b.getStyleClass().remove("maestros-nav-active");
        if (btn != null) btn.getStyleClass().add("maestros-nav-active");
    }

    // PANEL 1 — HOTEL

    private void cargarHotel() {
        try {
            Hotel h = HotelDAO.get();
            txtHotelNombre.setText(nvl(h.getNombre()));
            txtHotelDireccion.setText(nvl(h.getDomicilio()));
            txtHotelCiudad.setText(nvl(h.getCiudad()));
            txtHotelCp.setText(nvl(h.getCodigoPostal()));
            txtHotelPais.setText(nvl(h.getPais()));
            txtHotelTelefono.setText(nvl(h.getTelefono()));
            txtHotelEmail.setText(nvl(h.getEmail()));
            txtHotelWeb.setText(nvl(h.getSitioWeb()));
            txtHotelNif.setText(nvl(h.getNif()));
            txtHotelIban.setText(nvl(h.getIban()));
        } catch (Exception e) { error("Error cargando hotel", e); }
    }

    @FXML private void guardarHotel() {
        try {
            Hotel h = HotelDAO.get();
            h.setNombre(txtHotelNombre.getText().trim());
            h.setDomicilio(txtHotelDireccion.getText().trim());
            h.setCiudad(txtHotelCiudad.getText().trim());
            h.setCodigoPostal(txtHotelCp.getText().trim());
            h.setPais(txtHotelPais.getText().trim());
            h.setTelefono(txtHotelTelefono.getText().trim());
            h.setEmail(txtHotelEmail.getText().trim());
            h.setSitioWeb(txtHotelWeb.getText().trim());
            h.setNif(txtHotelNif.getText().trim());
            h.setIban(txtHotelIban.getText().trim());
            HotelDAO.guardar(h);
            info("Configuración guardada", "Los datos del hotel se han guardado correctamente.");
        } catch (Exception e) { error("Error guardando hotel", e); }
    }

    // PANEL 2 — TIPOS DE HABITACIÓN

    private void setupTablaTiposHab() {
        colThCod.setCellValueFactory(d -> sp(d.getValue().getCodigo()));
        colThNombre.setCellValueFactory(d -> sp(d.getValue().getNombre()));
        colThCapacidad.setCellValueFactory(d -> sp(String.valueOf(d.getValue().getCapacidadMaxima())));
        colThPrecio.setCellValueFactory(d -> sp(d.getValue().getPrecioBase() != null
                ? String.format("%.2f €", d.getValue().getPrecioBase()) : "—"));

        tblTiposHab.getSelectionModel().selectedItemProperty()
                .addListener((obs, old, sel) -> { if (sel != null) editarTipoHab(sel); });
    }

    private void cargarTiposHab() {
        try { tblTiposHab.setItems(FXCollections.observableArrayList(TipoHabitacionDAO.obtenerTodos())); }
        catch (Exception e) { error("Error cargando tipos", e); }
    }

    @FXML private void nuevoTipoHab() {
        tipoHabEdicion = null;
        limpiarFormTipoHab();
        mostrarFormTipoHab(true);
        tblTiposHab.getSelectionModel().clearSelection();
    }

    private void editarTipoHab(TipoHabitacion rt) {
        tipoHabEdicion = rt;
        txtThCod.setText(nvl(rt.getCodigo()));
        txtThNombre.setText(nvl(rt.getNombre()));
        txtaThDesc.setText(nvl(rt.getDescripcion()));
        txtThCapacidad.setText(String.valueOf(rt.getCapacidadMaxima()));
        txtThPrecio.setText(rt.getPrecioBase() != null ? rt.getPrecioBase().toPlainString() : "");
        txtaThAmenities.setText(nvl(rt.getAmenidades()));
        mostrarFormTipoHab(true);
        btnThEliminar.setVisible(true); btnThEliminar.setManaged(true);
    }

    @FXML private void guardarTipoHab() {
        String cod = txtThCod.getText().trim();
        String nom = txtThNombre.getText().trim();
        if (cod.isEmpty() || nom.isEmpty()) { warn("Código y nombre son obligatorios."); return; }
        try {
            TipoHabitacion rt = tipoHabEdicion != null ? tipoHabEdicion : new TipoHabitacion();
            rt.setCodigo(cod); rt.setNombre(nom);
            rt.setDescripcion(txtaThDesc.getText().trim());
            rt.setCapacidadMaxima(intVal(txtThCapacidad.getText(), 2));
            rt.setPrecioBase(decVal(txtThPrecio.getText()));
            rt.setAmenidades(txtaThAmenities.getText().trim());
            if (tipoHabEdicion == null) TipoHabitacionDAO.crear(rt);
            else TipoHabitacionDAO.actualizar(rt);
            cargarTiposHab();
            cancelarFormTipoHab();
        } catch (Exception e) { error("Error guardando tipo", e); }
    }

    @FXML private void eliminarTipoHab() {
        if (tipoHabEdicion == null) return;
        if (!confirm("¿Eliminar el tipo \"" + tipoHabEdicion.getNombre() + "\"?\n" +
                "Las habitaciones de este tipo quedarán sin tipo asignado.")) return;
        try { TipoHabitacionDAO.eliminar(tipoHabEdicion.getId()); cargarTiposHab(); cancelarFormTipoHab(); }
        catch (Exception e) { error("Error eliminando tipo", e); }
    }

    @FXML private void cancelarFormTipoHab() { tipoHabEdicion = null; limpiarFormTipoHab(); mostrarFormTipoHab(false); }

    private void limpiarFormTipoHab() {
        txtThCod.clear(); txtThNombre.clear(); txtaThDesc.clear();
        txtThCapacidad.clear(); txtThPrecio.clear(); txtaThAmenities.clear();
        btnThEliminar.setVisible(false); btnThEliminar.setManaged(false);
    }

    private void mostrarFormTipoHab(boolean v) { vboxThForm.setVisible(v); vboxThForm.setManaged(v); }


    // PANEL 3 — HABITACIONES

    private void setupTablaHabs() {
        colHabNum.setCellValueFactory(d -> sp(d.getValue().getNumero()));
        colHabPlanta.setCellValueFactory(d -> sp(String.valueOf(d.getValue().getPlanta())));
        colHabTipo.setCellValueFactory(d -> sp(d.getValue().getTipoHabitacion() != null
                ? d.getValue().getTipoHabitacion().getNombre() : "—"));
        colHabEstado.setCellValueFactory(d -> sp(estadoLabel(d.getValue().getEstado())));

        cmbHabTipo.setConverter(new StringConverter<>() {
            @Override public String toString(TipoHabitacion rt) { return rt != null ? rt.getNombre() : "—"; }
            @Override public TipoHabitacion fromString(String s) { return null; }
        });
        cmbHabEstado.setItems(FXCollections.observableArrayList(Habitacion.Estado.values()));
        cmbHabEstado.setConverter(new StringConverter<>() {
            @Override public String toString(Habitacion.Estado s) { return s != null ? estadoLabel(s) : ""; }
            @Override public Habitacion.Estado fromString(String s) { return null; }
        });

        tblHabs.getSelectionModel().selectedItemProperty()
                .addListener((obs, old, sel) -> { if (sel != null) editarHab(sel); });
    }

    private void cargarHabs() {
        try {
            tblHabs.setItems(FXCollections.observableArrayList(HabitacionDAO.obtenerTodas()));
            cmbHabTipo.setItems(FXCollections.observableArrayList(TipoHabitacionDAO.obtenerTodos()));
        } catch (Exception e) { error("Error cargando habitaciones", e); }
    }

    @FXML private void nuevaHab() {
        habEdicion = null; limpiarFormHab(); mostrarFormHab(true);
        tblHabs.getSelectionModel().clearSelection();
    }

    private void editarHab(Habitacion r) {
        habEdicion = r;
        txtHabNum.setText(nvl(r.getNumero()));
        txtHabPlanta.setText(String.valueOf(r.getPlanta()));
        cmbHabTipo.setValue(r.getTipoHabitacion());
        cmbHabEstado.setValue(r.getEstado());
        txtaHabNotas.setText(nvl(r.getNotas()));
        mostrarFormHab(true);
        btnHabEliminar.setVisible(true); btnHabEliminar.setManaged(true);
    }

    @FXML private void guardarHab() {
        String num = txtHabNum.getText().trim();
        if (num.isEmpty()) { warn("El número de habitación es obligatorio."); return; }
        try {
            Habitacion r = habEdicion != null ? habEdicion : new Habitacion();
            r.setNumero(num);
            r.setPlanta(intVal(txtHabPlanta.getText(), 1));
            r.setTipoHabitacion(cmbHabTipo.getValue());
            r.setEstado(cmbHabEstado.getValue() != null ? cmbHabEstado.getValue() : Habitacion.Estado.DISPONIBLE);
            r.setNotas(txtaHabNotas.getText().trim());
            if (habEdicion == null) HabitacionDAO.crear(r); else HabitacionDAO.actualizar(r);
            cargarHabs(); cancelarFormHab();
        } catch (Exception e) { error("Error guardando habitación", e); }
    }

    @FXML private void eliminarHab() {
        if (habEdicion == null) return;
        if (!confirm("¿Eliminar la habitación " + habEdicion.getNumero() + "?\n" +
                "Se eliminarán también las reservas asociadas.")) return;
        try { HabitacionDAO.eliminar(habEdicion.getId()); cargarHabs(); cancelarFormHab(); }
        catch (Exception e) { error("Error eliminando habitación", e); }
    }

    @FXML private void cancelarFormHab() { habEdicion = null; limpiarFormHab(); mostrarFormHab(false); }

    private void limpiarFormHab() {
        txtHabNum.clear(); txtHabPlanta.clear(); txtaHabNotas.clear();
        cmbHabTipo.setValue(null); cmbHabEstado.setValue(null);
        btnHabEliminar.setVisible(false); btnHabEliminar.setManaged(false);
    }

    private void mostrarFormHab(boolean v) { vboxHabForm.setVisible(v); vboxHabForm.setManaged(v); }


    // PANEL 4 — USUARIOS

    private void setupTablaUsuarios() {
        colUsrNombre.setCellValueFactory(d -> sp(d.getValue().getNombreCompleto()));
        colUsrUsername.setCellValueFactory(d -> sp(d.getValue().getNombreUsuario()));
        colUsrRol.setCellValueFactory(d -> sp(rolLabel(d.getValue().getRol())));
        colUsrActivo.setCellValueFactory(d -> sp(d.getValue().isActivo() ? "✓ Activo" : "✗ Inactivo"));

        cmbUsrRol.setItems(FXCollections.observableArrayList(Usuario.Rol.values()));
        cmbUsrRol.setConverter(new StringConverter<>() {
            @Override public String toString(Usuario.Rol r) { return r != null ? rolLabel(r) : ""; }
            @Override public Usuario.Rol fromString(String s) { return null; }
        });

        tblUsuarios.getSelectionModel().selectedItemProperty()
                .addListener((obs, old, sel) -> { if (sel != null) editarUsuario(sel); });
    }

    private void cargarUsuarios() {
        try { tblUsuarios.setItems(FXCollections.observableArrayList(UsuarioDAO.obtenerTodos())); }
        catch (Exception e) { error("Error cargando usuarios", e); }
    }

    @FXML private void nuevoUsuario() {
        usuarioEdicion = null; limpiarFormUsr(); mostrarFormUsr(true);
        tblUsuarios.getSelectionModel().clearSelection();
    }

    private void editarUsuario(Usuario u) {
        usuarioEdicion = u;
        txtUsrNombre.setText(nvl(u.getNombreCompleto()));
        txtUsrUsername.setText(nvl(u.getNombreUsuario()));
        txtUsrEmail.setText(nvl(u.getEmail()));
        txtUsrPassword.clear();
        cmbUsrRol.setValue(u.getRol());
        chkUsrActivo.setSelected(u.isActivo());
        mostrarFormUsr(true);
        btnUsrEliminar.setVisible(true); btnUsrEliminar.setManaged(true);
    }

    @FXML private void guardarUsuario() {
        String nombre = txtUsrNombre.getText().trim();
        String username = txtUsrUsername.getText().trim();
        String email = txtUsrEmail.getText().trim();
        if (nombre.isEmpty() || username.isEmpty() || email.isEmpty()) {
            warn("Nombre, usuario y email son obligatorios."); return;
        }
        try {
            if (usuarioEdicion == null) {
                String pwd = txtUsrPassword.getText();
                if (pwd.isEmpty()) { warn("Introduce una contraseña para el nuevo usuario."); return; }
                Usuario u = new Usuario();
                u.setNombreCompleto(nombre); u.setNombreUsuario(username); u.setEmail(email);
                u.setHashContrasena(pwd);
                u.setRol(cmbUsrRol.getValue() != null ? cmbUsrRol.getValue() : Usuario.Rol.RECEPCIONISTA);
                u.setActivo(chkUsrActivo.isSelected());
                UsuarioDAO.crear(u);
            } else {
                usuarioEdicion.setNombreCompleto(nombre); usuarioEdicion.setNombreUsuario(username);
                usuarioEdicion.setEmail(email);
                usuarioEdicion.setRol(cmbUsrRol.getValue() != null ? cmbUsrRol.getValue() : Usuario.Rol.RECEPCIONISTA);
                usuarioEdicion.setActivo(chkUsrActivo.isSelected());
                UsuarioDAO.actualizar(usuarioEdicion);
                String nuevaPwd = txtUsrPassword.getText();
                if (!nuevaPwd.isEmpty()) UsuarioDAO.actualizarContrasena(usuarioEdicion.getId(), nuevaPwd);
            }
            cargarUsuarios(); cancelarFormUsr();
        } catch (Exception e) { error("Error guardando usuario", e); }
    }

    @FXML private void eliminarUsuario() {
        if (usuarioEdicion == null) return;
        if (!confirm("¿Eliminar el usuario \"" + usuarioEdicion.getNombreUsuario() + "\"?")) return;
        try { UsuarioDAO.eliminar(usuarioEdicion.getId()); cargarUsuarios(); cancelarFormUsr(); }
        catch (Exception e) { error("Error eliminando usuario", e); }
    }

    @FXML private void cancelarFormUsr() { usuarioEdicion = null; limpiarFormUsr(); mostrarFormUsr(false); }

    private void limpiarFormUsr() {
        txtUsrNombre.clear(); txtUsrUsername.clear(); txtUsrEmail.clear(); txtUsrPassword.clear();
        cmbUsrRol.setValue(null); chkUsrActivo.setSelected(true);
        btnUsrEliminar.setVisible(false); btnUsrEliminar.setManaged(false);
    }

    private void mostrarFormUsr(boolean v) { vboxUsrForm.setVisible(v); vboxUsrForm.setManaged(v); }


    // PANEL 5 — TEMPORADAS

    private void setupTablaTemporadas() {
        colTmpNombre.setCellValueFactory(d -> sp(d.getValue().getNombre()));
        colTmpDesde.setCellValueFactory(d -> sp(d.getValue().getFechaInicio() != null
                ? d.getValue().getFechaInicio().format(FMT) : "—"));
        colTmpHasta.setCellValueFactory(d -> sp(d.getValue().getFechaFin() != null
                ? d.getValue().getFechaFin().format(FMT) : "—"));
        colTmpMult.setCellValueFactory(d -> sp(d.getValue().getMultiplicador() != null
                ? "×" + d.getValue().getMultiplicador().toPlainString() : "—"));

        tblTemporadas.getSelectionModel().selectedItemProperty()
                .addListener((obs, old, sel) -> { if (sel != null) editarTemporada(sel); });
    }

    private void cargarTemporadas() {
        try { tblTemporadas.setItems(FXCollections.observableArrayList(TemporadaDAO.obtenerTodas())); }
        catch (Exception e) { error("Error cargando temporadas", e); }
    }

    @FXML private void nuevaTemporada() {
        tmpEdicion = null; limpiarFormTmp(); mostrarFormTmp(true);
        tblTemporadas.getSelectionModel().clearSelection();
    }

    private void editarTemporada(Temporada s) {
        tmpEdicion = s;
        txtTmpNombre.setText(nvl(s.getNombre()));
        dpTmpDesde.setValue(s.getFechaInicio());
        dpTmpHasta.setValue(s.getFechaFin());
        txtTmpMult.setText(s.getMultiplicador() != null ? s.getMultiplicador().toPlainString() : "1.00");
        mostrarFormTmp(true);
        btnTmpEliminar.setVisible(true); btnTmpEliminar.setManaged(true);
    }

    @FXML private void guardarTemporada() {
        String nombre = txtTmpNombre.getText().trim();
        LocalDate desde = dpTmpDesde.getValue();
        LocalDate hasta = dpTmpHasta.getValue();
        if (nombre.isEmpty() || desde == null || hasta == null) {
            warn("Nombre, fecha inicio y fin son obligatorios."); return;
        }
        if (!hasta.isAfter(desde)) { warn("La fecha de fin debe ser posterior a la de inicio."); return; }
        try {
            Temporada s = tmpEdicion != null ? tmpEdicion : new Temporada();
            s.setNombre(nombre); s.setFechaInicio(desde); s.setFechaFin(hasta);
            s.setMultiplicador(decVal(txtTmpMult.getText()));
            if (tmpEdicion == null) TemporadaDAO.guardar(s); else TemporadaDAO.actualizar(s);
            cargarTemporadas(); cancelarFormTmp();
        } catch (Exception e) { error("Error guardando temporada", e); }
    }

    @FXML private void eliminarTemporada() {
        if (tmpEdicion == null) return;
        if (!confirm("¿Eliminar la temporada \"" + tmpEdicion.getNombre() + "\"?")) return;
        try { TemporadaDAO.eliminar(tmpEdicion.getId()); cargarTemporadas(); cancelarFormTmp(); }
        catch (Exception e) { error("Error eliminando temporada", e); }
    }

    @FXML private void cancelarFormTmp() { tmpEdicion = null; limpiarFormTmp(); mostrarFormTmp(false); }

    private void limpiarFormTmp() {
        txtTmpNombre.clear(); dpTmpDesde.setValue(null); dpTmpHasta.setValue(null); txtTmpMult.clear();
        btnTmpEliminar.setVisible(false); btnTmpEliminar.setManaged(false);
    }

    private void mostrarFormTmp(boolean v) { vboxTmpForm.setVisible(v); vboxTmpForm.setManaged(v); }

    // UTILIDADES

    private SimpleStringProperty sp(String s) { return new SimpleStringProperty(s != null ? s : ""); }
    private String nvl(String s) { return s != null ? s : ""; }

    private int intVal(String s, int def) {
        try { return Integer.parseInt(s.trim()); } catch (Exception e) { return def; }
    }

    private BigDecimal decVal(String s) {
        try { return new BigDecimal(s.trim().replace(",", ".")); }
        catch (Exception e) { return BigDecimal.ZERO; }
    }

    private String estadoLabel(Habitacion.Estado s) {
        if (s == null) return "";
        return switch (s) {
            case DISPONIBLE      -> "Disponible";
            case OCUPADA       -> "Ocupada";
            case FUERA_SERVICIO -> "Fuera de servicio";
            case BLOQUEADA        -> "Bloqueada";
            case LIMPIEZA       -> "Limpieza";
        };
    }

    private String rolLabel(Usuario.Rol r) {
        if (r == null) return "";
        return switch (r) {
            case ADMIN          -> "Administrador";
            case GERENTE        -> "Director";
            case RECEPCIONISTA   -> "Recepcionista";
            case LIMPIEZA    -> "Ama de llaves";
            case MANTENIMIENTO    -> "Mantenimiento";
            case RESTAURACION   -> "Restauración";
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

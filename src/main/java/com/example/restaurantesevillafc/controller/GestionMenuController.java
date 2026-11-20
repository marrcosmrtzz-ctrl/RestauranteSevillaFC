package com.example.restaurantesevillafc.controller;

import com.example.restaurantesevillafc.model.Plato;
import com.example.restaurantesevillafc.service.FotoService;
import com.example.restaurantesevillafc.service.PlatoService;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.ListChangeListener;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;

import java.io.File;
import java.text.NumberFormat;
import java.util.Locale;

public class GestionMenuController {
    @FXML private TableView<Plato> tablaPlatos;
    @FXML private TableColumn<Plato, Plato> colPlato;
    @FXML private TableColumn<Plato, String> colCategoria;
    @FXML private TableColumn<Plato, Number> colPrecio;
    @FXML private TableColumn<Plato, Boolean> colEstado;
    @FXML private Label lblTotal, lblResumen, lblModo, lblError;
    @FXML private Button btnModificar, btnEliminar, btnDeshacer, btnRehacer;
    @FXML private Tooltip tipDeshacer, tipRehacer;
    @FXML private TextField txtNombre, txtPrecio;
    @FXML private ComboBox<String> cmbCategoria;
    @FXML private TextArea txtDescripcion;
    @FXML private CheckBox chkDisponible;
    @FXML private ImageView imgFoto;
    @FXML private Label lblSinFoto;
    @FXML private Button btnFoto;

    private final PlatoService platoService = new PlatoService();
    private final FotoService fotoService = new FotoService();
    // Foto que ha elegido el trabajador y que se guarda al pulsar Guardar (null si no ha elegido ninguna)
    private File fotoElegida;
    private final NumberFormat formatoPrecio = NumberFormat.getCurrencyInstance(Locale.of("es", "ES"));
    // Plato que se esta editando en el formulario (null si es uno nuevo)
    private Plato platoEditando;

    @FXML public void initialize() {
        configurarColumnas();
        tablaPlatos.setItems(platoService.listarPlatos());
        cmbCategoria.getItems().setAll(PlatoService.CATEGORIAS);

        // Modificar y eliminar solo se pueden usar con un plato seleccionado
        btnModificar.disableProperty().bind(tablaPlatos.getSelectionModel().selectedItemProperty().isNull());
        btnEliminar.disableProperty().bind(tablaPlatos.getSelectionModel().selectedItemProperty().isNull());
        // Deshacer y rehacer solo se pueden pulsar si hay algo que deshacer o rehacer
        btnDeshacer.disableProperty().bind(PlatoService.puedeDeshacerProperty().not());
        btnRehacer.disableProperty().bind(PlatoService.puedeRehacerProperty().not());
        PlatoService.puedeDeshacerProperty().addListener((obs, a, b) -> actualizarTextosHistorial());
        PlatoService.puedeRehacerProperty().addListener((obs, a, b) -> actualizarTextosHistorial());
        actualizarTextosHistorial();

        // Al seleccionar un plato se cargan sus datos en el formulario
        tablaPlatos.getSelectionModel().selectedItemProperty().addListener((obs, antes, ahora) -> {
            if (ahora != null) cargarEnFormulario(ahora);
        });

        // El mensaje de error se quita cuando el usuario corrige algo
        txtNombre.textProperty().addListener((obs, a, b) -> ocultarError());
        txtPrecio.textProperty().addListener((obs, a, b) -> ocultarError());
        cmbCategoria.valueProperty().addListener((obs, a, b) -> ocultarError());

        tablaPlatos.getItems().addListener((ListChangeListener<Plato>) c -> actualizarResumen());
        limpiar();
        actualizarResumen();
    }

    @FXML private void nuevoPlato() {
        tablaPlatos.getSelectionModel().clearSelection();
        limpiar();
        txtNombre.requestFocus();
    }

    @FXML private void modificarPlato() {
        Plato seleccionado = tablaPlatos.getSelectionModel().getSelectedItem();
        if (seleccionado == null) return;
        cargarEnFormulario(seleccionado);
        txtNombre.requestFocus();
    }

    @FXML private void eliminarPlato() {
        Plato seleccionado = tablaPlatos.getSelectionModel().getSelectedItem();
        if (seleccionado == null) return;
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION,
                "Se va a eliminar \"" + seleccionado.getNombre() + "\" de la carta.\nSi te equivocas puedes deshacerlo con Ctrl+Z.",
                ButtonType.YES, ButtonType.NO);
        confirm.setHeaderText("¿Eliminar este plato?");
        confirm.showAndWait().filter(b -> b == ButtonType.YES).ifPresent(b -> {
            try {
                // La foto no se borra para que siga ahí si se deshace el borrado
                platoService.eliminar(seleccionado);
                actualizarTextosHistorial();
                tablaPlatos.getSelectionModel().clearSelection();
                limpiar();
            } catch (Exception e) {
                mostrarError(e.getMessage());
            }
        });
    }

    @FXML private void guardar() {
        try {
            if (platoEditando == null) {
                Plato nuevo = platoService.crear(txtNombre.getText(), txtDescripcion.getText(), cmbCategoria.getValue(), txtPrecio.getText(), chkDisponible.isSelected());
                // La foto se guarda antes de seleccionar el plato, porque al seleccionarlo se vacia la foto elegida
                guardarFotoElegida(nuevo);
                tablaPlatos.refresh();
                tablaPlatos.getSelectionModel().select(nuevo);
                tablaPlatos.scrollTo(nuevo);
            } else {
                platoService.modificar(platoEditando, txtNombre.getText(), txtDescripcion.getText(), cmbCategoria.getValue(), txtPrecio.getText(), chkDisponible.isSelected());
                guardarFotoElegida(platoEditando);
                tablaPlatos.refresh();
                cargarEnFormulario(platoEditando);
            }
            actualizarResumen();
            actualizarTextosHistorial();
        } catch (Exception e) {
            mostrarError(e.getMessage());
        }
    }

    @FXML private void elegirFoto() {
        FileChooser selector = new FileChooser();
        selector.setTitle("Elegir foto del plato");
        selector.getExtensionFilters().add(new FileChooser.ExtensionFilter("Imágenes (JPG, PNG)", "*.jpg", "*.jpeg", "*.png"));
        File archivo = selector.showOpenDialog(imgFoto.getScene().getWindow());
        if (archivo == null) return;
        try {
            fotoService.validar(archivo);
            fotoElegida = archivo;
            mostrarFoto(new Image(archivo.toURI().toString()));
            ocultarError();
        } catch (Exception e) {
            mostrarError(e.getMessage());
        }
    }

    private void guardarFotoElegida(Plato plato) throws Exception {
        if (fotoElegida == null) return;
        fotoService.guardar(plato, fotoElegida);
        fotoElegida = null;
    }

    private void mostrarFoto(Image foto) {
        imgFoto.setImage(foto);
        lblSinFoto.setVisible(foto == null);
        btnFoto.setText(foto == null ? "📷 Elegir foto" : "📷 Cambiar foto");
    }

    private void mostrarError(String mensaje) {
        lblError.setText(mensaje);
        lblError.setVisible(true);
        lblError.setManaged(true);
    }

    @FXML private void limpiar() {
        platoEditando = null;
        txtNombre.clear();
        txtPrecio.clear();
        txtDescripcion.clear();
        cmbCategoria.setValue(null);
        chkDisponible.setSelected(true);
        fotoElegida = null;
        mostrarFoto(null);
        lblModo.setText("Nuevo");
        ocultarError();
    }

    private void cargarEnFormulario(Plato plato) {
        platoEditando = plato;
        txtNombre.setText(plato.getNombre());
        txtDescripcion.setText(plato.getDescripcion());
        cmbCategoria.setValue(plato.getCategoria());
        txtPrecio.setText(String.format(Locale.of("es", "ES"), "%.2f", plato.getPrecio()));
        chkDisponible.setSelected(plato.isDisponible());
        fotoElegida = null;
        mostrarFoto(fotoService.cargar(plato, false));
        lblModo.setText("● Editando");
        ocultarError();
    }

    private void ocultarError() {
        lblError.setVisible(false);
        lblError.setManaged(false);
    }

    // Deshace el ultimo cambio de la carta (Ctrl+Z) y selecciona el plato para que se vea lo que ha cambiado
    @FXML public void deshacer() {
        try {
            mostrarCambio(platoService.deshacer());
        } catch (Exception e) {
            mostrarError(e.getMessage());
        }
    }

    @FXML public void rehacer() {
        try {
            mostrarCambio(platoService.rehacer());
        } catch (Exception e) {
            mostrarError(e.getMessage());
        }
    }

    private void mostrarCambio(Plato plato) {
        tablaPlatos.refresh();
        actualizarResumen();
        actualizarTextosHistorial();
        if (tablaPlatos.getItems().contains(plato)) {
            tablaPlatos.getSelectionModel().select(plato);
            tablaPlatos.scrollTo(plato);
            cargarEnFormulario(plato);
        } else {
            tablaPlatos.getSelectionModel().clearSelection();
            limpiar();
        }
    }

    // El aviso de los botones dice que se va a deshacer, por ejemplo "Deshacer: Eliminar Salmorejo (Ctrl+Z)"
    private void actualizarTextosHistorial() {
        String d = platoService.textoDeshacer();
        String r = platoService.textoRehacer();
        tipDeshacer.setText(d.isEmpty() ? "Deshacer (Ctrl+Z)" : "Deshacer: " + d + " (Ctrl+Z)");
        tipRehacer.setText(r.isEmpty() ? "Rehacer (Ctrl+Y)" : "Rehacer: " + r + " (Ctrl+Y)");
    }

    private void actualizarResumen() {
        int total = tablaPlatos.getItems().size();
        long disponibles = platoService.contarDisponibles();
        lblTotal.setText("Mostrando " + total + " platos");
        lblResumen.setText(disponibles + " disponibles · " + (total - disponibles) + " agotados");
    }

    private void configurarColumnas() {
        // Columna plato: nombre en negrita y la descripcion debajo
        colPlato.setCellValueFactory(c -> new SimpleObjectProperty<>(c.getValue()));
        colPlato.setCellFactory(col -> new TableCell<>() {
            @Override protected void updateItem(Plato plato, boolean vacia) {
                super.updateItem(plato, vacia);
                if (vacia || plato == null) { setGraphic(null); return; }
                Label nombre = new Label(plato.getNombre());
                nombre.getStyleClass().add("plato-nombre");
                Label descripcion = new Label(plato.getDescripcion());
                descripcion.getStyleClass().add("plato-descripcion");
                // Foto pequeña a la izquierda, si el plato no tiene foto se deja el hueco gris
                StackPane hueco = new StackPane();
                hueco.getStyleClass().add("hueco-foto-mini");
                Image foto = fotoService.cargar(plato, true);
                if (foto != null) {
                    ImageView mini = new ImageView(foto);
                    mini.setFitWidth(40);
                    mini.setFitHeight(40);
                    hueco.getChildren().add(mini);
                }
                HBox fila = new HBox(10, hueco, new VBox(2, nombre, descripcion));
                fila.setAlignment(Pos.CENTER_LEFT);
                setGraphic(fila);
            }
        });

        colCategoria.setCellValueFactory(c -> c.getValue().categoriaProperty());
        colCategoria.setCellFactory(col -> new TableCell<>() {
            @Override protected void updateItem(String categoria, boolean vacia) {
                super.updateItem(categoria, vacia);
                if (vacia || categoria == null) { setGraphic(null); return; }
                Label etiqueta = new Label(categoria);
                etiqueta.getStyleClass().add("etiqueta-categoria");
                setGraphic(etiqueta);
            }
        });

        colPrecio.setCellValueFactory(c -> c.getValue().precioProperty());
        colPrecio.setCellFactory(col -> new TableCell<>() {
            @Override protected void updateItem(Number precio, boolean vacia) {
                super.updateItem(precio, vacia);
                setText(vacia || precio == null ? null : formatoPrecio.format(precio.doubleValue()));
            }
        });

        colEstado.setCellValueFactory(c -> c.getValue().disponibleProperty());
        colEstado.setCellFactory(col -> new TableCell<>() {
            @Override protected void updateItem(Boolean disponible, boolean vacia) {
                super.updateItem(disponible, vacia);
                if (vacia || disponible == null) { setGraphic(null); return; }
                Label estado = new Label(disponible ? "● Disponible" : "● Agotado");
                estado.getStyleClass().add(disponible ? "estado-disponible" : "estado-agotado");
                setGraphic(estado);
            }
        });
    }
}

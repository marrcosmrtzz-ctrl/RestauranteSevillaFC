package com.example.restaurantesevillafc.controller;

import com.example.restaurantesevillafc.model.Plato;
import com.example.restaurantesevillafc.service.PlatoService;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.ListChangeListener;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;

import java.text.NumberFormat;
import java.util.Locale;

public class GestionMenuController {
    @FXML private TableView<Plato> tablaPlatos;
    @FXML private TableColumn<Plato, Plato> colPlato;
    @FXML private TableColumn<Plato, String> colCategoria;
    @FXML private TableColumn<Plato, Number> colPrecio;
    @FXML private TableColumn<Plato, Boolean> colEstado;
    @FXML private Label lblTotal, lblResumen, lblModo, lblError;
    @FXML private Button btnModificar, btnEliminar;
    @FXML private TextField txtNombre, txtPrecio;
    @FXML private ComboBox<String> cmbCategoria;
    @FXML private TextArea txtDescripcion;
    @FXML private CheckBox chkDisponible;

    private final PlatoService platoService = new PlatoService();
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
                "Se va a eliminar \"" + seleccionado.getNombre() + "\" de la carta. Esta acción no se puede deshacer.",
                ButtonType.YES, ButtonType.NO);
        confirm.setHeaderText("¿Eliminar este plato?");
        confirm.showAndWait().filter(b -> b == ButtonType.YES).ifPresent(b -> {
            platoService.eliminar(seleccionado);
            tablaPlatos.getSelectionModel().clearSelection();
            limpiar();
        });
    }

    @FXML private void guardar() {
        try {
            if (platoEditando == null) {
                Plato nuevo = platoService.crear(txtNombre.getText(), txtDescripcion.getText(), cmbCategoria.getValue(), txtPrecio.getText(), chkDisponible.isSelected());
                tablaPlatos.getSelectionModel().select(nuevo);
                tablaPlatos.scrollTo(nuevo);
            } else {
                platoService.modificar(platoEditando, txtNombre.getText(), txtDescripcion.getText(), cmbCategoria.getValue(), txtPrecio.getText(), chkDisponible.isSelected());
                tablaPlatos.refresh();
                cargarEnFormulario(platoEditando);
            }
            actualizarResumen();
        } catch (Exception e) {
            lblError.setText(e.getMessage());
            lblError.setVisible(true);
            lblError.setManaged(true);
        }
    }

    @FXML private void limpiar() {
        platoEditando = null;
        txtNombre.clear();
        txtPrecio.clear();
        txtDescripcion.clear();
        cmbCategoria.setValue(null);
        chkDisponible.setSelected(true);
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
        lblModo.setText("● Editando");
        ocultarError();
    }

    private void ocultarError() {
        lblError.setVisible(false);
        lblError.setManaged(false);
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
                setGraphic(new VBox(2, nombre, descripcion));
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

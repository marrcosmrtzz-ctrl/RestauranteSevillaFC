package com.example.restaurantesevillafc.controller;

import com.example.restaurantesevillafc.model.Plato;
import com.example.restaurantesevillafc.service.PlatoService;
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
    @FXML private Label lblTotal, lblResumen;

    private final PlatoService platoService = new PlatoService();
    private final NumberFormat formatoPrecio = NumberFormat.getCurrencyInstance(Locale.of("es", "ES"));

    @FXML public void initialize() {
        // Columna plato: nombre en negrita y la descripcion debajo
        colPlato.setCellValueFactory(c -> new javafx.beans.property.SimpleObjectProperty<>(c.getValue()));
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

        tablaPlatos.setItems(platoService.listarPlatos());
        actualizarResumen();
    }

    private void actualizarResumen() {
        int total = tablaPlatos.getItems().size();
        lblTotal.setText("Mostrando " + total + " platos");
        lblResumen.setText(platoService.contarDisponibles() + " disponibles · " + (total - platoService.contarDisponibles()) + " agotados");
    }
}

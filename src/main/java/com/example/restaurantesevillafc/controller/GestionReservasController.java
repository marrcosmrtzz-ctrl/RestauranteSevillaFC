package com.example.restaurantesevillafc.controller;

import com.example.restaurantesevillafc.model.Reserva;
import com.example.restaurantesevillafc.service.GestionReservasService;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.ListChangeListener;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.util.StringConverter;

import java.time.format.DateTimeFormatter;
import java.util.Comparator;

public class GestionReservasController {
    @FXML private Button btnConfirmar, btnCancelar;
    @FXML private Label lblHoy, lblPendientes, lblConfirmadas, lblCanceladas, lblObservaciones, lblError;
    @FXML private DatePicker dpFecha;
    @FXML private ComboBox<String> cmbEstado;
    @FXML private TextField txtBuscar;
    @FXML private TableView<Reserva> tablaReservas;
    @FXML private TableColumn<Reserva, Number> colId, colPersonas;
    @FXML private TableColumn<Reserva, Reserva> colCliente, colFecha, colHora;
    @FXML private TableColumn<Reserva, String> colEstado;

    private static final DateTimeFormatter FECHA = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DateTimeFormatter HORA = DateTimeFormatter.ofPattern("HH:mm");
    private final GestionReservasService reservasService = new GestionReservasService();
    private FilteredList<Reserva> filtradas;

    @FXML public void initialize() {
        configurarColumnas();
        // Ordenadas por dia y hora, las mas proximas arriba
        javafx.collections.ObservableList<Reserva> reservas = reservasService.listarReservas();
        filtradas = new FilteredList<>(reservas);
        SortedList<Reserva> ordenadas = new SortedList<>(filtradas,
                Comparator.comparing(Reserva::getFecha).thenComparing(Reserva::getHora));
        tablaReservas.setItems(ordenadas);
        tablaReservas.setPlaceholder(new Label("No hay reservas con esos filtros"));

        // null en el desplegable significa "Todos los estados"
        cmbEstado.getItems().add(null);
        cmbEstado.getItems().addAll(GestionReservasService.ESTADOS);
        cmbEstado.setConverter(new StringConverter<>() {
            @Override public String toString(String estado) { return GestionReservasService.textoEstado(estado); }
            @Override public String fromString(String texto) { return null; }
        });
        cmbEstado.setButtonCell(new ListCell<>() {
            @Override protected void updateItem(String estado, boolean vacia) {
                super.updateItem(estado, vacia);
                setText(GestionReservasService.textoEstado(estado));
            }
        });
        dpFecha.setConverter(new StringConverter<>() {
            @Override public String toString(java.time.LocalDate f) { return f == null ? "" : f.format(FECHA); }
            @Override public java.time.LocalDate fromString(String t) {
                try { return t == null || t.isBlank() ? null : java.time.LocalDate.parse(t.trim(), FECHA); } catch (Exception e) { return null; }
            }
        });

        // Confirmar y cancelar solo con una reserva seleccionada
        btnConfirmar.disableProperty().bind(tablaReservas.getSelectionModel().selectedItemProperty().isNull());
        btnCancelar.disableProperty().bind(tablaReservas.getSelectionModel().selectedItemProperty().isNull());
        tablaReservas.getSelectionModel().selectedItemProperty().addListener((obs, antes, ahora) -> mostrarObservaciones(ahora));

        // Si un cliente hace una reserva nueva se actualizan los numeros
        reservas.addListener((ListChangeListener<Reserva>) c -> actualizarResumen());
        filtrar();
        actualizarResumen();
    }

    @FXML private void filtrar() {
        java.time.LocalDate fecha = dpFecha.getValue();
        String estado = cmbEstado.getValue();
        String texto = txtBuscar.getText();
        filtradas.setPredicate(r -> reservasService.coincide(r, fecha, estado, texto));
        ocultarError();
    }

    @FXML private void limpiarFiltros() {
        dpFecha.setValue(null);
        cmbEstado.setValue(null);
        txtBuscar.clear();
        filtrar();
    }

    @FXML private void confirmar() {
        Reserva r = tablaReservas.getSelectionModel().getSelectedItem();
        cambiarEstado(r, true);
    }

    @FXML private void cancelar() {
        Reserva r = tablaReservas.getSelectionModel().getSelectedItem();
        cambiarEstado(r, false);
    }

    private void cambiarEstado(Reserva r, boolean confirmar) {
        if (r == null) return;
        String texto = "Reserva #" + r.getId() + " de " + reservasService.nombreCliente(r) + "\n"
                + r.getFecha().format(FECHA) + " a las " + r.getHora().format(HORA) + " · " + r.getNumPersonas() + " personas";
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION, texto, ButtonType.YES, ButtonType.NO);
        confirm.setHeaderText(confirmar ? "¿Confirmar esta reserva?" : "¿Cancelar esta reserva? El cliente la verá como cancelada.");
        confirm.showAndWait().filter(b -> b == ButtonType.YES).ifPresent(b -> {
            try {
                if (confirmar) reservasService.confirmar(r); else reservasService.cancelar(r);
                filtrar();
                tablaReservas.refresh();
                actualizarResumen();
                tablaReservas.getSelectionModel().select(r);
            } catch (Exception e) {
                mostrarError(e.getMessage());
            }
        });
    }

    private void actualizarResumen() {
        lblHoy.setText(String.valueOf(reservasService.contarHoy()));
        lblPendientes.setText(String.valueOf(reservasService.contar("PENDIENTE")));
        lblConfirmadas.setText(String.valueOf(reservasService.contar("CONFIRMADA")));
        lblCanceladas.setText(String.valueOf(reservasService.contar("CANCELADA")));
    }

    private void mostrarObservaciones(Reserva r) {
        ocultarError();
        if (r == null) {
            lblObservaciones.setText("Selecciona una reserva para ver sus observaciones.");
        } else if (r.getObservaciones() == null || r.getObservaciones().isBlank()) {
            lblObservaciones.setText("Reserva #" + r.getId() + ": sin observaciones.");
        } else {
            lblObservaciones.setText("Reserva #" + r.getId() + ": " + r.getObservaciones());
        }
    }

    private void mostrarError(String mensaje) {
        lblError.setText(mensaje);
        lblError.setVisible(true);
        lblError.setManaged(true);
    }

    private void ocultarError() {
        lblError.setVisible(false);
        lblError.setManaged(false);
    }

    private void configurarColumnas() {
        colId.setCellValueFactory(c -> c.getValue().idProperty());
        colId.setCellFactory(col -> new TableCell<>() {
            @Override protected void updateItem(Number id, boolean vacia) {
                super.updateItem(id, vacia);
                setText(vacia || id == null ? null : "#" + id);
            }
        });

        // Cliente: circulo con la inicial, nombre y correo debajo
        colCliente.setCellValueFactory(c -> new SimpleObjectProperty<>(c.getValue()));
        colCliente.setCellFactory(col -> new TableCell<>() {
            @Override protected void updateItem(Reserva r, boolean vacia) {
                super.updateItem(r, vacia);
                if (vacia || r == null) { setGraphic(null); return; }
                String nombre = reservasService.nombreCliente(r);
                Label inicial = new Label(nombre.substring(0, 1).toUpperCase());
                inicial.getStyleClass().add("inicial-cliente");
                StackPane circulo = new StackPane(inicial);
                circulo.getStyleClass().add("circulo-cliente");
                Label lblNombre = new Label(nombre);
                lblNombre.getStyleClass().add("plato-nombre");
                Label contacto = new Label(reservasService.contactoCliente(r));
                contacto.getStyleClass().add("plato-descripcion");
                HBox fila = new HBox(10, circulo, new VBox(2, lblNombre, contacto));
                fila.setAlignment(Pos.CENTER_LEFT);
                setGraphic(fila);
            }
        });

        colFecha.setCellValueFactory(c -> new SimpleObjectProperty<>(c.getValue()));
        colFecha.setCellFactory(col -> new TableCell<>() {
            @Override protected void updateItem(Reserva r, boolean vacia) {
                super.updateItem(r, vacia);
                setText(vacia || r == null ? null : r.getFecha().format(FECHA));
            }
        });

        colHora.setCellValueFactory(c -> new SimpleObjectProperty<>(c.getValue()));
        colHora.setCellFactory(col -> new TableCell<>() {
            @Override protected void updateItem(Reserva r, boolean vacia) {
                super.updateItem(r, vacia);
                setText(vacia || r == null ? null : r.getHora().format(HORA));
            }
        });

        colPersonas.setCellValueFactory(c -> c.getValue().numPersonasProperty());
        colPersonas.setCellFactory(col -> new TableCell<>() {
            @Override protected void updateItem(Number n, boolean vacia) {
                super.updateItem(n, vacia);
                setText(vacia || n == null ? null : "👥 " + n);
            }
        });

        colEstado.setCellValueFactory(c -> c.getValue().estadoProperty());
        colEstado.setCellFactory(col -> new TableCell<>() {
            @Override protected void updateItem(String estado, boolean vacia) {
                super.updateItem(estado, vacia);
                if (vacia || estado == null) { setGraphic(null); return; }
                Label etiqueta = new Label("● " + GestionReservasService.textoEstado(estado));
                etiqueta.getStyleClass().add(switch (estado) {
                    case "PENDIENTE" -> "estado-preparacion";
                    case "CONFIRMADA" -> "estado-disponible";
                    default -> "estado-agotado";
                });
                setGraphic(etiqueta);
            }
        });
    }
}

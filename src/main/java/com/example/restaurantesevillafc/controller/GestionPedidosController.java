package com.example.restaurantesevillafc.controller;

import com.example.restaurantesevillafc.model.LineaPedido;
import com.example.restaurantesevillafc.model.Pedido;
import com.example.restaurantesevillafc.service.FotoService;
import com.example.restaurantesevillafc.service.GestionPedidosService;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.ListChangeListener;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.util.StringConverter;

import java.util.Comparator;

public class GestionPedidosController {
    @FXML private ToggleButton btnEnCurso, btnFinalizados;
    @FXML private ToggleGroup grupoPestanas;
    @FXML private Label lblNumEnCurso, lblNumFinalizados;
    @FXML private TextField txtBuscar;
    @FXML private TableView<Pedido> tablaPedidos;
    @FXML private TableColumn<Pedido, Number> colNumero;
    @FXML private TableColumn<Pedido, Pedido> colCliente, colHora, colTotal;
    @FXML private TableColumn<Pedido, String> colEstado;
    @FXML private Label lblSinSeleccion, lblPedido, lblInfoPedido, lblTotalPedido, lblObservaciones, lblError;
    @FXML private VBox panelDetalle, panelLineas;
    @FXML private HBox panelProgreso;
    @FXML private ComboBox<String> cmbEstado;

    private final GestionPedidosService pedidosService = new GestionPedidosService();
    private final FotoService fotoService = new FotoService();
    private FilteredList<Pedido> filtrados;
    // Lo pone el panel del trabajador para actualizar el numero del menu lateral
    private Runnable alCambiarPedidos = () -> {};

    @FXML public void initialize() {
        configurarColumnas();
        // Los pedidos mas recientes arriba
        javafx.collections.ObservableList<Pedido> pedidos = pedidosService.listarPedidos();
        filtrados = new FilteredList<>(pedidos);
        SortedList<Pedido> ordenados = new SortedList<>(filtrados, Comparator.comparingInt(Pedido::getId).reversed());
        tablaPedidos.setItems(ordenados);
        tablaPedidos.setPlaceholder(new Label("No hay pedidos en esta pestaña"));

        // Siempre tiene que haber una pestaña marcada
        grupoPestanas.selectedToggleProperty().addListener((obs, antes, ahora) -> { if (ahora == null) antes.setSelected(true); });
        txtBuscar.textProperty().addListener((obs, a, b) -> aplicarFiltro());

        cmbEstado.getItems().setAll(GestionPedidosService.ESTADOS);
        cmbEstado.setConverter(new StringConverter<>() {
            @Override public String toString(String estado) { return GestionPedidosService.textoEstado(estado); }
            @Override public String fromString(String texto) { return null; }
        });
        cmbEstado.valueProperty().addListener((obs, a, b) -> ocultarError());

        tablaPedidos.getSelectionModel().selectedItemProperty().addListener((obs, antes, ahora) -> mostrarDetalle(ahora));
        // Si el cliente confirma un pedido nuevo aparece solo
        pedidos.addListener((ListChangeListener<Pedido>) c -> actualizarContadores());

        aplicarFiltro();
        actualizarContadores();
        tablaPedidos.getSelectionModel().selectFirst();
    }

    public void setAlCambiarPedidos(Runnable accion) { this.alCambiarPedidos = accion; }

    @FXML private void cambiarPestana() {
        aplicarFiltro();
        tablaPedidos.getSelectionModel().selectFirst();
    }

    private void aplicarFiltro() {
        boolean verFinalizados = btnFinalizados.isSelected();
        String texto = txtBuscar.getText();
        filtrados.setPredicate(p -> GestionPedidosService.estaFinalizado(p) == verFinalizados && pedidosService.coincide(p, texto));
    }

    private void actualizarContadores() {
        lblNumEnCurso.setText(String.valueOf(pedidosService.contarEnCurso()));
        lblNumFinalizados.setText(String.valueOf(pedidosService.contarFinalizados()));
        alCambiarPedidos.run();
    }

    private void mostrarDetalle(Pedido pedido) {
        ocultarError();
        boolean hay = pedido != null;
        panelDetalle.setVisible(hay);
        panelDetalle.setManaged(hay);
        lblSinSeleccion.setVisible(!hay);
        lblSinSeleccion.setManaged(!hay);
        if (!hay) return;

        lblPedido.setText("Pedido #" + pedido.getId());
        lblInfoPedido.setText(pedidosService.nombreCliente(pedido) + " · " + pedidosService.hora(pedido) + " · " + pedido.getNumPlatos() + " platos");
        lblTotalPedido.setText(String.format("%.2f €", pedido.getTotal()));
        boolean sinObs = pedido.getObservaciones() == null || pedido.getObservaciones().isBlank();
        lblObservaciones.setText(sinObs ? "Sin observaciones" : "ⓘ  " + pedido.getObservaciones());
        lblObservaciones.getStyleClass().removeAll("caja-observaciones", "caja-observaciones-vacia");
        lblObservaciones.getStyleClass().add(sinObs ? "caja-observaciones-vacia" : "caja-observaciones");
        cmbEstado.setValue(pedido.getEstado());
        pintarProgreso(pedido.getEstado());
        pintarLineas(pedido);
    }

    // Los cuatro pasos del pedido con una linea entre ellos, los pasos ya hechos en rojo
    private void pintarProgreso(String estado) {
        panelProgreso.getChildren().clear();
        int actual = GestionPedidosService.ESTADOS.indexOf(estado);
        for (int i = 0; i < GestionPedidosService.ESTADOS.size(); i++) {
            if (i > 0) {
                Region linea = new Region();
                linea.getStyleClass().add(i <= actual ? "linea-hecha" : "linea-paso");
                HBox.setHgrow(linea, Priority.ALWAYS);
                panelProgreso.getChildren().add(linea);
            }
            Label circulo = new Label(i < actual ? "✓" : "");
            circulo.getStyleClass().add(i < actual ? "paso-hecho" : i == actual ? "paso-actual" : "paso");
            Label texto = new Label(GestionPedidosService.textoEstado(GestionPedidosService.ESTADOS.get(i)));
            texto.getStyleClass().add(i == actual ? "texto-paso-actual" : "texto-paso");
            VBox paso = new VBox(4, circulo, texto);
            paso.setAlignment(Pos.TOP_CENTER);
            panelProgreso.getChildren().add(paso);
        }
    }

    private void pintarLineas(Pedido pedido) {
        panelLineas.getChildren().clear();
        for (LineaPedido l : pedido.getLineas()) {
            HBox fila = new HBox(8);
            fila.setAlignment(Pos.CENTER_LEFT);
            // La foto pequeña del plato (la de la carta o la que haya puesto el trabajador)
            Image foto = fotoService.cargar(l.getPlato(), true);
            if (foto != null) {
                ImageView img = new ImageView(foto);
                img.setFitWidth(32);
                img.setFitHeight(32);
                fila.getChildren().add(img);
            }
            Label nombre = new Label(l.getCantidad() + " × " + l.getPlato().getNombre());
            nombre.setWrapText(true);
            Region hueco = new Region();
            HBox.setHgrow(hueco, Priority.ALWAYS);
            Label subtotal = new Label(String.format("%.2f €", l.getSubtotal()));
            fila.getChildren().addAll(nombre, hueco, subtotal);
            panelLineas.getChildren().add(fila);
        }
    }

    @FXML private void cambiarEstado() {
        Pedido pedido = tablaPedidos.getSelectionModel().getSelectedItem();
        String nuevo = cmbEstado.getValue();
        if (pedido == null || nuevo == null || nuevo.equals(pedido.getEstado())) {
            try { pedidosService.cambiarEstado(pedido, nuevo); } catch (Exception e) { mostrarError(e.getMessage()); }
            return;
        }
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION,
                "El pedido #" + pedido.getId() + " pasará de «" + GestionPedidosService.textoEstado(pedido.getEstado()) + "» a «" + GestionPedidosService.textoEstado(nuevo) + "».",
                ButtonType.YES, ButtonType.NO);
        confirm.setHeaderText("¿Cambiar el estado del pedido?");
        confirm.showAndWait().filter(b -> b == ButtonType.YES).ifPresent(b -> {
            try {
                pedidosService.cambiarEstado(pedido, nuevo);
                aplicarFiltro();
                tablaPedidos.refresh();
                actualizarContadores();
                if (GestionPedidosService.estaFinalizado(pedido)) {
                    // Ya no esta en la pestaña En curso
                    tablaPedidos.getSelectionModel().selectFirst();
                } else {
                    tablaPedidos.getSelectionModel().select(pedido);
                    mostrarDetalle(pedido);
                }
            } catch (Exception e) {
                mostrarError(e.getMessage());
            }
        });
    }

    @FXML private void imprimirTicket() {
        Pedido pedido = tablaPedidos.getSelectionModel().getSelectedItem();
        if (pedido == null) return;
        Label ticket = new Label(pedidosService.ticket(pedido));
        ticket.getStyleClass().add("ticket");
        Alert info = new Alert(Alert.AlertType.INFORMATION);
        info.setHeaderText("Ticket del pedido #" + pedido.getId());
        info.getDialogPane().setContent(ticket);
        // El Alert es otra ventana: le ponemos la misma hoja de estilos y la clase del trabajador
        info.getDialogPane().getStylesheets().addAll(tablaPedidos.getScene().getStylesheets());
        info.getDialogPane().getStyleClass().add("pantalla-trabajador");
        info.showAndWait();
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
        colNumero.setCellValueFactory(c -> c.getValue().idProperty());
        colNumero.setCellFactory(col -> new TableCell<>() {
            @Override protected void updateItem(Number id, boolean vacia) {
                super.updateItem(id, vacia);
                setText(vacia || id == null ? null : "#" + id);
                getStyleClass().remove("plato-nombre");
                if (!vacia) getStyleClass().add("plato-nombre");
            }
        });

        colCliente.setCellValueFactory(c -> new SimpleObjectProperty<>(c.getValue()));
        colCliente.setCellFactory(col -> new TableCell<>() {
            @Override protected void updateItem(Pedido p, boolean vacia) {
                super.updateItem(p, vacia);
                if (vacia || p == null) { setGraphic(null); return; }
                Label nombre = new Label(pedidosService.nombreCliente(p));
                nombre.getStyleClass().add("plato-nombre");
                Label platos = new Label(p.getNumPlatos() + (p.getNumPlatos() == 1 ? " plato" : " platos"));
                platos.getStyleClass().add("plato-descripcion");
                setGraphic(new VBox(2, nombre, platos));
            }
        });

        colHora.setCellValueFactory(c -> new SimpleObjectProperty<>(c.getValue()));
        colHora.setCellFactory(col -> new TableCell<>() {
            @Override protected void updateItem(Pedido p, boolean vacia) {
                super.updateItem(p, vacia);
                setText(vacia || p == null ? null : "🕑 " + pedidosService.hora(p));
            }
        });

        colTotal.setCellValueFactory(c -> new SimpleObjectProperty<>(c.getValue()));
        colTotal.setCellFactory(col -> new TableCell<>() {
            @Override protected void updateItem(Pedido p, boolean vacia) {
                super.updateItem(p, vacia);
                setText(vacia || p == null ? null : String.format("%.2f €", p.getTotal()));
            }
        });

        colEstado.setCellValueFactory(c -> c.getValue().estadoProperty());
        colEstado.setCellFactory(col -> new TableCell<>() {
            @Override protected void updateItem(String estado, boolean vacia) {
                super.updateItem(estado, vacia);
                if (vacia || estado == null) { setGraphic(null); return; }
                Label etiqueta = new Label("● " + GestionPedidosService.textoEstado(estado));
                etiqueta.getStyleClass().add(switch (estado) {
                    case "RECIBIDO" -> "estado-recibido";
                    case "EN_PREPARACION" -> "estado-preparacion";
                    case "LISTO" -> "estado-disponible";
                    default -> "estado-entregado";
                });
                setGraphic(etiqueta);
            }
        });
    }
}

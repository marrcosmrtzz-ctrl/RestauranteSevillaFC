package com.example.restaurantesevillafc.controller;

import com.example.restaurantesevillafc.App;
import com.example.restaurantesevillafc.model.Pedido;
import com.example.restaurantesevillafc.model.Usuario;
import com.example.restaurantesevillafc.service.GestionPedidosService;
import com.example.restaurantesevillafc.service.UsuarioService;
import javafx.collections.ListChangeListener;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.layout.StackPane;

import java.io.IOException;

public class TrabajadorController {
    @FXML private StackPane contenido;
    @FXML private ToggleButton btnMenu, btnReservas, btnPedidos;
    @FXML private ToggleGroup grupoSecciones;
    @FXML private Label lblNombreUsuario, lblInicial, lblBadgePedidos;
    private final GestionPedidosService pedidosService = new GestionPedidosService();

    @FXML public void initialize() {
        // Siempre tiene que haber una seccion seleccionada
        grupoSecciones.selectedToggleProperty().addListener((obs, antes, ahora) -> { if (ahora == null) antes.setSelected(true); });
        // El numero rojo de Pedidos son los pedidos que todavia no se han entregado
        pedidosService.listarPedidos().addListener((ListChangeListener<Pedido>) c -> actualizarBadgePedidos());
        actualizarBadgePedidos();
        mostrarMenu();
    }

    private void actualizarBadgePedidos() {
        long enCurso = pedidosService.contarEnCurso();
        lblBadgePedidos.setText(String.valueOf(enCurso));
        lblBadgePedidos.setVisible(enCurso > 0);
    }

    public void setUsuario(Usuario usuario) {
        lblNombreUsuario.setText(usuario.getNombre() + " " + usuario.getApellidos());
        lblInicial.setText(usuario.getNombre().substring(0, 1).toUpperCase());
    }

    @FXML private void mostrarMenu() {
        btnMenu.setSelected(true);
        cargarVista("fxml/gestion_menu.fxml");
    }

    @FXML private void mostrarReservas() {
        btnReservas.setSelected(true);
        cargarVista("fxml/gestion_reservas.fxml");
    }

    @FXML private void mostrarPedidos() {
        btnPedidos.setSelected(true);
        FXMLLoader loader = cargarVista("fxml/gestion_pedidos.fxml");
        if (loader != null) loader.<GestionPedidosController>getController().setAlCambiarPedidos(this::actualizarBadgePedidos);
    }

    // Carga la pantalla en el centro y devuelve el loader para poder usar su controlador
    private FXMLLoader cargarVista(String ruta) {
        try {
            FXMLLoader loader = new FXMLLoader(App.class.getResource(ruta));
            Parent vista = loader.load();
            contenido.getChildren().setAll(vista);
            return loader;
        } catch (IOException e) {
            new Alert(Alert.AlertType.ERROR, "No se ha podido abrir la pantalla: " + e.getMessage()).showAndWait();
            return null;
        }
    }


    @FXML private void cerrarSesion() {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION, "¿Seguro que quieres cerrar sesión?", ButtonType.YES, ButtonType.NO);
        confirm.setHeaderText("Cerrar sesión");
        confirm.showAndWait().filter(b -> b == ButtonType.YES).ifPresent(b -> {
            UsuarioService.cerrarSesion();
            try {
                contenido.getScene().setRoot(FXMLLoader.load(App.class.getResource("fxml/login.fxml")));
            } catch (IOException e) {
                new Alert(Alert.AlertType.ERROR, "No se ha podido volver al inicio de sesión.").showAndWait();
            }
        });
    }

    @FXML private void salir() {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION, "¿Seguro que quieres salir?", ButtonType.YES, ButtonType.NO);
        confirm.setHeaderText("Salir");
        confirm.showAndWait().filter(b -> b == ButtonType.YES).ifPresent(b -> Platform.exit());
    }

    @FXML private void acercaDe() {
        Alert info = new Alert(Alert.AlertType.INFORMATION, "Proyecto de Desarrollo de Interfaces · 2.º DAM\nEquipo Sevilla FC");
        info.setHeaderText("Restaurante Sevilla FC");
        info.showAndWait();
    }
}

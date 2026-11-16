package com.example.restaurantesevillafc.controller;

import com.example.restaurantesevillafc.App;
import com.example.restaurantesevillafc.model.Usuario;
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
    @FXML private Label lblNombreUsuario, lblInicial;

    @FXML public void initialize() {
        // Siempre tiene que haber una seccion seleccionada
        grupoSecciones.selectedToggleProperty().addListener((obs, antes, ahora) -> { if (ahora == null) antes.setSelected(true); });
        mostrarMenu();
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
        mostrarPendiente("Gestión de reservas");
    }

    @FXML private void mostrarPedidos() {
        btnPedidos.setSelected(true);
        mostrarPendiente("Gestión de pedidos");
    }

    private void cargarVista(String ruta) {
        try {
            Parent vista = FXMLLoader.load(App.class.getResource(ruta));
            contenido.getChildren().setAll(vista);
        } catch (IOException e) {
            new Alert(Alert.AlertType.ERROR, "No se ha podido abrir la pantalla.").showAndWait();
        }
    }

    // Reservas y pedidos se haran en los siguientes avances
    private void mostrarPendiente(String titulo) {
        Label aviso = new Label(titulo + "\nEsta pantalla todavía está en desarrollo.");
        aviso.getStyleClass().add("subtitulo");
        contenido.getChildren().setAll(aviso);
    }

    @FXML private void cerrarSesion() {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION, "¿Seguro que quieres cerrar sesión?", ButtonType.YES, ButtonType.NO);
        confirm.setHeaderText("Cerrar sesión");
        confirm.showAndWait().filter(b -> b == ButtonType.YES).ifPresent(b -> {
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

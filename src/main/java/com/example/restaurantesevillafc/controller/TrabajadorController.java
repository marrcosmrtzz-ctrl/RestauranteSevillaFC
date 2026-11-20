package com.example.restaurantesevillafc.controller;

import com.example.restaurantesevillafc.App;
import com.example.restaurantesevillafc.config.Tema;
import com.example.restaurantesevillafc.model.Pedido;
import com.example.restaurantesevillafc.model.Usuario;
import com.example.restaurantesevillafc.service.GestionPedidosService;
import com.example.restaurantesevillafc.service.PlatoService;
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
    @FXML private MenuItem itemDeshacer, itemRehacer;
    @FXML private CheckMenuItem itemTemaOscuro;
    @FXML private MenuItem itemAumentarLetra, itemReducirLetra, itemLetraNormal;
    private final GestionPedidosService pedidosService = new GestionPedidosService();
    // Controlador de la gestion del menu cuando se esta viendo (para deshacer y rehacer)
    private GestionMenuController menuController;

    @FXML public void initialize() {
        // Siempre tiene que haber una seccion seleccionada
        grupoSecciones.selectedToggleProperty().addListener((obs, antes, ahora) -> { if (ahora == null) antes.setSelected(true); });
        // El numero rojo de Pedidos son los pedidos que todavia no se han entregado
        pedidosService.listarPedidos().addListener((ListChangeListener<Pedido>) c -> actualizarBadgePedidos());
        actualizarBadgePedidos();
        // Deshacer y rehacer son de la gestion del menu: solo se pueden usar en esa seccion y si hay algo que deshacer
        itemDeshacer.disableProperty().bind(PlatoService.puedeDeshacerProperty().not().or(btnMenu.selectedProperty().not()));
        itemRehacer.disableProperty().bind(PlatoService.puedeRehacerProperty().not().or(btnMenu.selectedProperty().not()));
        actualizarMenuTema();
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
        FXMLLoader loader = cargarVista("fxml/gestion_menu.fxml");
        menuController = loader == null ? null : loader.getController();
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
            PlatoService.reiniciarHistorial();
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

    @FXML private void deshacer() {
        if (btnMenu.isSelected() && menuController != null) menuController.deshacer();
    }

    @FXML private void rehacer() {
        if (btnMenu.isSelected() && menuController != null) menuController.rehacer();
    }

    // ===== Tema oscuro y tamaño de letra (clase Tema, igual que en las pantallas del cliente) =====
    private void actualizarMenuTema() {
        itemTemaOscuro.setSelected(Tema.isOscuro());
        itemAumentarLetra.setDisable(Tema.esLetraMaxima());
        itemReducirLetra.setDisable(Tema.esLetraMinima());
        itemLetraNormal.setDisable(Tema.esLetraMinima());
    }

    @FXML private void cambiarTema() {
        Tema.cambiarTema(contenido.getScene());
        actualizarMenuTema();
    }

    @FXML private void aumentarLetra() {
        Tema.aumentarLetra(contenido.getScene());
        actualizarMenuTema();
    }

    @FXML private void reducirLetra() {
        Tema.reducirLetra(contenido.getScene());
        actualizarMenuTema();
    }

    @FXML private void letraNormal() {
        Tema.letraNormal(contenido.getScene());
        actualizarMenuTema();
    }

    // Explica en pocos pasos la seccion que se esta viendo, se abre con F1
    @FXML private void verAyuda() {
        String titulo;
        String pasos;
        if (btnReservas.isSelected()) {
            titulo = "Cómo gestionar las reservas";
            pasos = "1. Arriba ves cuántas reservas hay hoy, pendientes, confirmadas y canceladas.\n"
                    + "2. Filtra por día, por estado o busca por cliente, correo o nº de reserva y pulsa «Filtrar».\n"
                    + "3. Selecciona una reserva en la tabla. Debajo salen sus observaciones.\n"
                    + "4. Pulsa «Confirmar» para aceptarla: se le asigna sola una mesa libre con sitio para esas personas.\n"
                    + "5. Pulsa «Cancelar» si no se puede atender. La mesa queda libre otra vez.";
        } else if (btnPedidos.isSelected()) {
            titulo = "Cómo gestionar los pedidos";
            pasos = "1. En «En curso» están los pedidos que hay que preparar y en «Finalizados» los ya entregados.\n"
                    + "2. Busca un pedido por su número o por el nombre del cliente.\n"
                    + "3. Selecciona un pedido para ver sus platos, el total y las observaciones.\n"
                    + "4. Elige el nuevo estado y pulsa «Cambiar estado». Solo se puede avanzar: Recibido → En preparación → Listo → Entregado.\n"
                    + "5. Con «Imprimir ticket» ves el ticket del pedido.";
        } else {
            titulo = "Cómo gestionar el menú";
            pasos = "1. Pulsa «+ Añadir plato», rellena el formulario de la derecha y pulsa «Guardar».\n"
                    + "2. Para cambiar un plato, selecciónalo en la tabla, cambia sus datos y pulsa «Guardar».\n"
                    + "3. Con «📷 Elegir foto» le pones una foto (JPG o PNG de hasta 5 MB).\n"
                    + "4. «Eliminar» lo quita de la carta. Si está en pedidos de clientes, desmarca «Disponible en la carta».\n"
                    + "5. Si te equivocas, deshaz con Ctrl+Z (o ↶) y rehaz con Ctrl+Y (o ↷).";
        }
        Alert ayuda = new Alert(Alert.AlertType.INFORMATION, pasos
                + "\n\nCambia de sección con Ctrl+1, Ctrl+2 y Ctrl+3.\nEn el menú Ver puedes poner el tema oscuro (Ctrl+T) y agrandar o reducir la letra (Ctrl + y Ctrl -).");
        ayuda.setHeaderText(titulo);
        ayuda.setTitle("Ayuda");
        ayuda.getDialogPane().setMinWidth(520);
        ayuda.showAndWait();
    }

    @FXML private void acercaDe() {
        Alert info = new Alert(Alert.AlertType.INFORMATION, "Proyecto de Desarrollo de Interfaces · 2.º DAM\nEquipo Sevilla FC");
        info.setHeaderText("Restaurante Sevilla FC");
        info.showAndWait();
    }
}

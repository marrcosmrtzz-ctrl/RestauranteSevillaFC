package com.example.restaurantesevillafc.controller;
import com.example.restaurantesevillafc.App;
import com.example.restaurantesevillafc.dao.PlatoDAO;
import com.example.restaurantesevillafc.model.Plato;
import com.example.restaurantesevillafc.service.PedidoService;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.layout.FlowPane;
public class CartaController {
    @FXML private FlowPane panelPlatos;
    @FXML private Label lblNumPlatos,lblSinResultados,lblNumTodos,lblNumEntrantes,lblNumPrincipales,lblNumPostres,lblNumBebidas;
    @FXML private TextField txtBuscar;
    @FXML private ToggleGroup grupoCategoria;
    @FXML private ToggleButton btnEntrantes,btnPrincipales,btnPostres,btnBebidas;
    private final PedidoService pedidoService=new PedidoService();

    @FXML public void initialize(){
        // Si vuelve a pulsar la categoria que ya estaba marcada no se desmarca, y cada vez que cambia se filtran los platos
        grupoCategoria.selectedToggleProperty().addListener((obs,antes,ahora)->{
            if(ahora==null)antes.setSelected(true);
            else filtrar();
        });
        // Cada letra que escribe en el buscador vuelve a filtrar los platos
        txtBuscar.textProperty().addListener((obs,antes,ahora)->filtrar());
        contarCategorias();
        filtrar();
        actualizarContador();
        // Si no ha llegado ningun plato es que no se ha podido conectar con MySQL
        if(PlatoDAO.todos().isEmpty()){
            Alert aviso=new Alert(Alert.AlertType.WARNING,"No se han podido cargar los platos.\nComprueba que MySQL está encendido en XAMPP.");
            aviso.setHeaderText("Carta sin platos");
            aviso.showAndWait();
        }
    }
    // Pone al lado de cada categoria cuantos platos tiene
    private void contarCategorias(){
        int entrantes=0,principales=0,postres=0,bebidas=0;
        for(Plato p:PlatoDAO.todos()){
            if(p.getCategoria().equals("Entrantes"))entrantes++;
            else if(p.getCategoria().equals("Principales"))principales++;
            else if(p.getCategoria().equals("Postres"))postres++;
            else if(p.getCategoria().equals("Bebidas"))bebidas++;
        }
        lblNumTodos.setText(String.valueOf(PlatoDAO.todos().size()));
        lblNumEntrantes.setText(String.valueOf(entrantes));
        lblNumPrincipales.setText(String.valueOf(principales));
        lblNumPostres.setText(String.valueOf(postres));
        lblNumBebidas.setText(String.valueOf(bebidas));
    }
    // Vacia el panel y vuelve a poner solo las tarjetas de los platos de la categoria marcada que tengan en el nombre lo que ha escrito
    private void filtrar(){
        String categoria="Todos";
        Toggle marcado=grupoCategoria.getSelectedToggle();
        if(marcado==btnEntrantes)categoria="Entrantes";
        else if(marcado==btnPrincipales)categoria="Principales";
        else if(marcado==btnPostres)categoria="Postres";
        else if(marcado==btnBebidas)categoria="Bebidas";
        String texto=txtBuscar.getText().trim().toLowerCase();
        panelPlatos.getChildren().clear();
        for(Plato p:PlatoDAO.todos()){
            boolean mismaCategoria=categoria.equals("Todos")||p.getCategoria().equals(categoria);
            boolean contieneTexto=p.getNombre().toLowerCase().contains(texto);
            if(mismaCategoria&&contieneTexto)crearTarjeta(p);
        }
        // Si no queda ningun plato se ensena el aviso
        boolean vacio=panelPlatos.getChildren().isEmpty();
        lblSinResultados.setVisible(vacio);
        lblSinResultados.setManaged(vacio);
    }
    // Carga la tarjeta del plato desde su fxml, le pasa el plato y la mete en el panel
    private void crearTarjeta(Plato plato){
        try{
            FXMLLoader loader=new FXMLLoader(App.class.getResource("fxml/tarjeta_plato.fxml"));
            Parent tarjeta=loader.load();
            TarjetaPlatoController controller=loader.getController();
            controller.setDatos(plato,this);
            panelPlatos.getChildren().add(tarjeta);
        }catch(Exception e){
            new Alert(Alert.AlertType.ERROR,"No se pudo cargar el plato "+plato.getNombre()+": "+e.getMessage()).showAndWait();
        }
    }
    // Lo llama la tarjeta cuando se anade un plato, el numero del boton Mi pedido son los platos que lleva el cliente
    public void actualizarContador(){
        lblNumPlatos.setText(String.valueOf(pedidoService.totalArticulos()));
    }
    @FXML private void vaciarPedido(){
        if(pedidoService.getCarrito().isEmpty()){
            new Alert(Alert.AlertType.INFORMATION,"Tu pedido ya está vacío.").showAndWait();
            return;
        }
        Alert confirm=new Alert(Alert.AlertType.CONFIRMATION,"Se quitarán los "+pedidoService.totalArticulos()+" platos de tu pedido.",ButtonType.YES,ButtonType.NO);
        confirm.setHeaderText("¿Vaciar el pedido?");
        confirm.showAndWait().filter(b->b==ButtonType.YES).ifPresent(b->{
            pedidoService.vaciar();
            // Se vuelven a crear las tarjetas para que los botones dejen de estar en verde
            filtrar();
            actualizarContador();
        });
    }
    @FXML private void verMenuDia(){
        Alert info=new Alert(Alert.AlertType.INFORMATION,"Primero, segundo, postre y bebida por 14,90 €.\nLos platos cambian cada día, pregunta en el local cuáles hay hoy.");
        info.setHeaderText("Menú del día");
        info.showAndWait();
    }
    // Cambia lo que se ve en la ventana por otra pantalla, el pedido no se pierde porque esta guardado en PedidoService
    private void abrirPantalla(String fxml){
        try{
            Parent pantalla=FXMLLoader.load(App.class.getResource(fxml));
            panelPlatos.getScene().setRoot(pantalla);
        }catch(Exception e){
            new Alert(Alert.AlertType.ERROR,"No se pudo abrir la pantalla: "+e.getMessage()).showAndWait();
        }
    }
    @FXML private void irAMiPedido(){abrirPantalla("fxml/mi_pedido.fxml");}
    @FXML private void cerrarSesion(){
        String mensaje="¿Seguro que quieres cerrar sesión?";
        if(!pedidoService.getCarrito().isEmpty())mensaje+="\nTienes platos sin confirmar y se perderán.";
        Alert confirm=new Alert(Alert.AlertType.CONFIRMATION,mensaje,ButtonType.YES,ButtonType.NO);
        confirm.setHeaderText("Cerrar sesión");
        confirm.showAndWait().filter(b->b==ButtonType.YES).ifPresent(b->{
            // Al salir se borra el pedido para que el siguiente cliente empiece de cero
            pedidoService.vaciar();
            abrirPantalla("fxml/login.fxml");
        });
    }
    // Reservar mesa se conecta cuando este su pantalla
    @FXML private void pantallaPendiente(){new Alert(Alert.AlertType.INFORMATION,"Esta pantalla todavía no está hecha.").showAndWait();}
    @FXML private void salir(){
        String mensaje="¿Seguro que quieres salir?";
        if(!pedidoService.getCarrito().isEmpty())mensaje+="\nTienes platos sin confirmar y se perderán.";
        Alert confirm=new Alert(Alert.AlertType.CONFIRMATION,mensaje,ButtonType.YES,ButtonType.NO);
        confirm.setHeaderText("Salir");
        confirm.showAndWait().filter(b->b==ButtonType.YES).ifPresent(b->Platform.exit());
    }
    @FXML private void acercaDe(){
        Alert info=new Alert(Alert.AlertType.INFORMATION,"Proyecto de Desarrollo de Interfaces · 2.º DAM\nEquipo Sevilla FC");
        info.setHeaderText("Restaurante Sevilla FC");
        info.showAndWait();
    }
}

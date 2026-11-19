package com.example.restaurantesevillafc.controller;
import com.example.restaurantesevillafc.App;
import com.example.restaurantesevillafc.model.LineaPedido;
import com.example.restaurantesevillafc.model.Pedido;
import com.example.restaurantesevillafc.model.Plato;
import com.example.restaurantesevillafc.model.Usuario;
import com.example.restaurantesevillafc.service.PedidoService;
import com.example.restaurantesevillafc.service.UsuarioService;
import javafx.application.Platform;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import java.net.URL;
public class MiPedidoController {
    @FXML private TableView<LineaPedido> tablaPedido;
    @FXML private TableColumn<LineaPedido,String> colPlato,colPrecio,colSubtotal;
    @FXML private TableColumn<LineaPedido,Number> colCantidad;
    @FXML private TableColumn<LineaPedido,ImageView> colFoto;
    @FXML private Label lblNumPlatos,lblTextoSubtotal,lblSubtotal,lblIva,lblTotal,lblContador,lblSaludo,lblInicial;
    @FXML private Button btnRestar,btnSumar,btnQuitar,btnConfirmar;
    @FXML private TextArea txtObservaciones;
    private final PedidoService pedidoService=new PedidoService();
    private Usuario usuario;

    @FXML public void initialize(){
        // La columna de la foto devuelve la foto pequena del plato y la tabla la pinta sola
        colFoto.setCellValueFactory(d->new SimpleObjectProperty<>(fotoPequena(d.getValue().getPlato())));
        colPlato.setCellValueFactory(d->new SimpleStringProperty(d.getValue().getPlato().getNombre()+"\n"+d.getValue().getPlato().getDescripcion()));
        colPrecio.setCellValueFactory(d->new SimpleStringProperty(String.format("%.2f €",d.getValue().getPrecioUnidad())));
        colCantidad.setCellValueFactory(d->d.getValue().cantidadProperty());
        colSubtotal.setCellValueFactory(d->new SimpleStringProperty(String.format("%.2f €",d.getValue().getSubtotal())));
        tablaPedido.setItems(pedidoService.getCarrito());
        tablaPedido.setPlaceholder(new Label("Tu pedido está vacío. Añade platos desde la carta."));
        // Los botones del plato solo se pueden usar si hay un plato seleccionado en la tabla
        btnRestar.disableProperty().bind(tablaPedido.getSelectionModel().selectedItemProperty().isNull());
        btnSumar.disableProperty().bind(tablaPedido.getSelectionModel().selectedItemProperty().isNull());
        btnQuitar.disableProperty().bind(tablaPedido.getSelectionModel().selectedItemProperty().isNull());
        // Contador de las observaciones, no deja escribir mas de 250 caracteres
        txtObservaciones.textProperty().addListener((obs,antes,ahora)->{
            if(ahora.length()>PedidoService.MAX_OBSERVACIONES)txtObservaciones.setText(antes);
            else lblContador.setText(ahora.length()+" / "+PedidoService.MAX_OBSERVACIONES);
        });
        // El pedido es del usuario que ha entrado desde el login o el registro
        setUsuario(UsuarioService.getUsuarioConectado());
        tablaPedido.getSelectionModel().selectFirst();
        actualizarResumen();
    }
    // Busca la foto pequena del plato, se llama plato y el id con _mini, si no hay foto la columna se queda vacia
    private ImageView fotoPequena(Plato plato){
        URL foto=App.class.getResource("images/platos/plato"+plato.getId()+"_mini.jpg");
        if(foto==null)return null;
        ImageView imagen=new ImageView(new Image(foto.toExternalForm()));
        imagen.setFitWidth(48);
        imagen.setFitHeight(48);
        return imagen;
    }
    public void setUsuario(Usuario usuario){
        this.usuario=usuario;
        if(usuario!=null){
            lblSaludo.setText("Hola, "+usuario.getNombre());
            lblInicial.setText(usuario.getNombre().substring(0,1));
        }
    }
    // Se llama al abrir la pantalla y cada vez que cambia algo del pedido
    private void actualizarResumen(){
        int articulos=pedidoService.totalArticulos();
        lblNumPlatos.setText(String.valueOf(articulos));
        lblTextoSubtotal.setText("Subtotal ("+articulos+(articulos==1?" artículo)":" artículos)"));
        lblSubtotal.setText(String.format("%.2f €",pedidoService.total()));
        lblIva.setText(String.format("%.2f €",pedidoService.ivaIncluido()));
        lblTotal.setText(String.format("%.2f €",pedidoService.total()));
        btnConfirmar.setDisable(pedidoService.getCarrito().isEmpty());
        tablaPedido.refresh();
    }
    @FXML private void sumar(){
        LineaPedido linea=tablaPedido.getSelectionModel().getSelectedItem(); if(linea==null)return;
        try{
            pedidoService.sumar(linea);
        }catch(Exception e){
            new Alert(Alert.AlertType.WARNING,e.getMessage()).showAndWait();
        }
        actualizarResumen();
    }
    @FXML private void restar(){
        LineaPedido linea=tablaPedido.getSelectionModel().getSelectedItem(); if(linea==null)return;
        // Si solo queda una unidad, restar es quitar el plato (y eso pide confirmacion)
        if(linea.getCantidad()==1){quitar();return;}
        pedidoService.restar(linea);
        actualizarResumen();
    }
    @FXML private void quitar(){
        LineaPedido linea=tablaPedido.getSelectionModel().getSelectedItem(); if(linea==null)return;
        Alert confirm=new Alert(Alert.AlertType.CONFIRMATION,"¿Quitar "+linea.getPlato().getNombre()+" de tu pedido?",ButtonType.YES,ButtonType.NO);
        confirm.setHeaderText("Quitar plato");
        confirm.showAndWait().filter(b->b==ButtonType.YES).ifPresent(b->{
            pedidoService.quitar(linea);
            actualizarResumen();
        });
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
            actualizarResumen();
        });
    }
    @FXML private void confirmar(){
        Alert confirm=new Alert(Alert.AlertType.CONFIRMATION,
                "Vas a enviar a cocina "+pedidoService.totalArticulos()+" platos por "+String.format("%.2f €",pedidoService.total())+".\nUna vez enviado ya no podrás cambiarlo.",
                ButtonType.YES,ButtonType.NO);
        confirm.setHeaderText("¿Confirmar el pedido?");
        confirm.showAndWait().filter(b->b==ButtonType.YES).ifPresent(b->enviarPedido());
    }
    private void enviarPedido(){
        try{
            Pedido pedido=pedidoService.confirmarPedido(usuario,txtObservaciones.getText());
            txtObservaciones.clear();
            actualizarResumen();
            Alert info=new Alert(Alert.AlertType.INFORMATION,"Tu pedido nº "+pedido.getId()+" ya está en cocina.\nTiempo estimado de preparación: 25–30 min.");
            info.setHeaderText("¡Pedido enviado!");
            info.showAndWait();
        }catch(Exception e){
            new Alert(Alert.AlertType.WARNING,e.getMessage()).showAndWait();
        }
    }
    // Cambia lo que se ve en la ventana por otra pantalla, el pedido no se pierde porque esta guardado en PedidoService
    private void abrirPantalla(String fxml){
        try{
            Parent pantalla=FXMLLoader.load(App.class.getResource(fxml));
            tablaPedido.getScene().setRoot(pantalla);
        }catch(Exception e){
            new Alert(Alert.AlertType.ERROR,"No se pudo abrir la pantalla: "+e.getMessage()).showAndWait();
        }
    }
    // Lo usan el boton Carta de arriba, el menu Ver y el boton Seguir comprando
    @FXML private void irACarta(){abrirPantalla("fxml/carta.fxml");}
    @FXML private void cerrarSesion(){
        String mensaje="¿Seguro que quieres cerrar sesión?";
        if(!pedidoService.getCarrito().isEmpty())mensaje+="\nTienes platos sin confirmar y se perderán.";
        Alert confirm=new Alert(Alert.AlertType.CONFIRMATION,mensaje,ButtonType.YES,ButtonType.NO);
        confirm.setHeaderText("Cerrar sesión");
        confirm.showAndWait().filter(b->b==ButtonType.YES).ifPresent(b->{
            // Al salir se borra el pedido para que el siguiente cliente empiece de cero
            pedidoService.vaciar();
            UsuarioService.cerrarSesion();
            abrirPantalla("fxml/login.fxml");
        });
    }
    // Reservar mesa se conecta cuando este su pantalla
    // Abre la pantalla de Reservar mesa (la hace Eduardo)
    @FXML private void irAReservar(){abrirPantalla("fxml/reservar_mesa.fxml");}
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

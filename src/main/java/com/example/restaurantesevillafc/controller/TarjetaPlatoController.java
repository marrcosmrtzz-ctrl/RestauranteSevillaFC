package com.example.restaurantesevillafc.controller;
import com.example.restaurantesevillafc.App;
import com.example.restaurantesevillafc.model.Plato;
import com.example.restaurantesevillafc.service.PedidoService;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import java.net.URL;
// Controlador de cada tarjeta de la carta, hay una tarjeta por plato
public class TarjetaPlatoController {
    @FXML private ImageView imgPlato;
    @FXML private Label lblNombre,lblDescripcion,lblAlergenos,lblPrecio;
    @FXML private Button btnAnadir;
    private final PedidoService pedidoService=new PedidoService();
    private Plato plato;
    private CartaController carta;

    // La carta le pasa el plato que tiene que mostrar y se pasa ella misma para avisarla cuando se anade algo
    public void setDatos(Plato plato,CartaController carta){
        this.plato=plato;
        this.carta=carta;
        lblNombre.setText(plato.getNombre());
        lblDescripcion.setText(plato.getDescripcion());
        lblAlergenos.setText(plato.getAlergenos()==null?"Sin alérgenos":plato.getAlergenos().replace(", "," · "));
        lblPrecio.setText(String.format("%.2f €",plato.getPrecio()));
        // La foto se llama plato y el id del plato, si no hay foto se queda el hueco gris
        URL foto=App.class.getResource("images/platos/plato"+plato.getId()+".jpg");
        if(foto!=null)imgPlato.setImage(new Image(foto.toExternalForm()));
        if(!plato.isDisponible()){
            btnAnadir.setText("No disponible");
            btnAnadir.setDisable(true);
        }
        actualizarBoton();
    }
    @FXML private void anadir(){
        try{
            pedidoService.agregarPlato(plato);
            actualizarBoton();
            carta.actualizarContador();
        }catch(Exception e){
            new Alert(Alert.AlertType.WARNING,e.getMessage()).showAndWait();
        }
    }
    // Si el plato ya esta en el pedido el boton se pone verde y dice cuantos lleva
    private void actualizarBoton(){
        int cantidad=pedidoService.cantidadDe(plato);
        if(cantidad==0)return;
        btnAnadir.setText(cantidad==1?"✓  Añadido":"✓  Añadido x"+cantidad);
        if(!btnAnadir.getStyleClass().contains("boton-anadido"))btnAnadir.getStyleClass().add("boton-anadido");
        btnAnadir.setTooltip(new Tooltip("Ya está en tu pedido. Pulsa otra vez para añadir otra unidad"));
    }
}

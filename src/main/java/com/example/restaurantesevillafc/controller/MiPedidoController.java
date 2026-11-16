package com.example.restaurantesevillafc.controller;
import com.example.restaurantesevillafc.model.LineaPedido;
import com.example.restaurantesevillafc.service.PedidoService;
import javafx.beans.property.SimpleStringProperty;
import javafx.fxml.FXML;
import javafx.scene.control.*;
public class MiPedidoController {
    @FXML private TableView<LineaPedido> tablaPedido;
    @FXML private TableColumn<LineaPedido,String> colPlato,colPrecio,colSubtotal;
    @FXML private TableColumn<LineaPedido,Number> colCantidad;
    @FXML private Label lblNumPlatos,lblTextoSubtotal,lblSubtotal,lblIva,lblTotal;
    private final PedidoService pedidoService=new PedidoService();

    @FXML public void initialize(){
        colPlato.setCellValueFactory(d->new SimpleStringProperty(d.getValue().getPlato().getNombre()+"\n"+d.getValue().getPlato().getDescripcion()));
        colPrecio.setCellValueFactory(d->new SimpleStringProperty(String.format("%.2f €",d.getValue().getPrecioUnidad())));
        colCantidad.setCellValueFactory(d->d.getValue().cantidadProperty());
        colSubtotal.setCellValueFactory(d->new SimpleStringProperty(String.format("%.2f €",d.getValue().getSubtotal())));
        tablaPedido.setItems(pedidoService.getCarrito());
        tablaPedido.setPlaceholder(new Label("Tu pedido está vacío. Añade platos desde la carta."));
        actualizarResumen();
    }
    // Se llama al abrir la pantalla y cada vez que cambia algo del pedido
    private void actualizarResumen(){
        int articulos=pedidoService.totalArticulos();
        lblNumPlatos.setText(String.valueOf(articulos));
        lblTextoSubtotal.setText("Subtotal ("+articulos+(articulos==1?" artículo)":" artículos)"));
        lblSubtotal.setText(String.format("%.2f €",pedidoService.total()));
        lblIva.setText(String.format("%.2f €",pedidoService.ivaIncluido()));
        lblTotal.setText(String.format("%.2f €",pedidoService.total()));
        tablaPedido.refresh();
    }
}

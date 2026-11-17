package com.example.restaurantesevillafc.controller;
import com.example.restaurantesevillafc.dao.PlatoDAO;
import com.example.restaurantesevillafc.model.Plato;
import com.example.restaurantesevillafc.service.PedidoService;
import javafx.beans.property.SimpleStringProperty;
import javafx.fxml.FXML;
import javafx.scene.control.*;
public class CartaController {
    @FXML private TableView<Plato> tablaCarta;
    @FXML private TableColumn<Plato,String> colPlato,colCategoria,colAlergenos,colPrecio;
    @FXML private Label lblNumPlatos;
    private final PedidoService pedidoService=new PedidoService();

    @FXML public void initialize(){
        colPlato.setCellValueFactory(d->new SimpleStringProperty(d.getValue().getNombre()+"\n"+d.getValue().getDescripcion()));
        colCategoria.setCellValueFactory(d->d.getValue().categoriaProperty());
        colAlergenos.setCellValueFactory(d->d.getValue().alergenosProperty());
        colPrecio.setCellValueFactory(d->new SimpleStringProperty(String.format("%.2f €",d.getValue().getPrecio())));
        tablaCarta.setItems(PlatoDAO.todos());
        tablaCarta.setPlaceholder(new Label("No hay platos que coincidan con tu búsqueda."));
        tablaCarta.getSelectionModel().selectFirst();
        // El numero del boton Mi pedido son los platos que lleva el cliente en el pedido
        lblNumPlatos.setText(String.valueOf(pedidoService.totalArticulos()));
    }
}

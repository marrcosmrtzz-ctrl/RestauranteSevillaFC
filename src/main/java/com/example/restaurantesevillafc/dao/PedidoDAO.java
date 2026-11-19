package com.example.restaurantesevillafc.dao;
import com.example.restaurantesevillafc.model.LineaPedido;
import com.example.restaurantesevillafc.model.Pedido;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import java.util.List;

// De momento los pedidos se guardan en una lista, mas adelante iran a la base de datos
public final class PedidoDAO {

    private static final ObservableList<Pedido> PEDIDOS=FXCollections.observableArrayList();
    private static int siguienteId=2041;
    private PedidoDAO(){}
    public static ObservableList<Pedido> todos(){return PEDIDOS;}
    public static Pedido insertar(int idUsuario,String observaciones,List<LineaPedido> lineas){
        Pedido pedido=new Pedido(siguienteId++,idUsuario,observaciones,lineas);
        PEDIDOS.add(pedido);
        return pedido;
    }
    public static ObservableList<Pedido> buscarPorUsuario(int idUsuario){
        ObservableList<Pedido> resultado=FXCollections.observableArrayList();
        for(Pedido p:PEDIDOS){
            if(p.getIdUsuario()==idUsuario)resultado.add(p);
        }
        return resultado;
    }
}

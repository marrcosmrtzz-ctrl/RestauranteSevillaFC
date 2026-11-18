package com.example.restaurantesevillafc.dao;
import com.example.restaurantesevillafc.model.LineaPedido;
import com.example.restaurantesevillafc.model.Pedido;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import java.time.LocalDate;
import java.util.List;

// De momento los pedidos se guardan en una lista, mas adelante iran a la base de datos
public final class PedidoDAO {

    private static final ObservableList<Pedido> PEDIDOS=FXCollections.observableArrayList();
    private static int siguienteId=2041;
    private PedidoDAO(){}

    // Pedidos de prueba para la gestion de pedidos del trabajador (los mismos ids que seguirian en la base de datos)
    static{
        prueba(2036,3,"","ENTREGADO",12,30,new int[][]{{7,2}});
        prueba(2037,2,"","ENTREGADO",12,50,new int[][]{{5,2},{10,2}});
        prueba(2038,4,"Una de las raciones sin huevo","LISTO",13,20,new int[][]{{1,2},{4,1},{9,2}});
        prueba(2039,3,"Sin picante en el adobo, por favor.","EN_PREPARACION",13,35,new int[][]{{2,2},{6,1}});
        prueba(2040,2,"","RECIBIDO",13,42,new int[][]{{3,1},{4,1},{8,2}});
    }
    // platos es una lista de {id del plato, cantidad}
    private static void prueba(int id,int idUsuario,String observaciones,String estado,int hora,int minuto,int[][] platos){
        List<LineaPedido> lineas=new java.util.ArrayList<>();
        for(int[] p:platos)lineas.add(new LineaPedido(PlatoDAO.buscarPorId(p[0]),p[1]));
        Pedido pedido=new Pedido(id,idUsuario,observaciones,lineas);
        pedido.setEstado(estado);
        pedido.setFechaHora(LocalDate.now().atTime(hora,minuto));
        PEDIDOS.add(pedido);
    }
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

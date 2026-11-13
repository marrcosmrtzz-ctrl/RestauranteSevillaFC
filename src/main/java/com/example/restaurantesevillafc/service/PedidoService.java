package com.example.restaurantesevillafc.service;
import com.example.restaurantesevillafc.dao.PedidoDAO;
import com.example.restaurantesevillafc.dao.PlatoDAO;
import com.example.restaurantesevillafc.model.LineaPedido;
import com.example.restaurantesevillafc.model.Pedido;
import com.example.restaurantesevillafc.model.Plato;
import com.example.restaurantesevillafc.model.Usuario;
import javafx.beans.Observable;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import java.util.ArrayList;
public class PedidoService {

    public static final double IVA=0.10;
    public static final int MAX_UNIDADES=20;
    public static final int MAX_OBSERVACIONES=250;

    // El pedido que va montando el cliente antes de confirmarlo. Es static para que la carta y Mi pedido usen el mismo.
    // El extractor hace que la lista avise tambien cuando cambia la cantidad de una linea.
    private static final ObservableList<LineaPedido> CARRITO=FXCollections.observableArrayList(l->new Observable[]{l.cantidadProperty()});

    // De momento, hasta que este la carta, metemos unos platos para poder probar la pantalla
    static{
        CARRITO.add(new LineaPedido(PlatoDAO.buscarPorId(1),2));
        CARRITO.add(new LineaPedido(PlatoDAO.buscarPorId(4),1));
        CARRITO.add(new LineaPedido(PlatoDAO.buscarPorId(7),2));
    }

    public ObservableList<LineaPedido> getCarrito(){return CARRITO;}

    public void agregarPlato(Plato plato) throws Exception{
        if(plato==null||!plato.isDisponible())throw new Exception("Este plato no está disponible ahora mismo.");
        for(LineaPedido l:CARRITO){
            if(l.getPlato().getId()==plato.getId()){sumar(l);return;}
        }
        CARRITO.add(new LineaPedido(plato,1));
    }
    public void sumar(LineaPedido linea) throws Exception{
        if(linea.getCantidad()>=MAX_UNIDADES)throw new Exception("No puedes pedir más de "+MAX_UNIDADES+" unidades del mismo plato.");
        linea.setCantidad(linea.getCantidad()+1);
    }
    // La cantidad minima es 1, para quitar el plato esta el boton de la papelera
    public void restar(LineaPedido linea){
        if(linea.getCantidad()>1)linea.setCantidad(linea.getCantidad()-1);
    }
    public void quitar(LineaPedido linea){CARRITO.remove(linea);}
    public void vaciar(){CARRITO.clear();}

    public int totalArticulos(){return CARRITO.stream().mapToInt(LineaPedido::getCantidad).sum();}
    public double total(){return CARRITO.stream().mapToDouble(LineaPedido::getSubtotal).sum();}
    // Los precios ya llevan el IVA incluido, asi que lo sacamos del total
    public double ivaIncluido(){return total()-total()/(1+IVA);}

    public Pedido confirmarPedido(Usuario usuario,String observaciones) throws Exception{
        if(usuario==null)throw new Exception("Tienes que iniciar sesión para hacer un pedido.");
        if(CARRITO.isEmpty())throw new Exception("Tu pedido está vacío. Añade algún plato desde la carta.");
        String obs=observaciones==null?"":observaciones.trim();
        if(obs.length()>MAX_OBSERVACIONES)throw new Exception("Las observaciones no pueden pasar de "+MAX_OBSERVACIONES+" caracteres.");
        Pedido pedido=PedidoDAO.insertar(usuario.getId(),obs,new ArrayList<>(CARRITO));
        vaciar();
        return pedido;
    }
}

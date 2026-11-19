package com.example.restaurantesevillafc.service;
import com.example.restaurantesevillafc.dao.PedidoDAO;
import com.example.restaurantesevillafc.model.LineaPedido;
import com.example.restaurantesevillafc.model.Pedido;
import com.example.restaurantesevillafc.model.Plato;
import com.example.restaurantesevillafc.model.Usuario;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
public class PedidoService {

    public static final double IVA=0.10;
    public static final int MAX_UNIDADES=20;
    public static final int MAX_OBSERVACIONES=250;

    // El pedido que va montando el cliente antes de confirmarlo. Es static para que la carta y Mi pedido usen el mismo.
    private static final ObservableList<LineaPedido> CARRITO=FXCollections.observableArrayList();

    // Copias del pedido para poder deshacer y rehacer. Antes guarda como estaba antes de cada cambio
    // y Despues lo que se ha deshecho, por si quiere rehacerlo
    private static final Deque<List<LineaPedido>> ANTES=new ArrayDeque<>();
    private static final Deque<List<LineaPedido>> DESPUES=new ArrayDeque<>();
    // Para no gastar memoria solo se recuerdan los ultimos 20 cambios
    private static final int MAX_PASOS=20;

    public ObservableList<LineaPedido> getCarrito(){return CARRITO;}

    // Hace una copia de como esta el pedido ahora, con lineas nuevas para que no cambien despues
    private static List<LineaPedido> copiar(){
        List<LineaPedido> copia=new ArrayList<>();
        for(LineaPedido l:CARRITO)copia.add(new LineaPedido(l.getPlato(),l.getCantidad()));
        return copia;
    }
    // Se llama justo antes de cambiar el pedido, asi luego se puede volver a como estaba
    private static void guardarPaso(){
        ANTES.push(copiar());
        if(ANTES.size()>MAX_PASOS)ANTES.removeLast();
        // Si hace un cambio nuevo ya no tiene sentido rehacer lo que habia deshecho
        DESPUES.clear();
    }
    public boolean puedeDeshacer(){return !ANTES.isEmpty();}
    public boolean puedeRehacer(){return !DESPUES.isEmpty();}
    public void deshacer(){
        if(ANTES.isEmpty())return;
        DESPUES.push(copiar());
        CARRITO.setAll(ANTES.pop());
    }
    public void rehacer(){
        if(DESPUES.isEmpty())return;
        ANTES.push(copiar());
        CARRITO.setAll(DESPUES.pop());
    }

    public void agregarPlato(Plato plato) throws Exception{
        if(plato==null||!plato.isDisponible())throw new Exception("Este plato no está disponible ahora mismo.");
        for(LineaPedido l:CARRITO){
            if(l.getPlato().getId()==plato.getId()){sumar(l);return;}
        }
        guardarPaso();
        CARRITO.add(new LineaPedido(plato,1));
    }
    public void sumar(LineaPedido linea) throws Exception{
        if(linea.getCantidad()>=MAX_UNIDADES)throw new Exception("No puedes pedir más de "+MAX_UNIDADES+" unidades del mismo plato.");
        guardarPaso();
        linea.setCantidad(linea.getCantidad()+1);
    }
    // La cantidad minima es 1, para quitar el plato del todo esta el boton Quitar plato
    public void restar(LineaPedido linea){
        if(linea.getCantidad()>1){
            guardarPaso();
            linea.setCantidad(linea.getCantidad()-1);
        }
    }
    public void quitar(LineaPedido linea){
        guardarPaso();
        CARRITO.remove(linea);
    }
    public void vaciar(){
        if(CARRITO.isEmpty())return;
        guardarPaso();
        CARRITO.clear();
    }
    // Al cerrar sesion se borra el pedido y tambien lo que se podia deshacer, para que el siguiente cliente no lo recupere
    public void reiniciar(){
        CARRITO.clear();
        ANTES.clear();
        DESPUES.clear();
    }

    // Cuantas unidades de este plato lleva ya el cliente en el pedido, 0 si no lo lleva
    public int cantidadDe(Plato plato){
        for(LineaPedido l:CARRITO){
            if(l.getPlato().getId()==plato.getId())return l.getCantidad();
        }
        return 0;
    }
    public int totalArticulos(){
        int num=0;
        for(LineaPedido l:CARRITO)num+=l.getCantidad();
        return num;
    }
    public double total(){
        double suma=0;
        for(LineaPedido l:CARRITO)suma+=l.getSubtotal();
        return suma;
    }
    // Los precios ya llevan el IVA incluido, asi que lo sacamos del total
    public double ivaIncluido(){return total()-total()/(1+IVA);}

    public Pedido confirmarPedido(Usuario usuario,String observaciones) throws Exception{
        if(usuario==null)throw new Exception("Tienes que iniciar sesión para hacer un pedido.");
        if(CARRITO.isEmpty())throw new Exception("Tu pedido está vacío. Añade algún plato desde la carta.");
        String obs=observaciones==null?"":observaciones.trim();
        if(obs.length()>MAX_OBSERVACIONES)throw new Exception("Las observaciones no pueden pasar de "+MAX_OBSERVACIONES+" caracteres.");
        Pedido pedido=PedidoDAO.insertar(usuario.getId(),obs,new ArrayList<>(CARRITO));
        // El pedido ya esta en cocina, asi que no se puede deshacer
        reiniciar();
        return pedido;
    }
}

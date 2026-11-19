package com.example.restaurantesevillafc.dao;
import com.example.restaurantesevillafc.config.Conexion;
import com.example.restaurantesevillafc.model.LineaPedido;
import com.example.restaurantesevillafc.model.Pedido;
import com.example.restaurantesevillafc.model.Plato;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import java.sql.*;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

// Los pedidos se guardan en las tablas pedido y linea_pedido. Se cargan una vez en la lista
// y cada pedido nuevo o cambio de estado se guarda tambien en la base de datos
public final class PedidoDAO {

    private static final ObservableList<Pedido> PEDIDOS=FXCollections.observableArrayList();
    private static boolean cargados=false;
    private PedidoDAO(){}

    public static ObservableList<Pedido> todos(){
        if(!cargados)recargar();
        return PEDIDOS;
    }
    // Lee todos los pedidos con sus platos
    public static void recargar(){
        Map<Integer,List<LineaPedido>> lineas=new LinkedHashMap<>();
        List<Pedido> leidos=new ArrayList<>();
        try(Connection con=Conexion.getConexion()){
            try(PreparedStatement ps=con.prepareStatement("SELECT id_pedido,id_plato,cantidad,precio_unidad FROM linea_pedido");
                ResultSet rs=ps.executeQuery()){
                while(rs.next()){
                    Plato plato=PlatoDAO.buscarPorId(rs.getInt("id_plato"));
                    if(plato==null)continue;
                    LineaPedido linea=new LineaPedido(plato,rs.getInt("cantidad"));
                    // El precio que tenia el plato cuando se pidio
                    linea.setPrecioUnidad(rs.getDouble("precio_unidad"));
                    lineas.computeIfAbsent(rs.getInt("id_pedido"),k->new ArrayList<>()).add(linea);
                }
            }
            try(PreparedStatement ps=con.prepareStatement("SELECT id_pedido,fecha_hora,observaciones,estado,total,id_usuario FROM pedido ORDER BY id_pedido");
                ResultSet rs=ps.executeQuery()){
                while(rs.next()){
                    int id=rs.getInt("id_pedido");
                    Pedido pedido=new Pedido(id,rs.getInt("id_usuario"),rs.getString("observaciones"),lineas.getOrDefault(id,new ArrayList<>()));
                    pedido.setFechaHora(rs.getTimestamp("fecha_hora").toLocalDateTime());
                    pedido.setEstado(rs.getString("estado"));
                    pedido.setTotal(rs.getDouble("total"));
                    guardarCambiosDeEstado(pedido);
                    leidos.add(pedido);
                }
            }
        }catch(SQLException e){
            throw new RuntimeException(ErrorBD.mensaje(e));
        }
        PEDIDOS.setAll(leidos);
        cargados=true;
    }
    // Inserta el pedido y sus lineas a la vez: si falla algo no se guarda nada
    public static Pedido insertar(int idUsuario,String observaciones,List<LineaPedido> lineas){
        Pedido pedido=new Pedido(0,idUsuario,observaciones,lineas);
        try(Connection con=Conexion.getConexion()){
            con.setAutoCommit(false);
            try{
                try(PreparedStatement ps=con.prepareStatement(
                        "INSERT INTO pedido (fecha_hora,observaciones,estado,total,id_usuario) VALUES (?,?,?,?,?)",Statement.RETURN_GENERATED_KEYS)){
                    ps.setTimestamp(1,Timestamp.valueOf(pedido.getFechaHora()));
                    ps.setString(2,observaciones);
                    ps.setString(3,pedido.getEstado());
                    ps.setDouble(4,pedido.getTotal());
                    ps.setInt(5,idUsuario);
                    ps.executeUpdate();
                    try(ResultSet claves=ps.getGeneratedKeys()){
                        if(claves.next())pedido.setId(claves.getInt(1));
                    }
                }
                try(PreparedStatement ps=con.prepareStatement(
                        "INSERT INTO linea_pedido (id_pedido,id_plato,cantidad,precio_unidad) VALUES (?,?,?,?)")){
                    for(LineaPedido l:lineas){
                        ps.setInt(1,pedido.getId());
                        ps.setInt(2,l.getPlato().getId());
                        ps.setInt(3,l.getCantidad());
                        ps.setDouble(4,l.getPrecioUnidad());
                        ps.addBatch();
                    }
                    ps.executeBatch();
                }
                con.commit();
            }catch(SQLException e){
                con.rollback();
                throw e;
            }
        }catch(SQLException e){
            throw new RuntimeException(ErrorBD.mensaje(e));
        }
        guardarCambiosDeEstado(pedido);
        todos().add(pedido);
        return pedido;
    }
    public static ObservableList<Pedido> buscarPorUsuario(int idUsuario){
        ObservableList<Pedido> resultado=FXCollections.observableArrayList();
        for(Pedido p:todos()){
            if(p.getIdUsuario()==idUsuario)resultado.add(p);
        }
        return resultado;
    }
    // Cuando el trabajador cambia el estado de un pedido se guarda en la base de datos
    private static void guardarCambiosDeEstado(Pedido pedido){
        pedido.estadoProperty().addListener((obs,antes,ahora)->{
            if(ahora==null||ahora.equals(antes))return;
            try(Connection con=Conexion.getConexion();
                PreparedStatement ps=con.prepareStatement("UPDATE pedido SET estado=? WHERE id_pedido=?")){
                ps.setString(1,ahora);
                ps.setInt(2,pedido.getId());
                ps.executeUpdate();
            }catch(SQLException e){
                throw new RuntimeException(ErrorBD.mensaje(e));
            }
        });
    }
}

package com.example.restaurantesevillafc.dao;
import com.example.restaurantesevillafc.config.Conexion;
import com.example.restaurantesevillafc.model.Reserva;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import java.sql.*;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

// Las reservas se guardan en la tabla reserva. Se cargan una vez en la lista y cada reserva nueva
// o cambio de estado (confirmar o cancelar) se guarda tambien en la base de datos
public final class ReservaDAO {

    private static final ObservableList<Reserva> RESERVAS=FXCollections.observableArrayList();
    private static boolean cargadas=false;
    private ReservaDAO(){}

    public static ObservableList<Reserva> todos(){
        if(!cargadas)recargar();
        return RESERVAS;
    }
    public static void recargar(){
        String sql="SELECT id_reserva,fecha,hora,num_personas,observaciones,estado,id_usuario FROM reserva ORDER BY id_reserva DESC";
        List<Reserva> leidas=new ArrayList<>();
        try(Connection con=Conexion.getConexion();
            PreparedStatement ps=con.prepareStatement(sql);
            ResultSet rs=ps.executeQuery()){
            while(rs.next()){
                Reserva r=new Reserva(rs.getInt("id_reserva"),rs.getInt("id_usuario"),rs.getDate("fecha").toLocalDate(),
                        rs.getTime("hora").toLocalTime(),rs.getInt("num_personas"),rs.getString("observaciones"));
                r.setEstado(rs.getString("estado"));
                guardarCambiosDeEstado(r);
                leidas.add(r);
            }
        }catch(SQLException e){
            throw new RuntimeException(ErrorBD.mensaje(e));
        }
        RESERVAS.setAll(leidas);
        cargadas=true;
    }
    // La reserva nueva se mete la primera para que salga arriba en la tabla
    public static Reserva insertar(int idUsuario,LocalDate fecha,LocalTime hora,int numPersonas,String observaciones){
        Reserva reserva=new Reserva(0,idUsuario,fecha,hora,numPersonas,observaciones);
        String sql="INSERT INTO reserva (fecha,hora,num_personas,observaciones,estado,id_usuario) VALUES (?,?,?,?,?,?)";
        try(Connection con=Conexion.getConexion();
            PreparedStatement ps=con.prepareStatement(sql,Statement.RETURN_GENERATED_KEYS)){
            ps.setDate(1,Date.valueOf(fecha));
            ps.setTime(2,Time.valueOf(hora));
            ps.setInt(3,numPersonas);
            ps.setString(4,observaciones);
            ps.setString(5,reserva.getEstado());
            ps.setInt(6,idUsuario);
            ps.executeUpdate();
            try(ResultSet claves=ps.getGeneratedKeys()){
                if(claves.next())reserva.setId(claves.getInt(1));
            }
        }catch(SQLException e){
            throw new RuntimeException(ErrorBD.mensaje(e));
        }
        guardarCambiosDeEstado(reserva);
        todos().add(0,reserva);
        return reserva;
    }
    public static ObservableList<Reserva> buscarPorUsuario(int idUsuario){
        ObservableList<Reserva> resultado=FXCollections.observableArrayList();
        for(Reserva r:todos()){
            if(r.getIdUsuario()==idUsuario)resultado.add(r);
        }
        return resultado;
    }
    // Cuantas reservas sin cancelar hay ese dia a esa hora (se pregunta a la base de datos por si otro cliente acaba de reservar)
    public static int contarReservas(LocalDate fecha,LocalTime hora){
        String sql="SELECT COUNT(*) FROM reserva WHERE estado<>'CANCELADA' AND fecha=? AND hora=?";
        try(Connection con=Conexion.getConexion();
            PreparedStatement ps=con.prepareStatement(sql)){
            ps.setDate(1,Date.valueOf(fecha));
            ps.setTime(2,Time.valueOf(hora));
            try(ResultSet rs=ps.executeQuery()){
                return rs.next()?rs.getInt(1):0;
            }
        }catch(SQLException e){
            throw new RuntimeException(ErrorBD.mensaje(e));
        }
    }
    private static void guardarCambiosDeEstado(Reserva reserva){
        reserva.estadoProperty().addListener((obs,antes,ahora)->{
            if(ahora==null||ahora.equals(antes))return;
            try(Connection con=Conexion.getConexion();
                PreparedStatement ps=con.prepareStatement("UPDATE reserva SET estado=? WHERE id_reserva=?")){
                ps.setString(1,ahora);
                ps.setInt(2,reserva.getId());
                ps.executeUpdate();
            }catch(SQLException e){
                throw new RuntimeException(ErrorBD.mensaje(e));
            }
        });
    }
}

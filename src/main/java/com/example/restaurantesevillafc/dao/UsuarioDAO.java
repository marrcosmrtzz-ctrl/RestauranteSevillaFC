package com.example.restaurantesevillafc.dao;
import com.example.restaurantesevillafc.config.Conexion;
import com.example.restaurantesevillafc.model.Usuario;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

// Los usuarios se leen de la tabla usuario de la base de datos
public final class UsuarioDAO {

    private static final String SELECT="SELECT id_usuario,nombre,apellidos,email,password,rol FROM usuario";
    private UsuarioDAO(){}

    public static ObservableList<Usuario> todos(){
        ObservableList<Usuario> lista=FXCollections.observableArrayList();
        try(Connection con=Conexion.getConexion();
            PreparedStatement ps=con.prepareStatement(SELECT+" ORDER BY id_usuario");
            ResultSet rs=ps.executeQuery()){
            while(rs.next())lista.add(crear(rs));
        }catch(SQLException e){
            throw new RuntimeException(ErrorBD.mensaje(e));
        }
        return lista;
    }
    public static Usuario buscarPorId(int id){
        return buscarUno(SELECT+" WHERE id_usuario=?",id);
    }
    public static Usuario buscarPorEmail(String email){
        return buscarUno(SELECT+" WHERE email=?",email);
    }
    private static Usuario buscarUno(String sql,Object valor){
        try(Connection con=Conexion.getConexion();
            PreparedStatement ps=con.prepareStatement(sql)){
            ps.setObject(1,valor);
            try(ResultSet rs=ps.executeQuery()){
                return rs.next()?crear(rs):null;
            }
        }catch(SQLException e){
            throw new RuntimeException(ErrorBD.mensaje(e));
        }
    }
    private static Usuario crear(ResultSet rs) throws SQLException{
        return new Usuario(rs.getInt("id_usuario"),rs.getString("nombre"),rs.getString("apellidos"),
                rs.getString("email"),rs.getString("password"),rs.getString("rol"));
    }
}

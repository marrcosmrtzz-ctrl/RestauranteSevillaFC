package com.example.restaurantesevillafc.dao;
import com.example.restaurantesevillafc.config.Conexion;
import com.example.restaurantesevillafc.model.Usuario;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

// Acceso a la tabla usuario de la base de datos. La conexion la da la clase Conexion (config)
public final class UsuarioDAO {

    private static final String SELECT="SELECT id_usuario,nombre,apellidos,email,telefono,password,rol FROM usuario";
    private UsuarioDAO(){}

    // Todos los usuarios (el trabajador los usa para ver los datos de los clientes)
    public static ObservableList<Usuario> todos(){
        ObservableList<Usuario> lista=FXCollections.observableArrayList();
        try(Connection con=Conexion.getConexion();
            PreparedStatement ps=con.prepareStatement(SELECT+" ORDER BY id_usuario");
            ResultSet rs=ps.executeQuery()){
            while(rs.next())lista.add(crear(rs));
        }catch(SQLException e){
            throw new RuntimeException(ErrorBD.mensaje(e),e);
        }
        return lista;
    }
    // Busca un usuario por su id. Devuelve null si no existe
    public static Usuario buscarPorId(int id){
        return buscarUno(SELECT+" WHERE id_usuario=?",id);
    }
    // Busca un usuario por su correo. Devuelve null si no existe
    public static Usuario buscarPorEmail(String email){
        return buscarUno(SELECT+" WHERE email=?",email);
    }

    // Guarda un usuario nuevo y le pone el id que le ha dado MySQL (lo usa el registro)
    public static void insertar(Usuario u){
        String sql="INSERT INTO usuario (nombre,apellidos,email,telefono,password,rol) VALUES (?,?,?,?,?,?)";
        try(Connection con=Conexion.getConexion();PreparedStatement ps=con.prepareStatement(sql,Statement.RETURN_GENERATED_KEYS)){
            ps.setString(1,u.getNombre());
            ps.setString(2,u.getApellidos());
            ps.setString(3,u.getEmail());
            ps.setString(4,u.getTelefono()==null||u.getTelefono().isBlank()?null:u.getTelefono());
            ps.setString(5,u.getPassword());
            ps.setString(6,u.getRol());
            ps.executeUpdate();
            try(ResultSet rs=ps.getGeneratedKeys()){
                if(rs.next())u.setId(rs.getInt(1));
            }
        }catch(SQLException e){
            throw new RuntimeException(ErrorBD.mensaje(e),e);
        }
    }

    private static Usuario buscarUno(String sql,Object valor){
        try(Connection con=Conexion.getConexion();
            PreparedStatement ps=con.prepareStatement(sql)){
            ps.setObject(1,valor);
            try(ResultSet rs=ps.executeQuery()){
                return rs.next()?crear(rs):null;
            }
        }catch(SQLException e){
            throw new RuntimeException(ErrorBD.mensaje(e),e);
        }
    }
    private static Usuario crear(ResultSet rs) throws SQLException{
        Usuario u=new Usuario(rs.getInt("id_usuario"),rs.getString("nombre"),rs.getString("apellidos"),
                rs.getString("email"),rs.getString("password"),rs.getString("rol"));
        u.setTelefono(rs.getString("telefono"));
        return u;
    }
}

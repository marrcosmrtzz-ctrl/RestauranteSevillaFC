package com.example.restaurantesevillafc.dao;
import com.example.restaurantesevillafc.config.Conexion;
import com.example.restaurantesevillafc.model.Usuario;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

// Acceso a la tabla usuario de la base de datos. La conexion la da la clase Conexion (config)
public final class UsuarioDAO {

    private static final String ERROR_BD="No se puede conectar con la base de datos. Comprueba que MySQL está arrancado.";
    private UsuarioDAO(){}

    // Busca un usuario por su correo. Devuelve null si no existe
    public static Usuario buscarPorEmail(String email){
        String sql="SELECT id_usuario,nombre,apellidos,email,telefono,password,rol FROM usuario WHERE email=?";
        try(Connection con=Conexion.getConexion();PreparedStatement ps=con.prepareStatement(sql)){
            ps.setString(1,email);
            try(ResultSet rs=ps.executeQuery()){
                if(!rs.next())return null;
                Usuario u=new Usuario(rs.getInt("id_usuario"),rs.getString("nombre"),rs.getString("apellidos"),
                        rs.getString("email"),rs.getString("password"),rs.getString("rol"));
                u.setTelefono(rs.getString("telefono"));
                return u;
            }
        }catch(SQLException e){
            throw new RuntimeException(ERROR_BD,e);
        }
    }

    // Guarda un usuario nuevo y le pone el id que le ha dado MySQL
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
            throw new RuntimeException(ERROR_BD,e);
        }
    }
}

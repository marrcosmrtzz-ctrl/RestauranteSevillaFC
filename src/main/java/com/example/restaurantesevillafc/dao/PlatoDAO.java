package com.example.restaurantesevillafc.dao;
import com.example.restaurantesevillafc.config.Conexion;
import com.example.restaurantesevillafc.model.Plato;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

// Los platos se cogen de la tabla plato de MySQL
public final class PlatoDAO {

    // Junta cada plato con su categoria para tener el nombre de la categoria y no solo su numero
    private static final String SELECT_PLATOS="SELECT p.id_plato, p.nombre, p.descripcion, p.alergenos, p.precio, p.disponible, c.nombre AS categoria "
            +"FROM plato p JOIN categoria c ON p.id_categoria = c.id_categoria";
    private PlatoDAO(){}

    // Crea un plato con los datos de la fila por la que va el ResultSet
    private static Plato leerPlato(ResultSet rs) throws SQLException{
        return new Plato(rs.getInt("id_plato"),rs.getString("nombre"),rs.getString("descripcion"),rs.getString("alergenos"),
                rs.getDouble("precio"),rs.getBoolean("disponible"),rs.getString("categoria"));
    }
    // Trae todos los platos, si MySQL falla devuelve la lista vacia
    public static ObservableList<Plato> todos(){
        ObservableList<Plato> lista=FXCollections.observableArrayList();
        try(Connection con=Conexion.getConexion();
            PreparedStatement ps=con.prepareStatement(SELECT_PLATOS+" ORDER BY p.id_plato");
            ResultSet rs=ps.executeQuery()){
            while(rs.next())lista.add(leerPlato(rs));
        }catch(SQLException e){
            System.out.println("Error al cargar los platos "+e.getMessage());
        }
        return lista;
    }
    // Busca un plato por su numero, devuelve null si no existe
    public static Plato buscarPorId(int id){
        try(Connection con=Conexion.getConexion();
            PreparedStatement ps=con.prepareStatement(SELECT_PLATOS+" WHERE p.id_plato = ?")){
            ps.setInt(1,id);
            ResultSet rs=ps.executeQuery();
            // if en vez de while porque como mucho sale un plato
            if(rs.next())return leerPlato(rs);
        }catch(SQLException e){
            System.out.println("Error al buscar el plato "+e.getMessage());
        }
        return null;
    }
}

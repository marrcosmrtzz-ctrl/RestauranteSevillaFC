package com.example.restaurantesevillafc.dao;
import com.example.restaurantesevillafc.config.Conexion;
import com.example.restaurantesevillafc.model.Plato;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import java.sql.*;

// Los platos se leen de la tabla plato. Se cargan una vez en la lista y cada cambio se guarda tambien en la base de datos
public final class PlatoDAO {

    private static final ObservableList<Plato> PLATOS=FXCollections.observableArrayList();
    private static boolean cargados=false;
    private PlatoDAO(){}

    // Si MySQL falla devuelve la lista vacia (la carta del cliente avisa en ese caso)
    public static ObservableList<Plato> todos(){
        if(!cargados){
            try{
                recargar();
            }catch(RuntimeException e){
                System.out.println("Error al cargar los platos "+e.getMessage());
            }
        }
        return PLATOS;
    }
    // Vuelve a leer la carta de la base de datos (por si otro la ha cambiado)
    public static void recargar(){
        String sql="SELECT p.id_plato,p.nombre,p.descripcion,p.alergenos,p.precio,p.disponible,c.nombre AS categoria "
                +"FROM plato p JOIN categoria c ON p.id_categoria=c.id_categoria ORDER BY p.id_plato";
        ObservableList<Plato> leidos=FXCollections.observableArrayList();
        try(Connection con=Conexion.getConexion();
            PreparedStatement ps=con.prepareStatement(sql);
            ResultSet rs=ps.executeQuery()){
            while(rs.next()){
                leidos.add(new Plato(rs.getInt("id_plato"),rs.getString("nombre"),rs.getString("descripcion"),
                        rs.getString("alergenos"),rs.getDouble("precio"),rs.getBoolean("disponible"),rs.getString("categoria")));
            }
        }catch(SQLException e){
            throw new RuntimeException(ErrorBD.mensaje(e));
        }
        PLATOS.setAll(leidos);
        cargados=true;
    }
    public static Plato buscarPorId(int id){
        for(Plato p:todos()){
            if(p.getId()==id)return p;
        }
        return null;
    }

    // Los usa la gestion del menu del trabajador
    public static Plato buscarPorNombre(String nombre){
        for(Plato p:todos()){
            if(p.getNombre().equalsIgnoreCase(nombre))return p;
        }
        return null;
    }
    public static int siguienteId(){
        // El id de verdad lo pone MySQL al insertar (AUTO_INCREMENT)
        int max=0;
        for(Plato p:todos())max=Math.max(max,p.getId());
        return max+1;
    }
    public static void anadir(Plato plato){
        String sql="INSERT INTO plato (nombre,descripcion,alergenos,precio,disponible,id_categoria) "
                +"VALUES (?,?,?,?,?,(SELECT id_categoria FROM categoria WHERE nombre=?))";
        try(Connection con=Conexion.getConexion();
            PreparedStatement ps=con.prepareStatement(sql,Statement.RETURN_GENERATED_KEYS)){
            rellenar(ps,plato);
            ps.executeUpdate();
            try(ResultSet claves=ps.getGeneratedKeys()){
                if(claves.next())plato.setId(claves.getInt(1));
            }
        }catch(SQLException e){
            throw new RuntimeException(ErrorBD.mensaje(e));
        }
        todos().add(plato);
    }
    // Vuelve a meter un plato borrado con su mismo id (lo usa deshacer), asi conserva su foto
    public static void restaurar(Plato plato){
        String sql="INSERT INTO plato (id_plato,nombre,descripcion,alergenos,precio,disponible,id_categoria) "
                +"VALUES (?,?,?,?,?,?,(SELECT id_categoria FROM categoria WHERE nombre=?))";
        try(Connection con=Conexion.getConexion();
            PreparedStatement ps=con.prepareStatement(sql)){
            ps.setInt(1,plato.getId());
            ps.setString(2,plato.getNombre());
            ps.setString(3,plato.getDescripcion());
            ps.setString(4,plato.getAlergenos());
            ps.setDouble(5,plato.getPrecio());
            ps.setBoolean(6,plato.isDisponible());
            ps.setString(7,plato.getCategoria());
            ps.executeUpdate();
        }catch(SQLException e){
            throw new RuntimeException(ErrorBD.mensaje(e));
        }
        // Se pone en su sitio segun el id para que la tabla siga en el mismo orden
        int i=0;
        while(i<todos().size()&&todos().get(i).getId()<plato.getId())i++;
        todos().add(i,plato);
    }
    public static void actualizar(Plato plato){
        String sql="UPDATE plato SET nombre=?,descripcion=?,alergenos=?,precio=?,disponible=?,"
                +"id_categoria=(SELECT id_categoria FROM categoria WHERE nombre=?) WHERE id_plato=?";
        try(Connection con=Conexion.getConexion();
            PreparedStatement ps=con.prepareStatement(sql)){
            rellenar(ps,plato);
            ps.setInt(7,plato.getId());
            ps.executeUpdate();
        }catch(SQLException e){
            throw new RuntimeException(ErrorBD.mensaje(e));
        }
    }
    public static void eliminar(Plato plato){
        try(Connection con=Conexion.getConexion();
            PreparedStatement ps=con.prepareStatement("DELETE FROM plato WHERE id_plato=?")){
            ps.setInt(1,plato.getId());
            ps.executeUpdate();
        }catch(SQLIntegrityConstraintViolationException e){
            // El plato esta en algun pedido (clave foranea de linea_pedido)
            throw new RuntimeException("Este plato aparece en pedidos de clientes y no se puede eliminar. Desmarca «Disponible en la carta» para quitarlo de la carta.");
        }catch(SQLException e){
            throw new RuntimeException(ErrorBD.mensaje(e));
        }
        todos().remove(plato);
    }
    private static void rellenar(PreparedStatement ps,Plato plato) throws SQLException{
        ps.setString(1,plato.getNombre());
        ps.setString(2,plato.getDescripcion());
        ps.setString(3,plato.getAlergenos());
        ps.setDouble(4,plato.getPrecio());
        ps.setBoolean(5,plato.isDisponible());
        ps.setString(6,plato.getCategoria());
    }
}

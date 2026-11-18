package com.example.restaurantesevillafc.config;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

// Abre la conexion con la base de datos, la base se crea con el script docs/base_datos/restaurante_sevillafc.sql
public class Conexion {
    private static final String URL="jdbc:mysql://localhost:3306/restaurante_sevillafc";
    private static final String USUARIO="root";
    private static final String PASSWORD="";

    // Cada DAO pide su propia conexion con este metodo y la cierra al terminar
    public static Connection getConexion() throws SQLException{
        return DriverManager.getConnection(URL,USUARIO,PASSWORD);
    }
}

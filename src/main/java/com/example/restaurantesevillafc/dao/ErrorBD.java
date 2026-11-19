package com.example.restaurantesevillafc.dao;
import java.sql.SQLException;

// Convierte los errores de MySQL en un mensaje que entienda el usuario
public final class ErrorBD {
    private ErrorBD(){}
    public static String mensaje(SQLException e){
        // 08xxx son errores de conexion (MySQL apagado, XAMPP sin arrancar...)
        if(e.getSQLState()!=null&&e.getSQLState().startsWith("08"))
            return "No se puede conectar con la base de datos. Comprueba que MySQL está arrancado en XAMPP y que has importado el script.";
        if(e.getErrorCode()==1049)
            return "No existe la base de datos restaurante_sevillafc. Importa el script docs/base_datos/restaurante_sevillafc.sql en phpMyAdmin.";
        return "Error en la base de datos: "+e.getMessage();
    }
}

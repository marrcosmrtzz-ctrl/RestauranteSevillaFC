package com.example.restaurantesevillafc.dao;
import com.example.restaurantesevillafc.model.Usuario;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

// De momento los usuarios estan en una lista, mas adelante se cogeran de la base de datos
public final class UsuarioDAO {

    private static final ObservableList<Usuario> USUARIOS=FXCollections.observableArrayList(
            new Usuario(1,"Antonio","Ruiz","antonio.ruiz@sevillafc.es","1234","TRABAJADOR"),
            new Usuario(2,"Lucía","García Romero","lucia.garcia@correo.com","1234","CLIENTE"));
    private UsuarioDAO(){}
    public static ObservableList<Usuario> todos(){return USUARIOS;}
    public static Usuario buscarPorEmail(String email){
        for(Usuario u:USUARIOS){
            if(u.getEmail().equalsIgnoreCase(email))return u;
        }
        return null;
    }
}

package com.example.restaurantesevillafc.model;
import javafx.beans.property.*;
public class Usuario {
    private final IntegerProperty id = new SimpleIntegerProperty();
    private final StringProperty nombre = new SimpleStringProperty();
    private final StringProperty apellidos = new SimpleStringProperty();
    private final StringProperty email = new SimpleStringProperty();
    private final StringProperty password = new SimpleStringProperty();
    private final StringProperty rol = new SimpleStringProperty();

    public Usuario(int id,String nombre,String apellidos,String email,String password,String rol){
        setId(id);
        setNombre(nombre);
        setApellidos(apellidos);
        setEmail(email);
        setPassword(password);
        setRol(rol);
    }
    public boolean esTrabajador(){return "TRABAJADOR".equals(getRol());}
    public int getId(){return id.get();}
    public void setId(int v){id.set(v);}
    public IntegerProperty idProperty(){return id;}
    public String getNombre(){return nombre.get();}
    public void setNombre(String v){nombre.set(v);}
    public StringProperty nombreProperty(){return nombre;}
    public String getApellidos(){return apellidos.get();}
    public void setApellidos(String v){apellidos.set(v);}
    public StringProperty apellidosProperty(){return apellidos;}
    public String getEmail(){return email.get();}
    public void setEmail(String v){email.set(v);}
    public StringProperty emailProperty(){return email;}
    public String getPassword(){return password.get();}
    public void setPassword(String v){password.set(v);}
    public StringProperty passwordProperty(){return password;}
    public String getRol(){return rol.get();}
    public void setRol(String v){rol.set(v);}
    public StringProperty rolProperty(){return rol;}
}

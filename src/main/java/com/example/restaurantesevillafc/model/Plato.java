package com.example.restaurantesevillafc.model;
import javafx.beans.property.*;
public class Plato {
    private final IntegerProperty id = new SimpleIntegerProperty();
    private final StringProperty nombre = new SimpleStringProperty();
    private final StringProperty descripcion = new SimpleStringProperty();
    private final StringProperty alergenos = new SimpleStringProperty();
    private final DoubleProperty precio = new SimpleDoubleProperty();
    private final BooleanProperty disponible = new SimpleBooleanProperty();
    private final StringProperty categoria = new SimpleStringProperty();

    public Plato(int id,String nombre,String descripcion,String alergenos,double precio,boolean disponible,String categoria){
        setId(id);
        setNombre(nombre);
        setDescripcion(descripcion);
        setAlergenos(alergenos);
        setPrecio(precio);
        setDisponible(disponible);
        setCategoria(categoria);
    }
    public int getId(){return id.get();}
    public void setId(int v){id.set(v);}
    public IntegerProperty idProperty(){return id;}
    public String getNombre(){return nombre.get();}
    public void setNombre(String v){nombre.set(v);}
    public StringProperty nombreProperty(){return nombre;}
    public String getDescripcion(){return descripcion.get();}
    public void setDescripcion(String v){descripcion.set(v);}
    public StringProperty descripcionProperty(){return descripcion;}
    public String getAlergenos(){return alergenos.get();}
    public void setAlergenos(String v){alergenos.set(v);}
    public StringProperty alergenosProperty(){return alergenos;}
    public double getPrecio(){return precio.get();}
    public void setPrecio(double v){precio.set(v);}
    public DoubleProperty precioProperty(){return precio;}
    public boolean isDisponible(){return disponible.get();}
    public void setDisponible(boolean v){disponible.set(v);}
    public BooleanProperty disponibleProperty(){return disponible;}
    public String getCategoria(){return categoria.get();}
    public void setCategoria(String v){categoria.set(v);}
    public StringProperty categoriaProperty(){return categoria;}
}

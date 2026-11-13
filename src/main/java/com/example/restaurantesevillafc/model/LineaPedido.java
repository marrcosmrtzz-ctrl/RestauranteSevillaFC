package com.example.restaurantesevillafc.model;
import javafx.beans.property.*;
public class LineaPedido {
    private final ObjectProperty<Plato> plato = new SimpleObjectProperty<>();
    private final IntegerProperty cantidad = new SimpleIntegerProperty();
    private final DoubleProperty precioUnidad = new SimpleDoubleProperty();
    private final DoubleProperty subtotal = new SimpleDoubleProperty();

    public LineaPedido(Plato plato,int cantidad){
        setPlato(plato);
        setCantidad(cantidad);
        // Se guarda el precio que tenia el plato al pedirlo, como en la tabla linea_pedido
        setPrecioUnidad(plato.getPrecio());
        subtotal.bind(this.cantidad.multiply(precioUnidad));
    }
    public Plato getPlato(){return plato.get();}
    public void setPlato(Plato v){plato.set(v);}
    public ObjectProperty<Plato> platoProperty(){return plato;}
    public int getCantidad(){return cantidad.get();}
    public void setCantidad(int v){cantidad.set(v);}
    public IntegerProperty cantidadProperty(){return cantidad;}
    public double getPrecioUnidad(){return precioUnidad.get();}
    public void setPrecioUnidad(double v){precioUnidad.set(v);}
    public DoubleProperty precioUnidadProperty(){return precioUnidad;}
    public double getSubtotal(){return subtotal.get();}
    public ReadOnlyDoubleProperty subtotalProperty(){return subtotal;}
}

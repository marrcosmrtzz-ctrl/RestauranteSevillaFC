package com.example.restaurantesevillafc.model;
import javafx.beans.property.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import java.time.LocalDateTime;
import java.util.List;
public class Pedido {
    private final IntegerProperty id = new SimpleIntegerProperty();
    private final ObjectProperty<LocalDateTime> fechaHora = new SimpleObjectProperty<>();
    private final StringProperty estado = new SimpleStringProperty();
    private final StringProperty observaciones = new SimpleStringProperty();
    private final DoubleProperty total = new SimpleDoubleProperty();
    private final IntegerProperty idUsuario = new SimpleIntegerProperty();
    private final ObservableList<LineaPedido> lineas = FXCollections.observableArrayList();

    public Pedido(int id,int idUsuario,String observaciones,List<LineaPedido> lineas){
        setId(id);
        setIdUsuario(idUsuario);
        setObservaciones(observaciones);
        setFechaHora(LocalDateTime.now());
        setEstado("RECIBIDO");
        this.lineas.addAll(lineas);
        setTotal(lineas.stream().mapToDouble(LineaPedido::getSubtotal).sum());
    }
    public int getNumPlatos(){return lineas.stream().mapToInt(LineaPedido::getCantidad).sum();}
    public ObservableList<LineaPedido> getLineas(){return lineas;}
    public int getId(){return id.get();}
    public void setId(int v){id.set(v);}
    public IntegerProperty idProperty(){return id;}
    public LocalDateTime getFechaHora(){return fechaHora.get();}
    public void setFechaHora(LocalDateTime v){fechaHora.set(v);}
    public ObjectProperty<LocalDateTime> fechaHoraProperty(){return fechaHora;}
    public String getEstado(){return estado.get();}
    public void setEstado(String v){estado.set(v);}
    public StringProperty estadoProperty(){return estado;}
    public String getObservaciones(){return observaciones.get();}
    public void setObservaciones(String v){observaciones.set(v);}
    public StringProperty observacionesProperty(){return observaciones;}
    public double getTotal(){return total.get();}
    public void setTotal(double v){total.set(v);}
    public DoubleProperty totalProperty(){return total;}
    public int getIdUsuario(){return idUsuario.get();}
    public void setIdUsuario(int v){idUsuario.set(v);}
    public IntegerProperty idUsuarioProperty(){return idUsuario;}
}

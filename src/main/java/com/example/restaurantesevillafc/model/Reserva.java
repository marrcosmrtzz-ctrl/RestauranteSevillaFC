package com.example.restaurantesevillafc.model;
import javafx.beans.property.*;
import java.time.LocalDate;
import java.time.LocalTime;
public class Reserva {
    private final IntegerProperty id = new SimpleIntegerProperty();
    private final ObjectProperty<LocalDate> fecha = new SimpleObjectProperty<>();
    private final ObjectProperty<LocalTime> hora = new SimpleObjectProperty<>();
    private final IntegerProperty numPersonas = new SimpleIntegerProperty();
    private final StringProperty observaciones = new SimpleStringProperty();
    private final StringProperty estado = new SimpleStringProperty();
    private final IntegerProperty idUsuario = new SimpleIntegerProperty();

    // Cada reserva nueva empieza como PENDIENTE hasta que el trabajador la confirma
    public Reserva(int id,int idUsuario,LocalDate fecha,LocalTime hora,int numPersonas,String observaciones){
        setId(id);
        setIdUsuario(idUsuario);
        setFecha(fecha);
        setHora(hora);
        setNumPersonas(numPersonas);
        setObservaciones(observaciones);
        setEstado("PENDIENTE");
    }
    // Una reserva cancelada ya no cuenta para nada, ni ocupa sitio ni se puede volver a cancelar
    public boolean estaCancelada(){return "CANCELADA".equals(getEstado());}
    public int getId(){return id.get();}
    public void setId(int v){id.set(v);}
    public IntegerProperty idProperty(){return id;}
    public LocalDate getFecha(){return fecha.get();}
    public void setFecha(LocalDate v){fecha.set(v);}
    public ObjectProperty<LocalDate> fechaProperty(){return fecha;}
    public LocalTime getHora(){return hora.get();}
    public void setHora(LocalTime v){hora.set(v);}
    public ObjectProperty<LocalTime> horaProperty(){return hora;}
    public int getNumPersonas(){return numPersonas.get();}
    public void setNumPersonas(int v){numPersonas.set(v);}
    public IntegerProperty numPersonasProperty(){return numPersonas;}
    public String getObservaciones(){return observaciones.get();}
    public void setObservaciones(String v){observaciones.set(v);}
    public StringProperty observacionesProperty(){return observaciones;}
    public String getEstado(){return estado.get();}
    public void setEstado(String v){estado.set(v);}
    public StringProperty estadoProperty(){return estado;}
    public int getIdUsuario(){return idUsuario.get();}
    public void setIdUsuario(int v){idUsuario.set(v);}
    public IntegerProperty idUsuarioProperty(){return idUsuario;}
}

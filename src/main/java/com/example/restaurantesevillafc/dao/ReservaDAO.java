package com.example.restaurantesevillafc.dao;
import com.example.restaurantesevillafc.model.Reserva;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import java.time.LocalDate;
import java.time.LocalTime;

// De momento las reservas se guardan en una lista, mas adelante iran a la base de datos
public final class ReservaDAO {

    private static final ObservableList<Reserva> RESERVAS=FXCollections.observableArrayList();
    private static int siguienteId=301;

    // Unas reservas de prueba de Lucia (usuario 2) para ver la tabla de Mis reservas
    static{
        LocalDate hoy=LocalDate.now();
        Reserva confirmada=insertar(2,hoy.minusDays(12),LocalTime.of(14,0),2,"");
        confirmada.setEstado("CONFIRMADA");
        Reserva cancelada=insertar(2,hoy.minusDays(5),LocalTime.of(21,30),6,"Cumpleaños");
        cancelada.setEstado("CANCELADA");
        insertar(2,hoy.plusDays(4),LocalTime.of(21,0),4,"Terraza si puede ser");
        // Llenamos las dos de la tarde de pasado manana con reservas de otros clientes para probar una hora completa
        for(int i=0;i<6;i++){
            insertar(10+i,hoy.plusDays(2),LocalTime.of(14,0),2,"");
        }
    }
    private ReservaDAO(){}
    public static ObservableList<Reserva> todos(){return RESERVAS;}
    // La reserva nueva se mete la primera para que salga arriba en la tabla
    public static Reserva insertar(int idUsuario,LocalDate fecha,LocalTime hora,int numPersonas,String observaciones){
        Reserva reserva=new Reserva(siguienteId++,idUsuario,fecha,hora,numPersonas,observaciones);
        RESERVAS.add(0,reserva);
        return reserva;
    }
    public static ObservableList<Reserva> buscarPorUsuario(int idUsuario){
        ObservableList<Reserva> resultado=FXCollections.observableArrayList();
        for(Reserva r:RESERVAS){
            if(r.getIdUsuario()==idUsuario)resultado.add(r);
        }
        return resultado;
    }
    // Cuantas reservas sin cancelar hay ese dia a esa hora
    public static int contarReservas(LocalDate fecha,LocalTime hora){
        int num=0;
        for(Reserva r:RESERVAS){
            if(!r.estaCancelada()&&r.getFecha().equals(fecha)&&r.getHora().equals(hora))num++;
        }
        return num;
    }
}

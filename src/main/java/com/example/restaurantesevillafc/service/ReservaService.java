package com.example.restaurantesevillafc.service;
import com.example.restaurantesevillafc.dao.ReservaDAO;
import com.example.restaurantesevillafc.model.Reserva;
import com.example.restaurantesevillafc.model.Usuario;
import javafx.collections.ObservableList;
import java.time.LocalDate;
import java.time.LocalTime;
public class ReservaService {

    public static final int MAX_PERSONAS=10;
    public static final int MAX_OBSERVACIONES=255;
    // Hasta cuantos dias por delante se puede reservar
    public static final int MAX_DIAS=60;
    // El restaurante tiene 6 mesas, asi que a cada hora caben 6 reservas como mucho
    public static final int MAX_RESERVAS_POR_HORA=6;

    public ObservableList<Reserva> misReservas(Usuario usuario){
        return ReservaDAO.buscarPorUsuario(usuario.getId());
    }

    // Una hora esta completa si ya tiene todas las mesas pilladas ese dia
    public boolean horaCompleta(LocalDate fecha,LocalTime hora){
        return ReservaDAO.contarReservas(fecha,hora)>=MAX_RESERVAS_POR_HORA;
    }

    // Si el dia es hoy y la hora ya ha pasado no se puede elegir
    public boolean horaPasada(LocalDate fecha,LocalTime hora){
        return fecha.equals(LocalDate.now())&&hora.isBefore(LocalTime.now());
    }

    // Mira si el dia elegido se puede reservar, se usa nada mas elegirlo en el calendario y al solicitar
    public void comprobarFecha(LocalDate fecha) throws Exception{
        if(fecha==null)throw new Exception("Elige el día de la reserva en el calendario.");
        if(fecha.isBefore(LocalDate.now()))throw new Exception("Ese día ya ha pasado. Elige otro día.");
        if(fecha.isAfter(LocalDate.now().plusDays(MAX_DIAS)))throw new Exception("Solo se puede reservar con "+MAX_DIAS+" días de antelación como mucho.");
    }

    public Reserva solicitarReserva(Usuario usuario,LocalDate fecha,LocalTime hora,int numPersonas,String observaciones) throws Exception{
        if(usuario==null)throw new Exception("Tienes que iniciar sesión para reservar una mesa.");
        comprobarFecha(fecha);
        if(hora==null)throw new Exception("Elige la hora de la reserva.");
        if(horaPasada(fecha,hora))throw new Exception("Esa hora ya ha pasado. Elige una hora más tarde.");
        if(numPersonas<1||numPersonas>MAX_PERSONAS)throw new Exception("La reserva tiene que ser de 1 a "+MAX_PERSONAS+" personas.");
        String obs=observaciones==null?"":observaciones.trim();
        if(obs.length()>MAX_OBSERVACIONES)throw new Exception("Las observaciones no pueden pasar de "+MAX_OBSERVACIONES+" caracteres.");
        if(horaCompleta(fecha,hora))throw new Exception("No quedan mesas libres a esa hora. Elige otra hora u otro día.");
        // Que el mismo cliente no reserve dos veces a la misma hora el mismo dia
        for(Reserva r:misReservas(usuario)){
            if(!r.estaCancelada()&&r.getFecha().equals(fecha)&&r.getHora().equals(hora))throw new Exception("Ya tienes una reserva ese día a esa hora.");
        }
        return ReservaDAO.insertar(usuario.getId(),fecha,hora,numPersonas,obs);
    }

    // Solo se pueden cancelar las pendientes o confirmadas que todavia no han pasado
    public void comprobarCancelar(Reserva reserva) throws Exception{
        if(reserva==null)throw new Exception("Selecciona en la tabla la reserva que quieres cancelar.");
        if(reserva.estaCancelada())throw new Exception("Esta reserva ya está cancelada.");
        if(reserva.getFecha().isBefore(LocalDate.now()))throw new Exception("No puedes cancelar una reserva de un día que ya ha pasado.");
    }
    public void cancelarReserva(Reserva reserva) throws Exception{
        comprobarCancelar(reserva);
        reserva.setEstado("CANCELADA");
    }
}

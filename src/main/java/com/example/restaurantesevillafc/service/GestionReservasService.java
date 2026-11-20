package com.example.restaurantesevillafc.service;

import com.example.restaurantesevillafc.dao.MesaDAO;
import com.example.restaurantesevillafc.dao.ReservaDAO;
import com.example.restaurantesevillafc.dao.UsuarioDAO;
import com.example.restaurantesevillafc.model.Mesa;
import com.example.restaurantesevillafc.model.Reserva;
import com.example.restaurantesevillafc.model.Usuario;
import javafx.collections.ObservableList;

import java.time.LocalDate;
import java.util.List;

// Lo que hace el trabajador con las reservas que solicitan los clientes desde Reservar mesa
public class GestionReservasService {

    public static final List<String> ESTADOS = List.of("PENDIENTE", "CONFIRMADA", "CANCELADA");

    // Al abrir la pantalla se vuelven a leer las reservas de la base de datos para ver las nuevas
    public ObservableList<Reserva> listarReservas() {
        ReservaDAO.recargar();
        MesaDAO.recargar();
        return ReservaDAO.todos();
    }

    // Texto de la columna Mesa: "Mesa 3" o una raya si todavia no tiene
    public String textoMesa(Reserva r) {
        Integer numero = MesaDAO.numeroDeMesa(r.getId());
        return numero == null || r.estaCancelada() ? "—" : "Mesa " + numero;
    }

    // La mesa que se le pondria al confirmarla (para enseñarla antes de confirmar), null si no hay ninguna libre
    public Mesa mesaLibre(Reserva r) {
        try {
            return MesaDAO.buscarLibre(r.getFecha(), r.getHora(), r.getNumPersonas(), r.getId());
        } catch (RuntimeException e) {
            return null;
        }
    }

    public static String textoEstado(String estado) {
        if (estado == null) return "Todos los estados";
        return switch (estado) {
            case "PENDIENTE" -> "Pendiente";
            case "CONFIRMADA" -> "Confirmada";
            case "CANCELADA" -> "Cancelada";
            default -> estado;
        };
    }

    public String nombreCliente(Reserva r) {
        Usuario u = UsuarioDAO.buscarPorId(r.getIdUsuario());
        return u == null ? "Cliente #" + r.getIdUsuario() : u.getNombre() + " " + u.getApellidos();
    }

    public String contactoCliente(Reserva r) {
        Usuario u = UsuarioDAO.buscarPorId(r.getIdUsuario());
        return u == null ? "" : u.getEmail();
    }

    // Reservas de hoy que no estan canceladas
    public long contarHoy() {
        return ReservaDAO.todos().stream().filter(r -> !r.estaCancelada() && LocalDate.now().equals(r.getFecha())).count();
    }

    public long contar(String estado) {
        return ReservaDAO.todos().stream().filter(r -> estado.equals(r.getEstado())).count();
    }

    // Filtros de la pantalla: dia, estado y texto (nombre o correo del cliente, o numero de reserva)
    public boolean coincide(Reserva r, LocalDate fecha, String estado, String texto) {
        if (fecha != null && !fecha.equals(r.getFecha())) return false;
        if (estado != null && !estado.equals(r.getEstado())) return false;
        if (texto == null || texto.isBlank()) return true;
        String t = texto.trim().toLowerCase().replace("#", "");
        return String.valueOf(r.getId()).contains(t)
                || nombreCliente(r).toLowerCase().contains(t)
                || contactoCliente(r).toLowerCase().contains(t);
    }

    public void confirmar(Reserva r) throws Exception {
        if (r == null) throw new Exception("Selecciona en la tabla la reserva que quieres confirmar.");
        if (r.estaCancelada()) throw new Exception("No se puede confirmar una reserva cancelada.");
        if ("CONFIRMADA".equals(r.getEstado())) throw new Exception("Esta reserva ya está confirmada.");
        if (r.getFecha().isBefore(LocalDate.now())) throw new Exception("Esa reserva es de un día que ya ha pasado.");
        // Se le pone la mesa mas pequeña donde quepan y que este libre ese dia a esa hora
        try {
            Mesa mesa = MesaDAO.buscarLibre(r.getFecha(), r.getHora(), r.getNumPersonas(), r.getId());
            if (mesa == null)
                throw new Exception("No queda ninguna mesa libre para " + r.getNumPersonas() + " personas ese día a esa hora. Cancela la reserva o avisa al cliente.");
            MesaDAO.asignar(r.getId(), mesa);
            r.setEstado("CONFIRMADA");
        } catch (RuntimeException e) {
            throw new Exception(e.getMessage());
        }
    }

    public void cancelar(Reserva r) throws Exception {
        if (r == null) throw new Exception("Selecciona en la tabla la reserva que quieres cancelar.");
        if (r.estaCancelada()) throw new Exception("Esta reserva ya está cancelada.");
        if (r.getFecha().isBefore(LocalDate.now())) throw new Exception("No se puede cancelar una reserva de un día que ya ha pasado.");
        try {
            r.setEstado("CANCELADA");
            // La mesa queda libre para otra reserva
            MesaDAO.asignar(r.getId(), null);
        } catch (RuntimeException e) {
            throw new Exception(e.getMessage());
        }
    }
}

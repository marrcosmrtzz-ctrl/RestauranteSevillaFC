package com.example.restaurantesevillafc.service;

import com.example.restaurantesevillafc.dao.PedidoDAO;
import com.example.restaurantesevillafc.dao.UsuarioDAO;
import com.example.restaurantesevillafc.model.LineaPedido;
import com.example.restaurantesevillafc.model.Pedido;
import com.example.restaurantesevillafc.model.Usuario;
import javafx.collections.ObservableList;

import java.time.format.DateTimeFormatter;
import java.util.List;

// Lo que hace el trabajador con los pedidos que confirman los clientes
public class GestionPedidosService {

    // Los estados van siempre en este orden
    public static final List<String> ESTADOS = List.of("RECIBIDO", "EN_PREPARACION", "LISTO", "ENTREGADO");
    private static final DateTimeFormatter HORA = DateTimeFormatter.ofPattern("HH:mm");

    // Al abrir la pantalla se vuelven a leer los pedidos de la base de datos para ver los nuevos
    public ObservableList<Pedido> listarPedidos() {
        PedidoDAO.recargar();
        return PedidoDAO.todos();
    }

    // Los pedidos que ya hay, sin volver a leerlos
    public ObservableList<Pedido> pedidosActuales() {
        return PedidoDAO.todos();
    }

    // Tarea que vuelve a leer los pedidos en segundo plano para que la pantalla no se quede congelada
    public javafx.concurrent.Task<java.util.List<Pedido>> tareaRecargar() {
        // Los platos se cargan antes aqui, porque los pedidos los necesitan y su lista la usa la pantalla
        com.example.restaurantesevillafc.dao.PlatoDAO.todos();
        javafx.concurrent.Task<java.util.List<Pedido>> tarea = new javafx.concurrent.Task<>() {
            @Override protected java.util.List<Pedido> call() {
                return PedidoDAO.leer();
            }
        };
        return tarea;
    }

    // Pone en la lista los pedidos que ha leido la tarea (en el hilo de la pantalla)
    public void ponerPedidos(java.util.List<Pedido> leidos) {
        PedidoDAO.ponerPedidos(leidos);
    }

    public static boolean estaFinalizado(Pedido pedido) {
        return "ENTREGADO".equals(pedido.getEstado());
    }

    public long contarEnCurso() {
        return PedidoDAO.todos().stream().filter(p -> !estaFinalizado(p)).count();
    }

    public long contarFinalizados() {
        return PedidoDAO.todos().stream().filter(GestionPedidosService::estaFinalizado).count();
    }

    // Texto bonito del estado para la pantalla
    public static String textoEstado(String estado) {
        if (estado == null) return "";
        return switch (estado) {
            case "RECIBIDO" -> "Recibido";
            case "EN_PREPARACION" -> "En preparación";
            case "LISTO" -> "Listo";
            case "ENTREGADO" -> "Entregado";
            default -> estado;
        };
    }

    public String nombreCliente(Pedido pedido) {
        Usuario u = UsuarioDAO.buscarPorId(pedido.getIdUsuario());
        return u == null ? "Cliente #" + pedido.getIdUsuario() : u.getNombre() + " " + u.getApellidos();
    }

    public String hora(Pedido pedido) {
        return pedido.getFechaHora() == null ? "" : pedido.getFechaHora().format(HORA);
    }

    // El buscador busca por numero de pedido o por nombre del cliente
    public boolean coincide(Pedido pedido, String texto) {
        if (texto == null || texto.isBlank()) return true;
        String t = texto.trim().toLowerCase().replace("#", "");
        return String.valueOf(pedido.getId()).contains(t) || nombreCliente(pedido).toLowerCase().contains(t);
    }

    public void cambiarEstado(Pedido pedido, String nuevo) throws Exception {
        if (pedido == null) throw new Exception("Selecciona un pedido de la tabla.");
        if (nuevo == null) throw new Exception("Elige el nuevo estado del pedido.");
        int actual = ESTADOS.indexOf(pedido.getEstado());
        int siguiente = ESTADOS.indexOf(nuevo);
        if (siguiente == actual) throw new Exception("El pedido ya está " + textoEstado(nuevo).toLowerCase() + ".");
        if (siguiente < actual) throw new Exception("Un pedido no puede volver a un estado anterior.");
        pedido.setEstado(nuevo);
    }

    // Texto del ticket para imprimir
    public String ticket(Pedido pedido) {
        StringBuilder sb = new StringBuilder();
        sb.append("RESTAURANTE SEVILLA FC\n");
        sb.append("Pedido #").append(pedido.getId()).append(" · ").append(hora(pedido)).append("\n");
        sb.append("Cliente: ").append(nombreCliente(pedido)).append("\n\n");
        for (LineaPedido l : pedido.getLineas()) {
            sb.append(String.format("%d x %-24s %7.2f €%n", l.getCantidad(), l.getPlato().getNombre(), l.getSubtotal()));
        }
        sb.append(String.format("%nTOTAL %30.2f €", pedido.getTotal()));
        if (pedido.getObservaciones() != null && !pedido.getObservaciones().isBlank()) {
            sb.append("\n\nObservaciones: ").append(pedido.getObservaciones());
        }
        return sb.toString();
    }
}

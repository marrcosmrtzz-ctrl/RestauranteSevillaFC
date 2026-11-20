package com.example.restaurantesevillafc.service;

import com.example.restaurantesevillafc.dao.PlatoDAO;
import com.example.restaurantesevillafc.model.Plato;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.collections.ObservableList;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;

public class PlatoService {

    public static final List<String> CATEGORIAS = List.of("Entrantes", "Principales", "Postres", "Bebidas");

    // ===== Deshacer y rehacer (RF-12) =====
    // Cada cambio sabe deshacerse y volver a hacerse en la base de datos
    private interface Cambio {
        void deshacer();
        void rehacer();
        Plato plato();
        String texto();
    }
    // Antes guarda los cambios hechos y Despues los que se han deshecho, por si quiere rehacerlos.
    // Son static para que no se pierdan al cambiar de seccion en el panel
    private static final Deque<Cambio> ANTES = new ArrayDeque<>();
    private static final Deque<Cambio> DESPUES = new ArrayDeque<>();
    // Solo se recuerdan los ultimos 20 cambios
    private static final int MAX_PASOS = 20;
    private static final BooleanProperty PUEDE_DESHACER = new SimpleBooleanProperty(false);
    private static final BooleanProperty PUEDE_REHACER = new SimpleBooleanProperty(false);

    public static ReadOnlyBooleanProperty puedeDeshacerProperty() { return PUEDE_DESHACER; }
    public static ReadOnlyBooleanProperty puedeRehacerProperty() { return PUEDE_REHACER; }

    private static void apuntar(Cambio cambio) {
        ANTES.push(cambio);
        if (ANTES.size() > MAX_PASOS) ANTES.removeLast();
        // Si hace un cambio nuevo ya no se puede rehacer lo que habia deshecho
        DESPUES.clear();
        actualizarHistorial();
    }
    private static void actualizarHistorial() {
        PUEDE_DESHACER.set(!ANTES.isEmpty());
        PUEDE_REHACER.set(!DESPUES.isEmpty());
    }
    // Deshace el ultimo cambio y devuelve el plato al que afecta para poder seleccionarlo
    public Plato deshacer() throws Exception {
        if (ANTES.isEmpty()) throw new Exception("No hay nada que deshacer.");
        Cambio cambio = ANTES.peek();
        try {
            cambio.deshacer();
        } catch (RuntimeException e) {
            PlatoDAO.recargar();
            throw new Exception("No se ha podido deshacer «" + cambio.texto() + "»: " + e.getMessage());
        }
        DESPUES.push(ANTES.pop());
        actualizarHistorial();
        return cambio.plato();
    }
    public Plato rehacer() throws Exception {
        if (DESPUES.isEmpty()) throw new Exception("No hay nada que rehacer.");
        Cambio cambio = DESPUES.peek();
        try {
            cambio.rehacer();
        } catch (RuntimeException e) {
            PlatoDAO.recargar();
            throw new Exception("No se ha podido rehacer «" + cambio.texto() + "»: " + e.getMessage());
        }
        ANTES.push(DESPUES.pop());
        actualizarHistorial();
        return cambio.plato();
    }
    public String textoDeshacer() { return ANTES.isEmpty() ? "" : ANTES.peek().texto(); }
    public String textoRehacer() { return DESPUES.isEmpty() ? "" : DESPUES.peek().texto(); }
    // Al cerrar sesion se olvida lo que se podia deshacer
    public static void reiniciarHistorial() {
        ANTES.clear();
        DESPUES.clear();
        actualizarHistorial();
    }

    private static Plato copiar(Plato p) {
        return new Plato(p.getId(), p.getNombre(), p.getDescripcion(), p.getAlergenos(), p.getPrecio(), p.isDisponible(), p.getCategoria());
    }
    private static void ponerDatos(Plato destino, Plato datos) {
        destino.setNombre(datos.getNombre());
        destino.setDescripcion(datos.getDescripcion());
        destino.setAlergenos(datos.getAlergenos());
        destino.setPrecio(datos.getPrecio());
        destino.setDisponible(datos.isDisponible());
        destino.setCategoria(datos.getCategoria());
    }

    public ObservableList<Plato> listarPlatos() {
        return PlatoDAO.todos();
    }

    public long contarDisponibles() {
        return PlatoDAO.todos().stream().filter(Plato::isDisponible).count();
    }

    // Crea un plato nuevo despues de validar los datos del formulario
    public Plato crear(String nombre, String descripcion, String categoria, String precioTexto, boolean disponible) throws Exception {
        double precio = validar(null, nombre, categoria, precioTexto);
        Plato plato = new Plato(PlatoDAO.siguienteId(), nombre.trim(), descripcion.trim(), null, precio, disponible, categoria);
        try {
            PlatoDAO.anadir(plato);
        } catch (RuntimeException e) {
            throw new Exception(e.getMessage());
        }
        apuntar(new Cambio() {
            public void deshacer() { PlatoDAO.eliminar(plato); }
            public void rehacer() { PlatoDAO.restaurar(plato); }
            public Plato plato() { return plato; }
            public String texto() { return "Añadir " + plato.getNombre(); }
        });
        return plato;
    }

    // Cambia los datos de un plato que ya existe
    public void modificar(Plato plato, String nombre, String descripcion, String categoria, String precioTexto, boolean disponible) throws Exception {
        double precio = validar(plato, nombre, categoria, precioTexto);
        Plato antes = copiar(plato);
        plato.setNombre(nombre.trim());
        plato.setDescripcion(descripcion.trim());
        plato.setCategoria(categoria);
        plato.setPrecio(precio);
        plato.setDisponible(disponible);
        try {
            PlatoDAO.actualizar(plato);
        } catch (RuntimeException e) {
            // Si no se ha podido guardar, se vuelve a leer la carta para no enseñar datos que no estan en la base de datos
            PlatoDAO.recargar();
            throw new Exception(e.getMessage());
        }
        Plato despues = copiar(plato);
        apuntar(new Cambio() {
            public void deshacer() { ponerDatos(plato, antes); PlatoDAO.actualizar(plato); }
            public void rehacer() { ponerDatos(plato, despues); PlatoDAO.actualizar(plato); }
            public Plato plato() { return plato; }
            public String texto() { return "Modificar " + despues.getNombre(); }
        });
    }

    public void eliminar(Plato plato) throws Exception {
        try {
            PlatoDAO.eliminar(plato);
        } catch (RuntimeException e) {
            throw new Exception(e.getMessage());
        }
        apuntar(new Cambio() {
            public void deshacer() { PlatoDAO.restaurar(plato); }
            public void rehacer() { PlatoDAO.eliminar(plato); }
            public Plato plato() { return plato; }
            public String texto() { return "Eliminar " + plato.getNombre(); }
        });
    }

    // Devuelve el precio ya convertido si todo esta bien, si no lanza una excepcion con el mensaje para el usuario
    private double validar(Plato actual, String nombre, String categoria, String precioTexto) throws Exception {
        if (nombre == null || nombre.isBlank()) throw new Exception("El nombre del plato es obligatorio.");
        if (nombre.trim().length() > 80) throw new Exception("El nombre no puede tener más de 80 caracteres.");
        Plato repetido = PlatoDAO.buscarPorNombre(nombre.trim());
        if (repetido != null && repetido != actual) throw new Exception("Ya hay un plato con ese nombre.");
        if (categoria == null) throw new Exception("Elige una categoría.");
        if (precioTexto == null || precioTexto.isBlank()) throw new Exception("El precio es obligatorio.");
        double precio;
        try {
            precio = Double.parseDouble(precioTexto.trim().replace("€", "").replace(",", ".").trim());
        } catch (NumberFormatException e) {
            throw new Exception("El precio tiene que ser un número, por ejemplo 12,50.");
        }
        if (precio <= 0) throw new Exception("El precio tiene que ser mayor que 0.");
        if (precio > 9999) throw new Exception("El precio es demasiado alto.");
        return Math.round(precio * 100) / 100.0;
    }
}

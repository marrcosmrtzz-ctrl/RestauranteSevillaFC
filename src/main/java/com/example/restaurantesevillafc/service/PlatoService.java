package com.example.restaurantesevillafc.service;

import com.example.restaurantesevillafc.dao.PlatoDAO;
import com.example.restaurantesevillafc.model.Plato;
import javafx.collections.ObservableList;

import java.util.List;

public class PlatoService {

    public static final List<String> CATEGORIAS = List.of("Entrantes", "Principales", "Postres", "Bebidas");

    public ObservableList<Plato> listarPlatos() {
        return PlatoDAO.todos();
    }

    public long contarDisponibles() {
        return PlatoDAO.todos().stream().filter(Plato::isDisponible).count();
    }

    // Crea un plato nuevo despues de validar los datos del formulario
    public Plato crear(String nombre, String descripcion, String categoria, String precioTexto, boolean disponible) throws Exception {
        double precio = validar(null, nombre, categoria, precioTexto);
        Plato plato = new Plato(PlatoDAO.siguienteId(), nombre.trim(), descripcion.trim(), categoria, precio, disponible);
        PlatoDAO.anadir(plato);
        return plato;
    }

    // Cambia los datos de un plato que ya existe
    public void modificar(Plato plato, String nombre, String descripcion, String categoria, String precioTexto, boolean disponible) throws Exception {
        double precio = validar(plato, nombre, categoria, precioTexto);
        plato.setNombre(nombre.trim());
        plato.setDescripcion(descripcion.trim());
        plato.setCategoria(categoria);
        plato.setPrecio(precio);
        plato.setDisponible(disponible);
    }

    public void eliminar(Plato plato) {
        PlatoDAO.eliminar(plato);
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

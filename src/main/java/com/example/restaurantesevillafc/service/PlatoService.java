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
}

package com.example.restaurantesevillafc.dao;

import com.example.restaurantesevillafc.model.Plato;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

// De momento los platos estan en una lista, mas adelante se cogeran de la base de datos
public final class PlatoDAO {

    private static final ObservableList<Plato> PLATOS = FXCollections.observableArrayList(
            new Plato(1, "Salmorejo", "Con jamón ibérico y huevo duro", "Entrantes", 6.50, true),
            new Plato(2, "Croquetas de puchero", "Ración de 8, cremosas por dentro", "Entrantes", 8.00, true),
            new Plato(3, "Gazpacho andaluz", "Fresquito, con su guarnición", "Entrantes", 5.50, true),
            new Plato(4, "Rabo de toro", "Guiso al vino tinto con patatas", "Principales", 15.50, true),
            new Plato(5, "Paella mixta", "Marisco y pollo, mín. 2 personas", "Principales", 13.90, false),
            new Plato(6, "Pescaíto frito", "Adobo, chocos y boquerones", "Principales", 12.50, true),
            new Plato(7, "Torrijas", "De leche, con miel y canela", "Postres", 5.00, true),
            new Plato(8, "Tocino de cielo", "Yema y almíbar, receta jerezana", "Postres", 4.80, true),
            new Plato(9, "Tinto de verano", "Con limón o casera", "Bebidas", 2.80, true),
            new Plato(10, "Agua mineral", "Botella de 50 cl", "Bebidas", 1.80, true));

    private PlatoDAO() {}

    public static ObservableList<Plato> todos() { return PLATOS; }
}

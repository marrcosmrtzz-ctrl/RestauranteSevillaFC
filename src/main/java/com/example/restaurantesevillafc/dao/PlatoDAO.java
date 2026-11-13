package com.example.restaurantesevillafc.dao;
import com.example.restaurantesevillafc.model.Plato;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

// De momento los platos estan en una lista (los mismos del script SQL), mas adelante se cogeran de la base de datos
public final class PlatoDAO {

    private static final ObservableList<Plato> PLATOS=FXCollections.observableArrayList(
            new Plato(1,"Salmorejo","Con jamón ibérico y huevo duro","Gluten, Huevo",6.50,true,"Entrantes"),
            new Plato(2,"Croquetas de puchero","Ración de 8, cremosas por dentro","Gluten, Lácteos",8.00,true,"Entrantes"),
            new Plato(3,"Gazpacho andaluz","Fresquito, con su guarnición",null,5.50,true,"Entrantes"),
            new Plato(4,"Rabo de toro","Guiso al vino tinto con patatas","Sulfitos",15.50,true,"Principales"),
            new Plato(5,"Paella mixta","Marisco y pollo, mín. 2 personas","Crustáceos",13.90,true,"Principales"),
            new Plato(6,"Pescaíto frito","Adobo, chocos y boquerones","Pescado, Gluten",12.50,true,"Principales"),
            new Plato(7,"Torrijas","De leche, con miel y canela","Gluten, Huevo",5.00,true,"Postres"),
            new Plato(8,"Tocino de cielo","Yema y almíbar, receta jerezana","Huevo",4.80,true,"Postres"),
            new Plato(9,"Tinto de verano","Con limón o casera","Sulfitos",2.80,true,"Bebidas"),
            new Plato(10,"Agua mineral","Botella de 50 cl",null,1.80,true,"Bebidas"));
    private PlatoDAO(){}
    public static ObservableList<Plato> todos(){return PLATOS;}
    public static Plato buscarPorId(int id){
        for(Plato p:PLATOS){
            if(p.getId()==id)return p;
        }
        return null;
    }
}

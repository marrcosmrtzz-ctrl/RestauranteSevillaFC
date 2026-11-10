module com.example.restaurantesevillafc {
    requires javafx.controls;
    requires javafx.fxml;

    opens com.example.restaurantesevillafc to javafx.fxml;
    opens com.example.restaurantesevillafc.controller to javafx.fxml;
    opens com.example.restaurantesevillafc.model to javafx.base;

    exports com.example.restaurantesevillafc;
}

package com.example.restaurantesevillafc;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class App extends Application {

    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader loader = new FXMLLoader(App.class.getResource("fxml/login.fxml"));
        Scene scene = new Scene(loader.load(), 1000, 640);
        scene.getStylesheets().add(App.class.getResource("css/estilos.css").toExternalForm());
        stage.setTitle("Restaurante Sevilla FC");
        stage.setMinWidth(820);
        stage.setMinHeight(560);
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}

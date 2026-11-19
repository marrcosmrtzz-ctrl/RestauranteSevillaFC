package com.example.restaurantesevillafc.controller;
import com.example.restaurantesevillafc.App;
import com.example.restaurantesevillafc.model.Usuario;
import com.example.restaurantesevillafc.service.UsuarioService;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.*;
import java.io.IOException;
public class LoginController {
    @FXML private TextField txtCorreo;
    @FXML private PasswordField txtPassword;
    @FXML private ToggleButton btnCliente,btnTrabajador;
    @FXML private ToggleGroup grupoRol;
    @FXML private Label lblTitulo,lblSubtitulo,lblError;
    @FXML private Button btnEntrar;
    @FXML private Hyperlink linkRegistro;
    private final UsuarioService usuarioService=new UsuarioService();

    @FXML public void initialize(){
        btnEntrar.disableProperty().bind(txtCorreo.textProperty().isEmpty().or(txtPassword.textProperty().isEmpty()));
        grupoRol.selectedToggleProperty().addListener((obs,antes,ahora)->{if(ahora==null)antes.setSelected(true);});
        txtCorreo.textProperty().addListener((obs,a,b)->ocultarError());
        txtPassword.textProperty().addListener((obs,a,b)->ocultarError());
    }
    @FXML private void cambiarRol(){
        boolean trabajador=btnTrabajador.isSelected();
        lblTitulo.setText(trabajador?"Acceso del personal":"Bienvenido de nuevo");
        lblSubtitulo.setText(trabajador?"Entra con tu cuenta de trabajador":"Inicia sesión para continuar");
        linkRegistro.setVisible(!trabajador);
        ocultarError();
    }
    @FXML private void entrar(){
        try{
            Usuario u=usuarioService.iniciarSesion(txtCorreo.getText(),txtPassword.getText(),btnTrabajador.isSelected());
            if(u.esTrabajador()){
                abrirPanelTrabajador(u);
            }else{
                abrirPantalla("fxml/carta.fxml");
            }
        }catch(Exception e){
            lblError.setText(e.getMessage());
            lblError.setVisible(true);
            lblError.setManaged(true);
        }
    }
    @FXML private void irARegistro(){abrirPantalla("fxml/registro.fxml");}
    // Cambia lo que se ve en la ventana por otra pantalla
    private void abrirPantalla(String fxml){
        try{ txtCorreo.getScene().setRoot(new FXMLLoader(App.class.getResource(fxml)).load()); }
        catch(Exception e){ new Alert(Alert.AlertType.ERROR,"No se pudo abrir la pantalla: "+e.getMessage()).showAndWait(); }
    }
    @FXML private void salir(){
        Alert confirm=new Alert(Alert.AlertType.CONFIRMATION,"¿Seguro que quieres salir?",ButtonType.YES,ButtonType.NO);
        confirm.setHeaderText("Salir");
        confirm.showAndWait().filter(b->b==ButtonType.YES).ifPresent(b->Platform.exit());
    }
    @FXML private void acercaDe(){
        Alert info=new Alert(Alert.AlertType.INFORMATION,"Proyecto de Desarrollo de Interfaces · 2.º DAM\nEquipo Sevilla FC");
        info.setHeaderText("Restaurante Sevilla FC");
        info.showAndWait();
    }
    private void abrirPanelTrabajador(Usuario u){
        try{
            FXMLLoader loader=new FXMLLoader(App.class.getResource("fxml/trabajador.fxml"));
            Parent panel=loader.load();
            loader.<TrabajadorController>getController().setUsuario(u);
            btnEntrar.getScene().setRoot(panel);
        }catch(IOException e){
            new Alert(Alert.AlertType.ERROR,"No se ha podido abrir el panel del trabajador.").showAndWait();
        }
    }
    private void ocultarError(){lblError.setVisible(false);lblError.setManaged(false);}
}

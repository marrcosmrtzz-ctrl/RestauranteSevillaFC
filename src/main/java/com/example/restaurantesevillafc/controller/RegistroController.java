package com.example.restaurantesevillafc.controller;
import com.example.restaurantesevillafc.App;
import com.example.restaurantesevillafc.config.Tema;
import com.example.restaurantesevillafc.model.Usuario;
import com.example.restaurantesevillafc.service.UsuarioService;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.*;
import javafx.scene.layout.Region;
import java.io.IOException;
public class RegistroController {
    @FXML private TextField txtNombre,txtApellidos,txtCorreo,txtTelefono;
    @FXML private PasswordField txtPassword,txtRepetir;
    @FXML private Label lblCoinciden,lblError;
    @FXML private Region seg1,seg2,seg3,seg4;
    @FXML private CheckBox chkPrivacidad;
    @FXML private Button btnCrear;
    @FXML private CheckMenuItem itemTemaOscuro;
    @FXML private MenuItem itemAumentarLetra,itemReducirLetra,itemLetraNormal;
    private final UsuarioService usuarioService=new UsuarioService();

    @FXML public void initialize(){
        actualizarMenuTema();
        // Binding: Crear cuenta desactivado hasta aceptar la política de privacidad
        btnCrear.disableProperty().bind(chkPrivacidad.selectedProperty().not());
        // Binding: el aviso verde solo se ve cuando las dos contraseñas son iguales
        lblCoinciden.visibleProperty().bind(txtRepetir.textProperty().isNotEmpty()
                .and(txtRepetir.textProperty().isEqualTo(txtPassword.textProperty())));
        // Listener: cada vez que cambia la contraseña se repinta la barra de seguridad
        txtPassword.textProperty().addListener((obs,a,b)->actualizarSeguridad());
        for(TextField t:new TextField[]{txtNombre,txtApellidos,txtCorreo,txtTelefono,txtPassword,txtRepetir})
            t.textProperty().addListener((obs,a,b)->ocultarError());
    }
    @FXML private void crearCuenta(){
        try{
            Usuario u=usuarioService.registrar(txtNombre.getText(),txtApellidos.getText(),txtCorreo.getText(),
                    txtTelefono.getText(),txtPassword.getText(),txtRepetir.getText(),chkPrivacidad.isSelected());
            Alert ok=new Alert(Alert.AlertType.INFORMATION,"Tu cuenta se ha guardado. Ya puedes pedir desde la carta.");
            ok.setHeaderText("¡Cuenta creada, "+u.getNombre()+"!");
            ok.showAndWait();
            // Como en el Figma, al crear la cuenta se abre la carta
            abrirPantalla("fxml/carta.fxml");
        }catch(Exception e){
            lblError.setText(e.getMessage());
            lblError.setVisible(true);
            lblError.setManaged(true);
        }
    }
    // Vuelve al inicio de sesión (botón Volver y enlace "Inicia sesión")
    @FXML private void volver(){abrirPantalla("fxml/login.fxml");}
    // Cambia lo que se ve en la ventana por otra pantalla
    private void abrirPantalla(String fxml){
        try{ txtNombre.getScene().setRoot(new FXMLLoader(App.class.getResource(fxml)).load()); }
        catch(IOException e){ new Alert(Alert.AlertType.ERROR,"No se pudo abrir la pantalla.").showAndWait(); }
    }
    @FXML private void verPrivacidad(){
        Alert info=new Alert(Alert.AlertType.INFORMATION,"Tus datos solo se usan para gestionar tus pedidos y reservas en el restaurante.");
        info.setHeaderText("Política de privacidad");
        info.showAndWait();
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
    // Pinta la barra de 4 tramos: rojo (débil), naranja (media), verde (fuerte)
    private void actualizarSeguridad(){
        int nivel=usuarioService.nivelSeguridad(txtPassword.getText());
        String color=nivel<=1?"#c8102e":nivel==2?"#e3a008":"#1e7b45";
        Region[] segs={seg1,seg2,seg3,seg4};
        for(int i=0;i<4;i++)segs[i].setStyle(i<nivel?"-fx-background-color:"+color+";":"");
    }
    // Menu Ver: tema oscuro y tamano de letra con la clase Tema de Rubén (se mantiene al cambiar de pantalla)
    private void actualizarMenuTema(){
        itemTemaOscuro.setSelected(Tema.isOscuro());
        itemAumentarLetra.setDisable(Tema.esLetraMaxima());
        itemReducirLetra.setDisable(Tema.esLetraMinima());
        itemLetraNormal.setDisable(Tema.esLetraMinima());
    }
    @FXML private void cambiarTema(){
        Tema.cambiarTema(txtNombre.getScene());
        actualizarMenuTema();
    }
    @FXML private void aumentarLetra(){
        Tema.aumentarLetra(txtNombre.getScene());
        actualizarMenuTema();
    }
    @FXML private void reducirLetra(){
        Tema.reducirLetra(txtNombre.getScene());
        actualizarMenuTema();
    }
    @FXML private void letraNormal(){
        Tema.letraNormal(txtNombre.getScene());
        actualizarMenuTema();
    }
    // Ayuda (F1): explica en pocas lineas como se usa esta pantalla
    @FXML private void verAyuda(){
        Alert ayuda=new Alert(Alert.AlertType.INFORMATION,"1. Rellena los campos con * (nombre, apellidos, correo y contraseña). El teléfono no es obligatorio.\n2. La contraseña tiene que tener mínimo 8 caracteres, una mayúscula y un número. La barra de colores te dice si es segura y debajo verás si las dos contraseñas coinciden.\n3. Marca la casilla de la política de privacidad y pulsa «Crear cuenta».\n\nAl crear la cuenta entras directamente a la carta. Con «Volver» regresas al inicio de sesión.\n\nEn el menú Ver puedes poner el tema oscuro (Ctrl+T) y agrandar o reducir la letra (Ctrl + y Ctrl -).");
        ayuda.setHeaderText("Cómo crear una cuenta");
        ayuda.setTitle("Ayuda");
        // Para que la ventana crezca y se lea todo el texto
        ayuda.getDialogPane().setMinHeight(Region.USE_PREF_SIZE);
        ayuda.showAndWait();
    }
    private void ocultarError(){lblError.setVisible(false);lblError.setManaged(false);}
}

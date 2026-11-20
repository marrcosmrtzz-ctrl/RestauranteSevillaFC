package com.example.restaurantesevillafc.config;
import com.example.restaurantesevillafc.App;
import javafx.scene.Scene;
import javafx.collections.ObservableList;

// Guarda el tema y el tamano de letra que ha elegido el usuario y se los pone a la ventana
// Es static para que se mantenga al cambiar de pantalla, igual que el usuario que ha entrado
public final class Tema {

    // Los tamanos de letra que hay, cada uno es un CSS que agranda las letras del CSS normal
    private static final String[] NOMBRES_LETRA={"Normal","Grande","Muy grande"};
    private static final String[] CSS_LETRA={null,"css/letra_grande.css","css/letra_muy_grande.css"};
    private static boolean oscuro=false;
    private static int letra=0;
    private Tema(){}

    // Le pone el tema a la ventana y a la pantalla que se esta viendo
    public static void aplicar(Scene scene){
        if(scene==null)return;
        ponerHojas(scene.getStylesheets());
        // Algunas pantallas traen estilos.css en su FXML para verse en Scene Builder, y eso manda sobre la ventana
        // asi que el tema tambien se pone en la pantalla
        if(scene.getRoot()!=null)ponerHojas(scene.getRoot().getStylesheets());
        // La primera vez nos apuntamos para volver a ponerlo cada vez que se cambia de pantalla
        if(!scene.getProperties().containsKey("tema")){
            scene.getProperties().put("tema",true);
            scene.rootProperty().addListener((obs,antes,ahora)->{
                if(ahora!=null)ponerHojas(ahora.getStylesheets());
            });
        }
    }
    // Deja el CSS normal y encima el del tema oscuro y el de la letra si hacen falta
    private static void ponerHojas(ObservableList<String> hojas){
        hojas.setAll(ruta("css/estilos.css"));
        if(oscuro)hojas.add(ruta("css/tema_oscuro.css"));
        if(letra>0)hojas.add(ruta(CSS_LETRA[letra]));
    }
    private static String ruta(String css){
        return App.class.getResource(css).toExternalForm();
    }
    public static void cambiarTema(Scene scene){
        oscuro=!oscuro;
        aplicar(scene);
    }
    public static void aumentarLetra(Scene scene){
        if(letra<CSS_LETRA.length-1)letra++;
        aplicar(scene);
    }
    public static void reducirLetra(Scene scene){
        if(letra>0)letra--;
        aplicar(scene);
    }
    public static void letraNormal(Scene scene){
        letra=0;
        aplicar(scene);
    }
    public static boolean isOscuro(){return oscuro;}
    public static String getNombreLetra(){return NOMBRES_LETRA[letra];}
    public static boolean esLetraMaxima(){return letra==CSS_LETRA.length-1;}
    public static boolean esLetraMinima(){return letra==0;}
}

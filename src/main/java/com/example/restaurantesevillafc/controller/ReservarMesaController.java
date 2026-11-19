package com.example.restaurantesevillafc.controller;
import com.example.restaurantesevillafc.App;
import com.example.restaurantesevillafc.dao.UsuarioDAO;
import com.example.restaurantesevillafc.model.Reserva;
import com.example.restaurantesevillafc.model.Usuario;
import com.example.restaurantesevillafc.service.PedidoService;
import com.example.restaurantesevillafc.service.ReservaService;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
public class ReservarMesaController {
    @FXML private TableView<Reserva> tablaReservas;
    @FXML private TableColumn<Reserva,String> colFecha,colHora,colEstado;
    @FXML private TableColumn<Reserva,Number> colPersonas;
    @FXML private Label lblNumReservas,lblNumPlatos,lblSaludo,lblInicial,lblPersonas,lblContador,lblResumen;
    @FXML private DatePicker dpFecha;
    @FXML private ToggleGroup grupoHoras;
    @FXML private Button btnMenosPersonas,btnMasPersonas,btnSolicitar,btnCancelar;
    @FXML private TextField txtObservaciones;
    private final ReservaService reservaService=new ReservaService();
    private final PedidoService pedidoService=new PedidoService();
    private final DateTimeFormatter formatoFecha=DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private final DateTimeFormatter formatoHora=DateTimeFormatter.ofPattern("HH:mm");
    // Nombres de los dias y los meses para escribir el resumen en castellano
    private final String[] DIAS={"Lunes","Martes","Miércoles","Jueves","Viernes","Sábado","Domingo"};
    private final String[] MESES={"enero","febrero","marzo","abril","mayo","junio","julio","agosto","septiembre","octubre","noviembre","diciembre"};
    private Usuario usuario;
    private int personas=2;

    @FXML public void initialize(){
        colFecha.setCellValueFactory(d->new SimpleStringProperty(d.getValue().getFecha().format(formatoFecha)));
        colHora.setCellValueFactory(d->new SimpleStringProperty(d.getValue().getHora().format(formatoHora)));
        colPersonas.setCellValueFactory(d->d.getValue().numPersonasProperty());
        colEstado.setCellValueFactory(d->new SimpleStringProperty(textoEstado(d.getValue().getEstado())));
        tablaReservas.setPlaceholder(new Label("Todavía no has hecho ninguna reserva."));
        // El boton de cancelar solo se puede usar si hay una reserva seleccionada
        btnCancelar.disableProperty().bind(tablaReservas.getSelectionModel().selectedItemProperty().isNull());
        // Cuando elige un dia miramos si vale y que horas quedan libres
        dpFecha.valueProperty().addListener((obs,antes,ahora)->{
            if(ahora!=null){
                try{
                    reservaService.comprobarFecha(ahora);
                }catch(Exception e){
                    new Alert(Alert.AlertType.WARNING,e.getMessage()).showAndWait();
                    dpFecha.setValue(antes);
                    return;
                }
            }
            actualizarHoras();
            actualizarResumen();
        });
        // Cada vez que elige o quita una hora se cambia el resumen
        grupoHoras.selectedToggleProperty().addListener((obs,antes,ahora)->actualizarResumen());
        // Contador de las observaciones, no deja escribir mas de 255 caracteres
        txtObservaciones.textProperty().addListener((obs,antes,ahora)->{
            if(ahora.length()>ReservaService.MAX_OBSERVACIONES)txtObservaciones.setText(antes);
            else lblContador.setText(ahora.length()+" / "+ReservaService.MAX_OBSERVACIONES);
        });
        // En el badge de Mi pedido sale cuantos platos lleva el cliente
        lblNumPlatos.setText(String.valueOf(pedidoService.totalArticulos()));
        // De momento, hasta que el login abra esta pantalla, usamos el cliente de prueba
        setUsuario(UsuarioDAO.buscarPorEmail("lucia.garcia@correo.com"));
        actualizarPersonas();
        actualizarResumen();
    }
    public void setUsuario(Usuario usuario){
        this.usuario=usuario;
        if(usuario!=null){
            lblSaludo.setText("Hola, "+usuario.getNombre());
            lblInicial.setText(usuario.getNombre().substring(0,1));
            cargarReservas();
        }
    }
    // Vuelve a pedir las reservas del cliente y las pone en la tabla
    private void cargarReservas(){
        tablaReservas.setItems(reservaService.misReservas(usuario));
        int num=tablaReservas.getItems().size();
        lblNumReservas.setText(num+(num==1?" reserva":" reservas"));
    }
    // Pasa el estado de la base de datos a un texto mas bonito para el cliente
    private String textoEstado(String estado){
        if(estado.equals("PENDIENTE"))return "● Pendiente";
        if(estado.equals("CONFIRMADA"))return "● Confirmada";
        return "● Cancelada";
    }
    // La hora del boton sale de su texto, por ejemplo las nueve de la noche
    private LocalTime horaDe(Toggle toggle){
        return LocalTime.parse(((ToggleButton)toggle).getText());
    }
    // Tacha las horas que ya han pasado o que no tienen mesas libres ese dia
    private void actualizarHoras(){
        LocalDate fecha=dpFecha.getValue();
        for(Toggle t:grupoHoras.getToggles()){
            ToggleButton boton=(ToggleButton)t;
            boolean noSePuede=fecha!=null&&(reservaService.horaPasada(fecha,horaDe(t))||reservaService.horaCompleta(fecha,horaDe(t)));
            boton.setDisable(noSePuede);
            // Si tenia elegida una hora que ya no se puede, se la quitamos
            if(noSePuede&&boton.isSelected())boton.setSelected(false);
        }
    }
    // Escribe el resumen de la reserva y activa el boton solo si ya tiene dia y hora
    private void actualizarResumen(){
        LocalDate fecha=dpFecha.getValue();
        Toggle hora=grupoHoras.getSelectedToggle();
        if(fecha==null||hora==null){
            lblResumen.setText(fecha==null?"Elige el día y la hora de tu reserva":"Ahora elige la hora de tu reserva");
            btnSolicitar.setDisable(true);
            return;
        }
        String dia=DIAS[fecha.getDayOfWeek().getValue()-1]+" "+fecha.getDayOfMonth()+" de "+MESES[fecha.getMonthValue()-1];
        lblResumen.setText(dia+" · "+((ToggleButton)hora).getText()+" · "+personas+(personas==1?" persona":" personas"));
        btnSolicitar.setDisable(false);
    }
    // El menos no baja de 1 y el mas no pasa de 10
    private void actualizarPersonas(){
        lblPersonas.setText(String.valueOf(personas));
        btnMenosPersonas.setDisable(personas<=1);
        btnMasPersonas.setDisable(personas>=ReservaService.MAX_PERSONAS);
        actualizarResumen();
    }
    @FXML private void menosPersonas(){
        if(personas>1)personas--;
        actualizarPersonas();
    }
    @FXML private void masPersonas(){
        if(personas<ReservaService.MAX_PERSONAS)personas++;
        actualizarPersonas();
    }
    @FXML private void solicitar(){
        Alert confirm=new Alert(Alert.AlertType.CONFIRMATION,lblResumen.getText()+"\nEl restaurante te la confirmará lo antes posible.",ButtonType.YES,ButtonType.NO);
        confirm.setHeaderText("¿Solicitar esta reserva?");
        confirm.showAndWait().filter(b->b==ButtonType.YES).ifPresent(b->enviarReserva());
    }
    private void enviarReserva(){
        try{
            Reserva reserva=reservaService.solicitarReserva(usuario,dpFecha.getValue(),horaDe(grupoHoras.getSelectedToggle()),personas,txtObservaciones.getText());
            Alert info=new Alert(Alert.AlertType.INFORMATION,lblResumen.getText()+"\nCuando el restaurante la confirme lo verás en Mis reservas.");
            info.setHeaderText("¡Reserva solicitada!");
            // Dejamos el formulario limpio para otra reserva
            grupoHoras.selectToggle(null);
            txtObservaciones.clear();
            personas=2;
            cargarReservas();
            actualizarHoras();
            actualizarPersonas();
            tablaReservas.getSelectionModel().select(reserva);
            info.showAndWait();
        }catch(Exception e){
            new Alert(Alert.AlertType.WARNING,e.getMessage()).showAndWait();
        }
    }
    @FXML private void cancelar(){
        Reserva reserva=tablaReservas.getSelectionModel().getSelectedItem();
        // Primero miramos si se puede cancelar, para no preguntar si luego no se va a poder
        try{
            reservaService.comprobarCancelar(reserva);
        }catch(Exception e){
            new Alert(Alert.AlertType.WARNING,e.getMessage()).showAndWait();
            return;
        }
        Alert confirm=new Alert(Alert.AlertType.CONFIRMATION,
                reserva.getFecha().format(formatoFecha)+" a las "+reserva.getHora().format(formatoHora)+" · "+reserva.getNumPersonas()+(reserva.getNumPersonas()==1?" persona":" personas")+"\nUna vez cancelada no se puede recuperar.",
                ButtonType.YES,ButtonType.NO);
        confirm.setHeaderText("¿Cancelar esta reserva?");
        confirm.showAndWait().filter(b->b==ButtonType.YES).ifPresent(b->{
            try{
                reservaService.cancelarReserva(reserva);
                tablaReservas.refresh();
                // Al cancelar queda una mesa libre, asi que puede que una hora tachada vuelva a estar libre
                actualizarHoras();
            }catch(Exception e){
                new Alert(Alert.AlertType.WARNING,e.getMessage()).showAndWait();
            }
        });
    }
    // Carga otra pantalla en la misma ventana y devuelve su controlador para pasarle datos
    private Object abrirPantalla(String fxml){
        try{
            FXMLLoader loader=new FXMLLoader(App.class.getResource("fxml/"+fxml));
            Parent root=loader.load();
            Stage stage=(Stage)tablaReservas.getScene().getWindow();
            Scene scene=new Scene(root,stage.getScene().getWidth(),stage.getScene().getHeight());
            // Le ponemos los estilos igual que en App, porque el login no los trae en su FXML
            scene.getStylesheets().add(App.class.getResource("css/estilos.css").toExternalForm());
            stage.setScene(scene);
            return loader.getController();
        }catch(Exception e){
            new Alert(Alert.AlertType.ERROR,"No se ha podido abrir la pantalla.").showAndWait();
            return null;
        }
    }
    @FXML private void irACarta(){abrirPantalla("carta.fxml");}
    @FXML private void irAMiPedido(){
        MiPedidoController controlador=(MiPedidoController)abrirPantalla("mi_pedido.fxml");
        if(controlador!=null)controlador.setUsuario(usuario);
    }
    @FXML private void cerrarSesion(){
        Alert confirm=new Alert(Alert.AlertType.CONFIRMATION,"¿Seguro que quieres cerrar sesión?",ButtonType.YES,ButtonType.NO);
        confirm.setHeaderText("Cerrar sesión");
        confirm.showAndWait().filter(b->b==ButtonType.YES).ifPresent(b->abrirPantalla("login.fxml"));
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
}

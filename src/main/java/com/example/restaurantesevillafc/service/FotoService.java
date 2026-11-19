package com.example.restaurantesevillafc.service;

import com.example.restaurantesevillafc.App;
import com.example.restaurantesevillafc.model.Plato;
import javafx.scene.image.Image;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;

// Fotos de los platos. Las de la carta vienen en resources/images/platos (plato1.jpg, plato1_mini.jpg...)
// y las que elige el trabajador desde la gestion del menu se guardan en la carpeta fotos/ del proyecto
public class FotoService {

    private static final Path CARPETA = Paths.get("fotos");
    private static final List<String> EXTENSIONES = List.of("jpg", "jpeg", "png");
    private static final long MAX_BYTES = 5 * 1024 * 1024;

    // Devuelve la foto del plato o null si no tiene. Primero mira si el trabajador ha puesto una nueva
    public Image cargar(Plato plato, boolean mini) {
        Path elegida = buscarElegida(plato.getId());
        if (elegida != null) return new Image(elegida.toUri().toString());
        URL foto = App.class.getResource("images/platos/plato" + plato.getId() + (mini ? "_mini" : "") + ".jpg");
        return foto == null ? null : new Image(foto.toExternalForm());
    }

    // Comprueba que el archivo es una imagen valida antes de usarlo
    public void validar(File archivo) throws Exception {
        if (archivo == null || !archivo.isFile()) throw new Exception("No se ha encontrado la imagen.");
        if (!EXTENSIONES.contains(extension(archivo.getName()))) throw new Exception("La foto tiene que ser JPG o PNG.");
        if (archivo.length() > MAX_BYTES) throw new Exception("La foto no puede pasar de 5 MB.");
    }

    // Copia la foto elegida a fotos/plato<id>.<extension>, quitando la anterior si la habia
    public void guardar(Plato plato, File archivo) throws Exception {
        validar(archivo);
        try {
            Files.createDirectories(CARPETA);
            eliminar(plato);
            Path destino = CARPETA.resolve("plato" + plato.getId() + "." + extension(archivo.getName()));
            Files.copy(archivo.toPath(), destino, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            throw new Exception("No se ha podido guardar la foto: " + e.getMessage());
        }
    }

    // Al borrar un plato se borra tambien la foto que eligio el trabajador
    public void eliminar(Plato plato) {
        Path elegida = buscarElegida(plato.getId());
        if (elegida == null) return;
        try {
            Files.deleteIfExists(elegida);
        } catch (IOException e) {
            // Si no se puede borrar no pasa nada, la foto se queda en la carpeta
        }
    }

    private Path buscarElegida(int idPlato) {
        for (String ext : EXTENSIONES) {
            Path p = CARPETA.resolve("plato" + idPlato + "." + ext);
            if (Files.exists(p)) return p;
        }
        return null;
    }

    private String extension(String nombre) {
        int punto = nombre.lastIndexOf('.');
        return punto < 0 ? "" : nombre.substring(punto + 1).toLowerCase();
    }
}

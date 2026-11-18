package com.example.restaurantesevillafc.service;
import com.example.restaurantesevillafc.dao.UsuarioDAO;
import com.example.restaurantesevillafc.model.Usuario;
public class UsuarioService {

    public Usuario iniciarSesion(String email,String password,boolean esTrabajador) throws Exception{
        if(email.isBlank()||password.isBlank())throw new Exception("Rellena el correo y la contraseña.");
        if(!email.matches("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$"))throw new Exception("El correo no tiene un formato válido.");
        Usuario usuario=UsuarioDAO.buscarPorEmail(email.trim());
        if(usuario==null||!usuario.getPassword().equals(password))throw new Exception("Usuario o contraseña incorrectos.");
        if(esTrabajador!=usuario.esTrabajador())throw new Exception(esTrabajador?"Esta cuenta no es de trabajador.":"Esta cuenta es de trabajador, entra desde la pestaña Trabajador.");
        return usuario;
    }

    public Usuario registrar(String nombre,String apellidos,String email,String telefono,String password,String repetir,boolean aceptaPrivacidad) throws Exception{
        if(nombre.isBlank()||apellidos.isBlank()||email.isBlank()||password.isBlank()||repetir.isBlank())
            throw new Exception("Rellena todos los campos obligatorios (*).");
        if(!email.trim().matches("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$"))throw new Exception("El correo no tiene un formato válido.");
        if(!telefono.isBlank()&&!telefono.replace(" ","").matches("^(\\+34)?[6789]\\d{8}$"))throw new Exception("El teléfono no es válido.");
        if(!password.matches("^(?=.*[A-Z])(?=.*\\d).{8,}$"))throw new Exception("La contraseña debe tener mínimo 8 caracteres, una mayúscula y un número.");
        if(!password.equals(repetir))throw new Exception("Las contraseñas no coinciden.");
        if(!aceptaPrivacidad)throw new Exception("Tienes que aceptar la política de privacidad.");
        if(UsuarioDAO.buscarPorEmail(email.trim())!=null)throw new Exception("Ya existe una cuenta con ese correo.");
        Usuario nuevo=new Usuario(0,nombre.trim(),apellidos.trim(),email.trim().toLowerCase(),password,"CLIENTE");
        nuevo.setTelefono(telefono.replace(" ",""));
        UsuarioDAO.insertar(nuevo);
        return nuevo;
    }

    // Nivel de seguridad de la contraseña (0 a 4) para la barra del registro
    public int nivelSeguridad(String p){
        if(p==null||p.isEmpty())return 0;
        int n=0;
        if(p.length()>=8)n++;
        if(p.matches(".*[A-Z].*"))n++;
        if(p.matches(".*\\d.*"))n++;
        if(p.matches(".*[^A-Za-z0-9].*"))n++;
        return n;
    }
}

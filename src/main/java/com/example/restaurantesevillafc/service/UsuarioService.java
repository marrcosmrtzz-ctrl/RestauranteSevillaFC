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
}

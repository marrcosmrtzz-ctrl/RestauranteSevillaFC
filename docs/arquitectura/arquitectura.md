# Arquitectura

Usamos MVC por capas. Cada capa solo llama a la siguiente:

```
Vista (FXML + CSS)  ->  controller  ->  service  ->  dao  ->  base de datos
```

Las clases de `model` (Usuario...) se usan en todas las capas.

| Paquete | Para qué sirve |
|---|---|
| fxml/ y css/ | Las pantallas y los estilos. No tienen lógica ni SQL. |
| controller/ | Un controlador por pantalla. Recoge los datos y muestra los avisos. |
| model/ | Las clases de los datos (Usuario, Plato, Reserva...). |
| service/ | Validaciones (campos vacíos, formato del correo, contraseña...). |
| dao/ | Acceso a los datos. Es el único sitio donde habrá SQL. |
| config/ | Aquí irá la conexión con la base de datos. |

Ningún controlador hace SQL.

Ejemplo con el login: `LoginController` coge el correo y la contraseña y llama a `UsuarioService`, que valida los campos y busca el usuario con `UsuarioDAO`. Si algo está mal, el controlador enseña el mensaje de error en rojo.

Por ahora `UsuarioDAO` tiene los usuarios en una lista. Cuando conectemos la base de datos solo habrá que cambiar el DAO y añadir la conexión en `config`.

# Restaurante Sevilla FC

Aplicación de escritorio en JavaFX para gestionar un restaurante. Proyecto de Desarrollo de Interfaces, 2.º DAM.

Equipo Sevilla FC: Marcos Martínez, Juan de la Puerta, Eduardo Contreras y Rubén Fernández.

## Tecnologías

- Java 21 y JavaFX 21 (FXML con Scene Builder y CSS)
- Maven
- MySQL (se conectará en los siguientes sprints)

## Cómo clonar y ejecutar

1. Clonar el repositorio:

   ```
   git clone [enlace del repositorio]
   ```

2. Abrir la carpeta en IntelliJ (File > Open) y esperar a que Maven descargue las dependencias.

3. Ejecutar la clase `App`, o en la pestaña de Maven: Plugins > javafx > javafx:run.

## Usuarios para probar el login

De momento están en una lista dentro de `UsuarioDAO`, todavía no hay base de datos.

| Rol | Correo | Contraseña |
|---|---|---|
| Trabajador | antonio.ruiz@sevillafc.es | 1234 |
| Cliente | lucia.garcia@correo.com | 1234 |

## Estructura

```
com/example/restaurantesevillafc/
├── App.java
├── config/
├── model/
├── dao/
├── service/
└── controller/
```

Las vistas están en `resources/.../fxml/` y los estilos en `resources/.../css/`.

## Cómo trabajamos

- `main` es la rama estable.
- Cada tarea en su rama (`feature/login`...).
- Al terminar se hace un pull request y lo revisa otro compañero.

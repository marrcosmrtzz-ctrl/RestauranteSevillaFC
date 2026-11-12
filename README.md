# Restaurante Sevilla FC

Aplicación de escritorio en JavaFX para gestionar un restaurante. Proyecto de Desarrollo de Interfaces, 2.º DAM.

Equipo Sevilla FC: Marcos Martínez, Juan de la Puerta, Eduardo Contreras y Rubén Fernández.

Tablero de Trello: https://trello.com/b/kv6CVTfn

## Tecnologías

- Java 21 y JavaFX 21 (FXML con Scene Builder y CSS)
- Maven
- MySQL (se conectará en los siguientes sprints)

## Cómo clonar y ejecutar

1. Clonar el repositorio:

   ```
   git clone https://github.com/marrcosmrtzz-ctrl/RestauranteSevillaFC.git
   ```

   También se puede hacer desde IntelliJ: File > New > Project from Version Control y pegar la URL.

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

- `main` es la rama estable y nadie sube directamente a ella.
- Cada integrante tiene su propia rama:

  | Integrante | Rama |
  |---|---|
  | Marcos Martínez | `marcos` |
  | Juan de la Puerta | `juanpuerta` |
  | Eduardo Contreras | `eduardo` |
  | Rubén Fernández | `ruben` |

- Antes de empezar una tarea, cada uno actualiza su rama con lo último de `main`.
- Al terminar, abre un pull request de su rama hacia `main` y lo revisa otro compañero antes de juntarlo.
- Los datos de conexión a la base de datos irán en `config.properties`, que está en el `.gitignore` para no subir contraseñas.

# Restaurante Sevilla FC

Aplicación de escritorio en JavaFX para gestionar un restaurante. Proyecto de Desarrollo de Interfaces, 2.º DAM.

Equipo Sevilla FC: Marcos Martínez, Juan de la Puerta, Eduardo Contreras y Rubén Fernández.

Tablero de Trello: https://trello.com/b/kv6CVTfn

## Tecnologías

- Java 21 y JavaFX 21 (FXML con Scene Builder y CSS)
- Maven (el conector de MySQL viene en el `pom.xml`)
- MySQL con XAMPP

## Cómo clonar y ejecutar

1. Clonar el repositorio:

   ```
   git clone https://github.com/marrcosmrtzz-ctrl/RestauranteSevillaFC.git
   ```

   También se puede hacer desde IntelliJ: File > New > Project from Version Control y pegar la URL.

2. Abrir la carpeta en IntelliJ (File > Open) y esperar a que Maven descargue las dependencias.

3. Preparar la base de datos (ver el apartado siguiente).

4. Ejecutar la clase `App`, o en la pestaña de Maven: Plugins > javafx > javafx:run.

## Base de datos (MySQL con XAMPP)

1. Abrir el panel de XAMPP y pulsar **Start** en **Apache** y en **MySQL**.
2. Entrar en phpMyAdmin: http://localhost/phpmyadmin
3. Ir a **Importar**, elegir `docs/base_datos/restaurante_sevillafc.sql` y pulsar **Importar**. Crea la base de datos `restaurante_sevillafc` con sus tablas y los platos de la carta.
4. Para tener pedidos y reservas de prueba en las pantallas del trabajador, importar después `docs/base_datos/datos_prueba_trabajador.sql`.

La app se conecta con el usuario `root` sin contraseña, que es el que trae XAMPP (está en `config/Conexion.java`). Si MySQL está apagado, la app avisa de que no puede conectar.

Si MySQL no arranca en XAMPP y en sus Logs sale "OS error 259", añadir estas dos líneas en `C:\xampp\mysql\bin\my.ini`, debajo de `[mysqld]`:

```
innodb_use_native_aio=0
innodb_flush_method=normal
```

## Usuarios de prueba

Están en la tabla `usuario` de la base de datos (se crean al importar el script):

| Rol | Correo | Contraseña |
|---|---|---|
| Trabajador | antonio.ruiz@sevillafc.es | 1234 |
| Cliente | lucia.garcia@correo.com | 1234 |

Con `datos_prueba_trabajador.sql` se añaden más clientes (por ejemplo `manuel.perez@correo.com`, contraseña `1234`).

También se puede crear un cliente nuevo desde la pantalla de registro. La contraseña tiene que tener mínimo 8 caracteres, una mayúscula y un número.

## Accesibilidad y ayuda

En todas las pantallas:

- **Ver**: tema oscuro (Ctrl+T) y tamaño de letra (Ctrl + para agrandar, Ctrl - para reducir, Ctrl 0 para volver a la normal). Se mantiene al cambiar de pantalla.
- **Ayuda → Cómo usar esta pantalla** (F1): explica en pocas líneas cómo se usa.
- Los campos y botones tienen texto accesible para los lectores de pantalla.

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
- Los datos de conexión a la base de datos están en `config/Conexion.java` (usuario `root` de XAMPP, sin contraseña).

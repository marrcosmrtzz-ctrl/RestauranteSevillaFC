# Diccionario de datos · restaurante_sevillafc

Gestor: MySQL. El script `restaurante_sevillafc.sql` lo usaremos cuando conectemos la aplicación con la base de datos.

## usuario
Clientes y trabajadores del restaurante.

| Campo | Tipo | Restricciones | Descripción |
|---|---|---|---|
| id_usuario | INT | PK, AUTO_INCREMENT | Identificador |
| nombre | VARCHAR(50) | NOT NULL | Nombre |
| apellidos | VARCHAR(100) | NOT NULL | Apellidos |
| email | VARCHAR(100) | NOT NULL, UNIQUE | Correo para iniciar sesión |
| telefono | VARCHAR(15) | | Teléfono (opcional) |
| password | VARCHAR(255) | NOT NULL | Contraseña |
| rol | ENUM | NOT NULL, CLIENTE o TRABAJADOR | Tipo de usuario |
| fecha_registro | DATETIME | NOT NULL, por defecto ahora | Fecha de alta |

## categoria

| Campo | Tipo | Restricciones | Descripción |
|---|---|---|---|
| id_categoria | INT | PK, AUTO_INCREMENT | Identificador |
| nombre | VARCHAR(30) | NOT NULL, UNIQUE | Entrantes, Principales, Postres, Bebidas |

## plato

| Campo | Tipo | Restricciones | Descripción |
|---|---|---|---|
| id_plato | INT | PK, AUTO_INCREMENT | Identificador |
| nombre | VARCHAR(80) | NOT NULL | Nombre del plato |
| descripcion | VARCHAR(255) | | Descripción corta |
| alergenos | VARCHAR(150) | | Alérgenos separados por comas |
| precio | DECIMAL(6,2) | NOT NULL, mayor que 0 | Precio en euros |
| imagen | VARCHAR(150) | | Nombre del archivo de la foto |
| disponible | BOOLEAN | NOT NULL, por defecto TRUE | Si se puede pedir o está agotado |
| id_categoria | INT | FK → categoria, NOT NULL | Categoría del plato |

## mesa

| Campo | Tipo | Restricciones | Descripción |
|---|---|---|---|
| id_mesa | INT | PK, AUTO_INCREMENT | Identificador |
| numero | INT | NOT NULL, UNIQUE | Número de la mesa |
| capacidad | INT | NOT NULL, mayor que 0 | Personas que caben |

## reserva

| Campo | Tipo | Restricciones | Descripción |
|---|---|---|---|
| id_reserva | INT | PK, AUTO_INCREMENT | Identificador |
| fecha | DATE | NOT NULL | Día de la reserva |
| hora | TIME | NOT NULL | Hora de la reserva |
| num_personas | INT | NOT NULL, entre 1 y 10 | Comensales |
| observaciones | VARCHAR(255) | | Terraza, trona, alergias... |
| estado | ENUM | NOT NULL, PENDIENTE / CONFIRMADA / CANCELADA | Estado |
| id_usuario | INT | FK → usuario, NOT NULL | Cliente que reserva |
| id_mesa | INT | FK → mesa, puede ser NULL | Se asigna al confirmar |

## pedido

| Campo | Tipo | Restricciones | Descripción |
|---|---|---|---|
| id_pedido | INT | PK, AUTO_INCREMENT | Identificador |
| fecha_hora | DATETIME | NOT NULL, por defecto ahora | Cuándo se hizo |
| observaciones | VARCHAR(250) | | Notas para cocina |
| estado | ENUM | NOT NULL, RECIBIDO / EN_PREPARACION / LISTO / ENTREGADO | Estado |
| total | DECIMAL(8,2) | NOT NULL | Importe total |
| id_usuario | INT | FK → usuario, NOT NULL | Cliente |

## linea_pedido
Resuelve la relación N:M entre pedido y plato.

| Campo | Tipo | Restricciones | Descripción |
|---|---|---|---|
| id_pedido | INT | PK, FK → pedido (borrado en cascada) | Pedido |
| id_plato | INT | PK, FK → plato | Plato pedido |
| cantidad | INT | NOT NULL, mayor que 0 | Unidades |
| precio_unidad | DECIMAL(6,2) | NOT NULL | Precio del plato en ese momento |

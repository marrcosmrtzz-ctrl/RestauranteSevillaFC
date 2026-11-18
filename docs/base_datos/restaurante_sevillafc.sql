-- Base de datos del Restaurante Sevilla FC
-- Proyecto Desarrollo de Interfaces - Sprint 2

DROP DATABASE IF EXISTS restaurante_sevillafc;
CREATE DATABASE restaurante_sevillafc;
USE restaurante_sevillafc;

-- Clientes y trabajadores (se diferencian por el rol)
CREATE TABLE usuario (
    id_usuario     INT AUTO_INCREMENT PRIMARY KEY,
    nombre         VARCHAR(50)  NOT NULL,
    apellidos      VARCHAR(100) NOT NULL,
    email          VARCHAR(100) NOT NULL UNIQUE,
    telefono       VARCHAR(15),
    password       VARCHAR(255) NOT NULL,
    rol            ENUM('CLIENTE', 'TRABAJADOR') NOT NULL DEFAULT 'CLIENTE',
    fecha_registro DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE categoria (
    id_categoria INT AUTO_INCREMENT PRIMARY KEY,
    nombre       VARCHAR(30) NOT NULL UNIQUE
);

CREATE TABLE plato (
    id_plato     INT AUTO_INCREMENT PRIMARY KEY,
    nombre       VARCHAR(80)  NOT NULL,
    descripcion  VARCHAR(255),
    alergenos    VARCHAR(150),
    precio       DECIMAL(6,2) NOT NULL CHECK (precio > 0),
    imagen       VARCHAR(150),
    disponible   BOOLEAN NOT NULL DEFAULT TRUE,
    id_categoria INT NOT NULL,
    FOREIGN KEY (id_categoria) REFERENCES categoria(id_categoria)
);

CREATE TABLE mesa (
    id_mesa   INT AUTO_INCREMENT PRIMARY KEY,
    numero    INT NOT NULL UNIQUE,
    capacidad INT NOT NULL CHECK (capacidad > 0)
);

-- La mesa se asigna cuando el trabajador confirma la reserva
CREATE TABLE reserva (
    id_reserva    INT AUTO_INCREMENT PRIMARY KEY,
    fecha         DATE NOT NULL,
    hora          TIME NOT NULL,
    num_personas  INT  NOT NULL CHECK (num_personas BETWEEN 1 AND 10),
    observaciones VARCHAR(255),
    estado        ENUM('PENDIENTE', 'CONFIRMADA', 'CANCELADA') NOT NULL DEFAULT 'PENDIENTE',
    id_usuario    INT NOT NULL,
    id_mesa       INT NULL,
    FOREIGN KEY (id_usuario) REFERENCES usuario(id_usuario),
    FOREIGN KEY (id_mesa) REFERENCES mesa(id_mesa)
);

CREATE TABLE pedido (
    id_pedido     INT AUTO_INCREMENT PRIMARY KEY,
    fecha_hora    DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    observaciones VARCHAR(250),
    estado        ENUM('RECIBIDO', 'EN_PREPARACION', 'LISTO', 'ENTREGADO') NOT NULL DEFAULT 'RECIBIDO',
    total         DECIMAL(8,2) NOT NULL DEFAULT 0,
    id_usuario    INT NOT NULL,
    FOREIGN KEY (id_usuario) REFERENCES usuario(id_usuario)
);

-- Resuelve la relacion N:M entre pedido y plato
CREATE TABLE linea_pedido (
    id_pedido     INT NOT NULL,
    id_plato      INT NOT NULL,
    cantidad      INT NOT NULL CHECK (cantidad > 0),
    precio_unidad DECIMAL(6,2) NOT NULL,
    PRIMARY KEY (id_pedido, id_plato),
    FOREIGN KEY (id_pedido) REFERENCES pedido(id_pedido) ON DELETE CASCADE,
    FOREIGN KEY (id_plato) REFERENCES plato(id_plato)
);

-- Datos de prueba
INSERT INTO categoria (nombre) VALUES ('Entrantes'), ('Principales'), ('Postres'), ('Bebidas');

INSERT INTO usuario (nombre, apellidos, email, telefono, password, rol) VALUES
('Antonio', 'Ruiz', 'antonio.ruiz@sevillafc.es', '600111222', '1234', 'TRABAJADOR'),
('Lucía', 'García Romero', 'lucia.garcia@correo.com', '600123456', '1234', 'CLIENTE');

INSERT INTO plato (nombre, descripcion, alergenos, precio, id_categoria) VALUES
('Salmorejo', 'Con jamón ibérico y huevo duro', 'Gluten, Huevo', 6.50, 1),
('Croquetas de puchero', 'Ración de 8, cremosas por dentro', 'Gluten, Lácteos', 8.00, 1),
('Gazpacho andaluz', 'Fresquito, con su guarnición', NULL, 5.50, 1),
('Rabo de toro', 'Guiso al vino tinto con patatas', 'Sulfitos', 15.50, 2),
('Paella mixta', 'Marisco y pollo, mín. 2 personas', 'Crustáceos', 13.90, 2),
('Pescaíto frito', 'Adobo, chocos y boquerones', 'Pescado, Gluten', 12.50, 2),
('Torrijas', 'De leche, con miel y canela', 'Gluten, Huevo', 5.00, 3),
('Tocino de cielo', 'Yema y almíbar, receta jerezana', 'Huevo', 4.80, 3),
('Tinto de verano', 'Con limón o casera', 'Sulfitos', 2.80, 4),
('Agua mineral', 'Botella de 50 cl', NULL, 1.80, 4);

INSERT INTO mesa (numero, capacidad) VALUES (1, 2), (2, 2), (3, 4), (4, 4), (5, 6), (6, 8);

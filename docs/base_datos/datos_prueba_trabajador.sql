-- Datos de prueba para las pantallas del trabajador (pedidos y reservas)
-- Se importa DESPUES de restaurante_sevillafc.sql
USE restaurante_sevillafc;

-- Mas clientes para que los pedidos y reservas no sean todos de Lucia
INSERT INTO usuario (id_usuario, nombre, apellidos, email, telefono, password, rol) VALUES
(3, 'Manuel', 'Pérez', 'manuel.perez@correo.com', '611456789', '1234', 'CLIENTE'),
(4, 'Carmen', 'Ruiz', 'carmen.ruiz@correo.com', '622987654', '1234', 'CLIENTE'),
(10, 'Rocío', 'Martín', 'rocio.martin@correo.com', '644555333', '1234', 'CLIENTE'),
(11, 'Javier', 'López', 'javier.lopez@correo.com', '655777888', '1234', 'CLIENTE'),
(12, 'Ana', 'Morales', 'ana.morales@correo.com', '666101202', '1234', 'CLIENTE'),
(13, 'José Antonio', 'Díaz', 'jose.diaz@correo.com', '633222111', '1234', 'CLIENTE'),
(14, 'Pablo', 'Romero', 'pablo.romero@correo.com', NULL, '1234', 'CLIENTE'),
(15, 'Elena', 'Sánchez', 'elena.sanchez@correo.com', NULL, '1234', 'CLIENTE');

-- Pedidos de hoy en distintos estados (el siguiente pedido que haga un cliente sera el 2041)
INSERT INTO pedido (id_pedido, fecha_hora, observaciones, estado, total, id_usuario) VALUES
(2036, TIMESTAMP(CURDATE(), '12:30:00'), '', 'ENTREGADO', 10.00, 3),
(2037, TIMESTAMP(CURDATE(), '12:50:00'), '', 'ENTREGADO', 31.40, 2),
(2038, TIMESTAMP(CURDATE(), '13:20:00'), 'Una de las raciones sin huevo', 'LISTO', 34.10, 4),
(2039, TIMESTAMP(CURDATE(), '13:35:00'), 'Sin picante en el adobo, por favor.', 'EN_PREPARACION', 28.50, 3),
(2040, TIMESTAMP(CURDATE(), '13:42:00'), '', 'RECIBIDO', 30.60, 2);

INSERT INTO linea_pedido (id_pedido, id_plato, cantidad, precio_unidad) VALUES
(2036, 7, 2, 5.00),
(2037, 5, 2, 13.90), (2037, 10, 2, 1.80),
(2038, 1, 2, 6.50), (2038, 4, 1, 15.50), (2038, 9, 2, 2.80),
(2039, 2, 2, 8.00), (2039, 6, 1, 12.50),
(2040, 3, 1, 5.50), (2040, 4, 1, 15.50), (2040, 8, 2, 4.80);

-- Reservas (las mismas de prueba que tenia Eduardo en ReservaDAO, mas dos de hoy)
INSERT INTO reserva (id_reserva, fecha, hora, num_personas, observaciones, estado, id_usuario) VALUES
(301, CURDATE() - INTERVAL 12 DAY, '14:00:00', 2, '', 'CONFIRMADA', 2),
(302, CURDATE() - INTERVAL 5 DAY, '21:30:00', 6, 'Cumpleaños', 'CANCELADA', 2),
(303, CURDATE() + INTERVAL 4 DAY, '21:00:00', 4, 'Terraza si puede ser', 'PENDIENTE', 2),
(304, CURDATE() + INTERVAL 2 DAY, '14:00:00', 2, '', 'PENDIENTE', 10),
(305, CURDATE() + INTERVAL 2 DAY, '14:00:00', 2, '', 'PENDIENTE', 11),
(306, CURDATE() + INTERVAL 2 DAY, '14:00:00', 2, '', 'PENDIENTE', 12),
(307, CURDATE() + INTERVAL 2 DAY, '14:00:00', 2, '', 'PENDIENTE', 13),
(308, CURDATE() + INTERVAL 2 DAY, '14:00:00', 2, '', 'PENDIENTE', 14),
(309, CURDATE() + INTERVAL 2 DAY, '14:00:00', 2, '', 'PENDIENTE', 15),
(310, CURDATE(), '21:00:00', 4, 'Trona para un bebé', 'PENDIENTE', 3),
(311, CURDATE(), '14:00:00', 2, '', 'CONFIRMADA', 4);

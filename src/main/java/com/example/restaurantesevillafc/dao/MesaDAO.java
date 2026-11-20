package com.example.restaurantesevillafc.dao;

import com.example.restaurantesevillafc.config.Conexion;
import com.example.restaurantesevillafc.model.Mesa;

import java.sql.*;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.Map;

// Mesas de la tabla mesa. Al confirmar una reserva se le pone una mesa libre (columna id_mesa de reserva)
public final class MesaDAO {

    // Numero de mesa de cada reserva que tiene mesa (id de la reserva -> numero de la mesa)
    private static final Map<Integer, Integer> MESA_DE_RESERVA = new HashMap<>();
    private MesaDAO() {}

    // Lee de la base de datos que mesa tiene cada reserva
    public static void recargar() {
        String sql = "SELECT r.id_reserva, m.numero FROM reserva r JOIN mesa m ON r.id_mesa = m.id_mesa";
        Map<Integer, Integer> leidas = new HashMap<>();
        try (Connection con = Conexion.getConexion();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) leidas.put(rs.getInt("id_reserva"), rs.getInt("numero"));
        } catch (SQLException e) {
            throw new RuntimeException(ErrorBD.mensaje(e));
        }
        MESA_DE_RESERVA.clear();
        MESA_DE_RESERVA.putAll(leidas);
    }

    // Numero de la mesa de una reserva, o null si todavia no tiene
    public static Integer numeroDeMesa(int idReserva) {
        return MESA_DE_RESERVA.get(idReserva);
    }

    // La mesa mas pequeña en la que caben esas personas y que no esta ya cogida ese dia a esa hora.
    // Devuelve null si no queda ninguna
    public static Mesa buscarLibre(LocalDate fecha, LocalTime hora, int personas, int idReserva) {
        String sql = "SELECT id_mesa, numero, capacidad FROM mesa WHERE capacidad >= ? AND id_mesa NOT IN ("
                + "SELECT id_mesa FROM reserva WHERE id_mesa IS NOT NULL AND estado = 'CONFIRMADA' "
                + "AND fecha = ? AND hora = ? AND id_reserva <> ?) ORDER BY capacidad, numero LIMIT 1";
        try (Connection con = Conexion.getConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, personas);
            ps.setDate(2, Date.valueOf(fecha));
            ps.setTime(3, Time.valueOf(hora));
            ps.setInt(4, idReserva);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? new Mesa(rs.getInt("id_mesa"), rs.getInt("numero"), rs.getInt("capacidad")) : null;
            }
        } catch (SQLException e) {
            throw new RuntimeException(ErrorBD.mensaje(e));
        }
    }

    // Pone la mesa a la reserva (o la quita si mesa es null)
    public static void asignar(int idReserva, Mesa mesa) {
        try (Connection con = Conexion.getConexion();
             PreparedStatement ps = con.prepareStatement("UPDATE reserva SET id_mesa = ? WHERE id_reserva = ?")) {
            if (mesa == null) ps.setNull(1, Types.INTEGER); else ps.setInt(1, mesa.getId());
            ps.setInt(2, idReserva);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(ErrorBD.mensaje(e));
        }
        if (mesa == null) MESA_DE_RESERVA.remove(idReserva); else MESA_DE_RESERVA.put(idReserva, mesa.getNumero());
    }
}

package com.example.restaurantesevillafc.model;

// Mesa del restaurante: su numero y cuantas personas caben
public class Mesa {
    private final int id;
    private final int numero;
    private final int capacidad;

    public Mesa(int id, int numero, int capacidad) {
        this.id = id;
        this.numero = numero;
        this.capacidad = capacidad;
    }
    public int getId() { return id; }
    public int getNumero() { return numero; }
    public int getCapacidad() { return capacidad; }
}

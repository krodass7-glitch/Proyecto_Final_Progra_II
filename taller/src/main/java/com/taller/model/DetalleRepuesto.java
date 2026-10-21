package com.taller.model;

import java.math.BigDecimal;

public class DetalleRepuesto implements Costeable {
    private final Repuesto repuesto;
    private final int cantidad;
    private final BigDecimal precioRegistrado;

    public DetalleRepuesto(Repuesto repuesto, int cantidad, BigDecimal precioRegistrado) {
        this.repuesto = repuesto;
        this.cantidad = cantidad;
        this.precioRegistrado = precioRegistrado;
    }

    public Repuesto getRepuesto() {
        return repuesto;
    }

    public int getCantidad() {
        return cantidad;
    }

    @Override
    public String tipo() {
        return "Repuesto";
    }

    @Override
    public String descripcion() {
        return repuesto.getNombre();
    }

    @Override
    public int cantidad() {
        return cantidad;
    }

    @Override
    public BigDecimal precioUnitario() {
        return precioRegistrado;
    }
}

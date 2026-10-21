package com.taller.model;

import java.math.BigDecimal;

public class DetalleServicio implements Costeable {
    private final Servicio servicio;
    private final BigDecimal costoRegistrado;

    public DetalleServicio(Servicio servicio, BigDecimal costoRegistrado) {
        this.servicio = servicio;
        this.costoRegistrado = costoRegistrado;
    }

    public Servicio getServicio() {
        return servicio;
    }

    @Override
    public String tipo() {
        return "Mano de obra";
    }

    @Override
    public String descripcion() {
        return servicio.getDescripcion();
    }

    @Override
    public int cantidad() {
        return 1;
    }

    @Override
    public BigDecimal precioUnitario() {
        return costoRegistrado;
    }
}

package com.taller.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class Comprobante {
    private int id;
    private int ordenId;
    private LocalDateTime fecha;
    private BigDecimal totalServicios;
    private BigDecimal totalRepuestos;
    private BigDecimal total;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getOrdenId() {
        return ordenId;
    }

    public void setOrdenId(int ordenId) {
        this.ordenId = ordenId;
    }

    public LocalDateTime getFecha() {
        return fecha;
    }

    public void setFecha(LocalDateTime fecha) {
        this.fecha = fecha;
    }

    public BigDecimal getTotalServicios() {
        return totalServicios;
    }

    public void setTotalServicios(BigDecimal totalServicios) {
        this.totalServicios = totalServicios;
    }

    public BigDecimal getTotalRepuestos() {
        return totalRepuestos;
    }

    public void setTotalRepuestos(BigDecimal totalRepuestos) {
        this.totalRepuestos = totalRepuestos;
    }

    public BigDecimal getTotal() {
        return total;
    }

    public void setTotal(BigDecimal total) {
        this.total = total;
    }
}

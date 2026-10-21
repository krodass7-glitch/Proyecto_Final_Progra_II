package com.taller.model;

import java.math.BigDecimal;

public class Repuesto {
    public static final int LIMITE_BAJO_INVENTARIO = 5;

    private String id;
    private String nombre;
    private String descripcion;
    private BigDecimal precio;
    private int existencia;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id == null ? null : id.trim();
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre == null ? null : nombre.trim();
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public BigDecimal getPrecio() {
        return precio;
    }

    public void setPrecio(BigDecimal precio) {
        this.precio = precio;
    }

    public int getExistencia() {
        return existencia;
    }

    public void setExistencia(int existencia) {
        this.existencia = existencia;
    }

    public boolean isAgotado() {
        return existencia == 0;
    }

    public boolean isBajoInventario() {
        return existencia <= LIMITE_BAJO_INVENTARIO;
    }
}

package com.taller.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class OrdenTrabajo {
    private int id;
    private Vehiculo vehiculo;
    private Mecanico mecanico;
    private String descripcionProblema;
    private EstadoOrden estado;
    private LocalDateTime fechaCreacion;
    private LocalDateTime fechaFinalizacion;
    private BigDecimal costoTotal;
    private List<DetalleServicio> servicios = new ArrayList<>();
    private List<DetalleRepuesto> repuestos = new ArrayList<>();

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public Vehiculo getVehiculo() {
        return vehiculo;
    }

    public void setVehiculo(Vehiculo vehiculo) {
        this.vehiculo = vehiculo;
    }

    public Mecanico getMecanico() {
        return mecanico;
    }

    public void setMecanico(Mecanico mecanico) {
        this.mecanico = mecanico;
    }

    public String getDescripcionProblema() {
        return descripcionProblema;
    }

    public void setDescripcionProblema(String descripcionProblema) {
        this.descripcionProblema = descripcionProblema;
    }

    public EstadoOrden getEstado() {
        return estado;
    }

    public void setEstado(EstadoOrden estado) {
        this.estado = estado;
    }

    public LocalDateTime getFechaCreacion() {
        return fechaCreacion;
    }

    public void setFechaCreacion(LocalDateTime fechaCreacion) {
        this.fechaCreacion = fechaCreacion;
    }

    public LocalDateTime getFechaFinalizacion() {
        return fechaFinalizacion;
    }

    public void setFechaFinalizacion(LocalDateTime fechaFinalizacion) {
        this.fechaFinalizacion = fechaFinalizacion;
    }

    public BigDecimal getCostoTotal() {
        return costoTotal;
    }

    public void setCostoTotal(BigDecimal costoTotal) {
        this.costoTotal = costoTotal;
    }

    public List<DetalleServicio> getServicios() {
        return servicios;
    }

    public void setServicios(List<DetalleServicio> servicios) {
        this.servicios = servicios;
    }

    public List<DetalleRepuesto> getRepuestos() {
        return repuestos;
    }

    public void setRepuestos(List<DetalleRepuesto> repuestos) {
        this.repuestos = repuestos;
    }

    public List<Costeable> detalles() {
        List<Costeable> todos = new ArrayList<>(servicios);
        todos.addAll(repuestos);
        return todos;
    }

    public BigDecimal totalServicios() {
        return sumar(servicios);
    }

    public BigDecimal totalRepuestos() {
        return sumar(repuestos);
    }

    public BigDecimal calcularTotal() {
        return sumar(detalles());
    }

    private static BigDecimal sumar(List<? extends Costeable> lista) {
        return lista.stream()
                .map(c -> c.costo())
                .reduce(BigDecimal.ZERO, (acc, val) -> acc.add(val));
    }
}

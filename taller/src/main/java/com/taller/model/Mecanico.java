package com.taller.model;

public class Mecanico extends Persona {
    private String especialidad;

    public String getEspecialidad() {
        return especialidad;
    }

    public void setEspecialidad(String especialidad) {
        this.especialidad = especialidad;
    }

    @Override
    public String getTipo() {
        return "Mecánico";
    }

    @Override
    public String resumen() {
        return super.resumen() + " - " + (especialidad == null ? "General" : especialidad);
    }
}

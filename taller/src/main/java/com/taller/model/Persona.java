package com.taller.model;

public abstract class Persona {
    private String id;
    private String nombre;
    private String telefono;

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

    public String getTelefono() {
        return telefono;
    }

    public void setTelefono(String telefono) {
        this.telefono = telefono;
    }

    public abstract String getTipo();

    public String resumen() {
        return getTipo() + ": " + nombre + " (" + id + ")";
    }
}

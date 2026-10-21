package com.taller.model;

import java.util.List;

public class Reporte {
    private final String titulo;
    private final List<String> columnas;
    private final List<List<String>> filas;
    private final String pie;

    public Reporte(String titulo, List<String> columnas, List<List<String>> filas, String pie) {
        this.titulo = titulo;
        this.columnas = columnas;
        this.filas = filas;
        this.pie = pie;
    }

    public String getTitulo() {
        return titulo;
    }

    public List<String> getColumnas() {
        return columnas;
    }

    public List<List<String>> getFilas() {
        return filas;
    }

    public String getPie() {
        return pie;
    }
}

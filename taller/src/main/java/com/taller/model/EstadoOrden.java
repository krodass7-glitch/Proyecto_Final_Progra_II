package com.taller.model;

import com.taller.exception.NegocioException;

public enum EstadoOrden {
    PENDIENTE("Pendiente"),
    EN_PROCESO("En proceso"),
    FINALIZADA("Finalizada"),
    CANCELADA("Cancelada");

    private final String etiqueta;

    EstadoOrden(String etiqueta) {
        this.etiqueta = etiqueta;
    }

    public String getEtiqueta() {
        return etiqueta;
    }

    public boolean abierta() {
        return this == PENDIENTE || this == EN_PROCESO;
    }

    public static EstadoOrden desde(String valor) {
        try {
            return valueOf(valor);
        } catch (IllegalArgumentException | NullPointerException e) {
            throw new NegocioException("Estado de orden no válido");
        }
    }
}

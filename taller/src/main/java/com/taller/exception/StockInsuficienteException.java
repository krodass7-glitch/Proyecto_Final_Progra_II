package com.taller.exception;

public class StockInsuficienteException extends NegocioException {
    private final int disponible;

    public StockInsuficienteException(String repuesto, int disponible, int solicitado) {
        super("Existencia insuficiente de " + repuesto + ": disponibles " + disponible + ", solicitados " + solicitado);
        this.disponible = disponible;
    }

    public int getDisponible() {
        return disponible;
    }
}

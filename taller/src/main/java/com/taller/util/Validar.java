package com.taller.util;

import com.taller.exception.NegocioException;
import java.math.BigDecimal;

public final class Validar {
    private Validar() {
    }

    public static void requerido(String valor, String campo) {
        if (valor == null || valor.isBlank()) {
            throw new NegocioException("El campo " + campo + " es obligatorio");
        }
    }

    public static void noNegativo(BigDecimal valor, String campo) {
        if (valor == null || valor.signum() < 0) {
            throw new NegocioException("El campo " + campo + " no puede ser negativo");
        }
    }

    public static void noNegativo(int valor, String campo) {
        if (valor < 0) {
            throw new NegocioException("El campo " + campo + " no puede ser negativo");
        }
    }

    public static void positivo(int valor, String campo) {
        if (valor <= 0) {
            throw new NegocioException("El campo " + campo + " debe ser mayor que cero");
        }
    }
}

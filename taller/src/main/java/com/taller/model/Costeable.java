package com.taller.model;

import java.math.BigDecimal;

public interface Costeable {
    String tipo();

    String descripcion();

    int cantidad();

    BigDecimal precioUnitario();

    default BigDecimal costo() {
        return precioUnitario().multiply(BigDecimal.valueOf(cantidad()));
    }
}

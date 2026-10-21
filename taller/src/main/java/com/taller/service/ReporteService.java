package com.taller.service;

import com.taller.dao.OrdenDao;
import com.taller.dao.RepuestoDao;
import com.taller.exception.NegocioException;
import com.taller.model.Comprobante;
import com.taller.model.Costeable;
import com.taller.model.EstadoOrden;
import com.taller.model.OrdenTrabajo;
import com.taller.model.Repuesto;
import com.taller.model.Reporte;
import com.taller.model.Vehiculo;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class ReporteService {
    private static final DateTimeFormatter FORMATO = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    private static final List<String> COLUMNAS_ORDEN =
            List.of("Orden", "Placa", "Vehículo", "Mecánico", "Estado", "Creada", "Total");

    private final OrdenDao ordenes;
    private final RepuestoDao repuestos;
    private final VehiculoService vehiculos;

    public ReporteService(OrdenDao ordenes, RepuestoDao repuestos, VehiculoService vehiculos) {
        this.ordenes = ordenes;
        this.repuestos = repuestos;
        this.vehiculos = vehiculos;
    }

    public Reporte construir(String tipo, Map<String, String> p) {
        return switch (tipo) {
            case "estados" -> ordenesPorEstado(p.get("estado"));
            case "vehiculo" -> historialVehiculo(p.get("placa"));
            case "repuestos" -> repuestosBajos();
            case "mecanicos" -> ordenesPorMecanico(p.get("mecanicoId"));
            case "ingresos" -> ingresos(p.get("desde"), p.get("hasta"));
            case "comprobante" -> comprobante(entero(p.get("id")));
            default -> throw new NegocioException("El reporte solicitado no existe");
        };
    }

    private Reporte ordenesPorEstado(String estado) {
        List<OrdenTrabajo> lista = vacio(estado) ? ordenes.findAll() : ordenes.findByEstado(EstadoOrden.desde(estado));
        Map<EstadoOrden, Long> conteo = lista.stream()
                .collect(Collectors.groupingBy(o -> o.getEstado(), TreeMap::new, Collectors.counting()));
        String detalle = conteo.entrySet().stream()
                .map(e -> e.getKey().getEtiqueta() + ": " + e.getValue())
                .collect(Collectors.joining("  |  "));
        String pie = "Total de órdenes: " + lista.size() + (detalle.isEmpty() ? "" : "  (" + detalle + ")");
        return new Reporte("Órdenes por estado", COLUMNAS_ORDEN, lista.stream().map(o -> filaOrden(o)).toList(), pie);
    }

    private Reporte historialVehiculo(String placa) {
        if (vacio(placa)) {
            throw new NegocioException("Debe indicar la placa del vehículo");
        }
        Vehiculo vehiculo = vehiculos.obtener(placa.trim().toUpperCase());
        List<OrdenTrabajo> lista = vehiculos.historial(vehiculo.getPlaca());
        BigDecimal invertido = lista.stream()
                .filter(o -> o.getEstado() == EstadoOrden.FINALIZADA)
                .map(o -> o.getCostoTotal())
                .reduce(BigDecimal.ZERO, (acc, val) -> acc.add(val));
        String titulo = "Historial de reparaciones - " + vehiculo.getPlaca() + " (" + vehiculo.descripcion() + ")";
        String pie = "Propietario: " + vehiculo.getCliente().getNombre() + "  |  Órdenes: " + lista.size()
                + "  |  Total en reparaciones finalizadas: " + dinero(invertido);
        return new Reporte(titulo, COLUMNAS_ORDEN, lista.stream().map(o -> filaOrden(o)).toList(), pie);
    }

    private Reporte repuestosBajos() {
        List<Repuesto> lista = repuestos.findByExistenciaMaxima(Repuesto.LIMITE_BAJO_INVENTARIO);
        List<List<String>> filas = lista.stream()
                .map(r -> List.of(r.getId(), r.getNombre(), String.valueOf(r.getDescripcion() == null ? "" : r.getDescripcion()),
                        dinero(r.getPrecio()), String.valueOf(r.getExistencia()), r.isAgotado() ? "AGOTADO" : "BAJO INVENTARIO"))
                .toList();
        return new Reporte("Repuestos con existencia de " + Repuesto.LIMITE_BAJO_INVENTARIO + " unidades o menos",
                List.of("Código", "Nombre", "Descripción", "Precio", "Existencia", "Situación"),
                filas, "Repuestos a reabastecer: " + lista.size());
    }

    private Reporte ordenesPorMecanico(String mecanicoId) {
        List<OrdenTrabajo> lista = ordenes.findAll().stream()
                .filter(o -> vacio(mecanicoId) || o.getMecanico().getId().equals(mecanicoId))
                .sorted(Comparator.comparing((OrdenTrabajo o) -> o.getMecanico().getNombre()).thenComparing(o -> o.getId()))
                .toList();
        List<List<String>> filas = lista.stream()
                .map(o -> List.of(o.getMecanico().getNombre(), String.valueOf(o.getId()), o.getVehiculo().getPlaca(),
                        o.getVehiculo().descripcion(), o.getEstado().getEtiqueta(), o.getFechaCreacion().format(FORMATO)))
                .toList();
        Map<String, Long> porEstado = lista.stream()
                .collect(Collectors.groupingBy(o -> o.getEstado().getEtiqueta(), TreeMap::new, Collectors.counting()));
        String pie = "Total de órdenes: " + lista.size() + "  (" + porEstado.entrySet().stream()
                .map(e -> e.getKey() + ": " + e.getValue()).collect(Collectors.joining("  |  ")) + ")";
        return new Reporte("Órdenes por mecánico y estado",
                List.of("Mecánico", "Orden", "Placa", "Vehículo", "Estado", "Creada"), filas, pie);
    }

    private Reporte ingresos(String desdeTexto, String hastaTexto) {
        LocalDate desde = fecha(desdeTexto, "inicial");
        LocalDate hasta = fecha(hastaTexto, "final");
        if (desde.isAfter(hasta)) {
            throw new NegocioException("La fecha inicial no puede ser posterior a la final");
        }
        List<OrdenTrabajo> lista = ordenes.findFinalizadasEntre(desde.atStartOfDay(), hasta.plusDays(1).atStartOfDay());
        BigDecimal total = lista.stream().map(o -> o.getCostoTotal()).reduce(BigDecimal.ZERO, (acc, val) -> acc.add(val));
        List<List<String>> filas = lista.stream()
                .map(o -> List.of(String.valueOf(o.getId()), o.getVehiculo().getPlaca(), o.getVehiculo().descripcion(),
                        o.getMecanico().getNombre(), o.getFechaFinalizacion().format(FORMATO), dinero(o.getCostoTotal())))
                .toList();
        return new Reporte("Ingresos por reparaciones del " + desde.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))
                + " al " + hasta.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")),
                List.of("Orden", "Placa", "Vehículo", "Mecánico", "Finalizada", "Total"), filas,
                "Órdenes finalizadas: " + lista.size() + "  |  Ingresos totales: " + dinero(total));
    }

    private Reporte comprobante(int ordenId) {
        OrdenTrabajo orden = ordenes.findById(ordenId).orElseThrow(() -> new NegocioException("La orden no existe"));
        Comprobante comprobante = ordenes.findComprobante(ordenId)
                .orElseThrow(() -> new NegocioException("La orden aún no tiene comprobante"));
        List<List<String>> filas = new ArrayList<>();
        for (Costeable detalle : orden.detalles()) {
            filas.add(List.of(detalle.tipo(), detalle.descripcion(), String.valueOf(detalle.cantidad()),
                    dinero(detalle.precioUnitario()), dinero(detalle.costo())));
        }
        String pie = "Cliente: " + orden.getVehiculo().getCliente().getNombre() + "  |  Vehículo: "
                + orden.getVehiculo().getPlaca() + "  |  Mecánico: " + orden.getMecanico().getNombre()
                + "  |  Mano de obra: " + dinero(comprobante.getTotalServicios())
                + "  |  Repuestos: " + dinero(comprobante.getTotalRepuestos())
                + "  |  TOTAL: " + dinero(comprobante.getTotal())
                + "  |  Emitido: " + comprobante.getFecha().format(FORMATO);
        return new Reporte("Comprobante de la orden #" + ordenId,
                List.of("Concepto", "Descripción", "Cantidad", "Precio unitario", "Subtotal"), filas, pie);
    }

    private List<String> filaOrden(OrdenTrabajo o) {
        return List.of(String.valueOf(o.getId()), o.getVehiculo().getPlaca(), o.getVehiculo().descripcion(),
                o.getMecanico().getNombre(), o.getEstado().getEtiqueta(), o.getFechaCreacion().format(FORMATO),
                dinero(o.getCostoTotal()));
    }

    private String dinero(BigDecimal valor) {
        return valor == null ? "-" : "Q " + valor.setScale(2, RoundingMode.HALF_UP).toPlainString();
    }

    private boolean vacio(String valor) {
        return valor == null || valor.isBlank();
    }

    private int entero(String valor) {
        try {
            return Integer.parseInt(valor);
        } catch (NumberFormatException e) {
            throw new NegocioException("El número de orden no es válido");
        }
    }

    private LocalDate fecha(String valor, String nombre) {
        try {
            return LocalDate.parse(valor);
        } catch (DateTimeParseException | NullPointerException e) {
            throw new NegocioException("La fecha " + nombre + " no es válida");
        }
    }
}
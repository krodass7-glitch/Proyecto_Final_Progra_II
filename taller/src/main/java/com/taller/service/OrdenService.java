package com.taller.service;

import com.taller.dao.MecanicoDao;
import com.taller.dao.OrdenDao;
import com.taller.dao.RepuestoDao;
import com.taller.dao.ServicioDao;
import com.taller.dao.VehiculoDao;
import com.taller.exception.NegocioException;
import com.taller.exception.StockInsuficienteException;
import com.taller.model.Comprobante;
import com.taller.model.DetalleRepuesto;
import com.taller.model.EstadoOrden;
import com.taller.model.OrdenTrabajo;
import com.taller.model.Repuesto;
import com.taller.model.Servicio;
import com.taller.util.Validar;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class OrdenService {
    private final OrdenDao ordenes;
    private final VehiculoDao vehiculos;
    private final MecanicoDao mecanicos;
    private final ServicioDao servicios;
    private final RepuestoDao repuestos;

    public OrdenService(OrdenDao ordenes, VehiculoDao vehiculos, MecanicoDao mecanicos,
                        ServicioDao servicios, RepuestoDao repuestos) {
        this.ordenes = ordenes;
        this.vehiculos = vehiculos;
        this.mecanicos = mecanicos;
        this.servicios = servicios;
        this.repuestos = repuestos;
    }

    @Transactional(readOnly = true)
    public List<OrdenTrabajo> listar(EstadoOrden estado) {
        return estado == null ? ordenes.findAll() : ordenes.findByEstado(estado);
    }

    @Transactional(readOnly = true)
    public OrdenTrabajo obtener(int id) {
        return ordenes.findById(id).orElseThrow(() -> new NegocioException("La orden " + id + " no existe"));
    }

    @Transactional(readOnly = true)
    public Optional<Comprobante> comprobante(int ordenId) {
        return ordenes.findComprobante(ordenId);
    }

    public int crear(String placa, String mecanicoId, String descripcion) {
        Validar.requerido(placa, "vehículo");
        Validar.requerido(mecanicoId, "mecánico");
        Validar.requerido(descripcion, "descripción del problema");
        vehiculos.findByPlaca(placa).orElseThrow(() -> new NegocioException("El vehículo no existe"));
        mecanicos.findById(mecanicoId).orElseThrow(() -> new NegocioException("El mecánico no existe"));
        return ordenes.insert(placa, mecanicoId, descripcion.trim());
    }

    public void iniciar(int id) {
        OrdenTrabajo orden = obtener(id);
        if (orden.getEstado() != EstadoOrden.PENDIENTE) {
            throw new NegocioException("Solo una orden pendiente puede iniciarse");
        }
        ordenes.cambiarEstado(id, EstadoOrden.EN_PROCESO);
    }

    public void agregarServicio(int id, String servicioId) {
        exigirEnProceso(obtener(id));
        Servicio servicio = servicios.findById(servicioId)
                .orElseThrow(() -> new NegocioException("El servicio seleccionado no existe"));
        if (ordenes.contarServicio(id, servicioId) > 0) {
            throw new NegocioException("El servicio ya está registrado en la orden");
        }
        ordenes.insertServicio(id, servicioId, servicio.getCostoManoObra());
    }

    public void quitarServicio(int id, String servicioId) {
        exigirEnProceso(obtener(id));
        ordenes.deleteServicio(id, servicioId);
    }

    public void asignarRepuesto(int id, String repuestoId, int cantidad) {
        exigirEnProceso(obtener(id));
        Validar.positivo(cantidad, "cantidad");
        Repuesto repuesto = repuestos.findById(repuestoId)
                .orElseThrow(() -> new NegocioException("El repuesto seleccionado no existe"));
        if (repuesto.getExistencia() < cantidad) {
            throw new StockInsuficienteException(repuesto.getNombre(), repuesto.getExistencia(), cantidad);
        }
        if (repuestos.descontar(repuestoId, cantidad) == 0) {
            throw new StockInsuficienteException(repuesto.getNombre(), 0, cantidad);
        }
        if (ordenes.cantidadAsignada(id, repuestoId).isPresent()) {
            ordenes.sumarCantidad(id, repuestoId, cantidad);
        } else {
            ordenes.insertRepuesto(id, repuestoId, cantidad, repuesto.getPrecio());
        }
    }

    public void retirarRepuesto(int id, String repuestoId) {
        exigirEnProceso(obtener(id));
        int cantidad = ordenes.cantidadAsignada(id, repuestoId)
                .orElseThrow(() -> new NegocioException("El repuesto no está asignado a la orden"));
        repuestos.reintegrar(repuestoId, cantidad);
        ordenes.deleteRepuesto(id, repuestoId);
    }

    public Comprobante finalizar(int id) {
        OrdenTrabajo orden = obtener(id);
        exigirEnProceso(orden);
        if (orden.getServicios().isEmpty()) {
            throw new NegocioException("No se puede finalizar una orden sin servicios registrados");
        }
        BigDecimal total = orden.calcularTotal();
        LocalDateTime ahora = LocalDateTime.now();
        ordenes.finalizar(id, total, ahora);
        Comprobante comprobante = new Comprobante();
        comprobante.setOrdenId(id);
        comprobante.setFecha(ahora);
        comprobante.setTotalServicios(orden.totalServicios());
        comprobante.setTotalRepuestos(orden.totalRepuestos());
        comprobante.setTotal(total);
        ordenes.insertComprobante(comprobante);
        return comprobante;
    }

    public void cancelar(int id) {
        OrdenTrabajo orden = obtener(id);
        if (!orden.getEstado().abierta()) {
            throw new NegocioException("Solo se puede cancelar una orden pendiente o en proceso");
        }
        for (DetalleRepuesto detalle : orden.getRepuestos()) {
            repuestos.reintegrar(detalle.getRepuesto().getId(), detalle.getCantidad());
        }
        ordenes.deleteRepuestos(id);
        ordenes.cambiarEstado(id, EstadoOrden.CANCELADA);
    }

    private void exigirEnProceso(OrdenTrabajo orden) {
        if (!orden.getEstado().abierta()) {
            throw new NegocioException("La orden está " + orden.getEstado().getEtiqueta().toLowerCase()
                    + " y no admite modificaciones");
        }
        if (orden.getEstado() != EstadoOrden.EN_PROCESO) {
            throw new NegocioException("La orden debe iniciarse (estado EN PROCESO) antes de registrar servicios o repuestos");
        }
    }
}

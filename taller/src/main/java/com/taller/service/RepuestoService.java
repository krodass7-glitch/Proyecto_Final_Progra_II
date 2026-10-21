package com.taller.service;

import com.taller.dao.RepuestoDao;
import com.taller.exception.NegocioException;
import com.taller.model.Repuesto;
import com.taller.util.Validar;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class RepuestoService {
    private final RepuestoDao repuestos;

    public RepuestoService(RepuestoDao repuestos) {
        this.repuestos = repuestos;
    }

    public List<Repuesto> listar() {
        return repuestos.findAll();
    }

    public List<Repuesto> bajoInventario() {
        return repuestos.findByExistenciaMaxima(Repuesto.LIMITE_BAJO_INVENTARIO);
    }

    public Repuesto obtener(String id) {
        return repuestos.findById(id).orElseThrow(() -> new NegocioException("El repuesto " + id + " no existe"));
    }

    public void registrar(Repuesto repuesto) {
        validar(repuesto);
        if (repuestos.findById(repuesto.getId()).isPresent()) {
            throw new NegocioException("Ya existe un repuesto con el identificador " + repuesto.getId());
        }
        repuestos.insert(repuesto);
    }

    public void modificar(Repuesto repuesto) {
        validar(repuesto);
        obtener(repuesto.getId());
        repuestos.update(repuesto);
    }

    @Transactional
    public void abastecer(String id, int cantidad) {
        Validar.positivo(cantidad, "cantidad");
        obtener(id);
        repuestos.abastecer(id, cantidad);
    }

    private void validar(Repuesto repuesto) {
        Validar.requerido(repuesto.getId(), "identificador");
        Validar.requerido(repuesto.getNombre(), "nombre");
        Validar.noNegativo(repuesto.getPrecio(), "precio");
        Validar.noNegativo(repuesto.getExistencia(), "existencia");
    }
}

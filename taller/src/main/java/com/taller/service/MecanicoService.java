package com.taller.service;

import com.taller.dao.MecanicoDao;
import com.taller.dao.OrdenDao;
import com.taller.exception.NegocioException;
import com.taller.model.Mecanico;
import com.taller.util.Validar;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class MecanicoService {
    private final MecanicoDao mecanicos;
    private final OrdenDao ordenes;

    public MecanicoService(MecanicoDao mecanicos, OrdenDao ordenes) {
        this.mecanicos = mecanicos;
        this.ordenes = ordenes;
    }

    public List<Mecanico> listar() {
        return mecanicos.findAll();
    }

    public Mecanico obtener(String id) {
        return mecanicos.findById(id).orElseThrow(() -> new NegocioException("El mecánico " + id + " no existe"));
    }

    public void registrar(Mecanico mecanico) {
        validar(mecanico);
        if (mecanicos.findById(mecanico.getId()).isPresent()) {
            throw new NegocioException("Ya existe un mecánico con el identificador " + mecanico.getId());
        }
        mecanicos.insert(mecanico);
    }

    public void modificar(Mecanico mecanico) {
        validar(mecanico);
        obtener(mecanico.getId());
        mecanicos.update(mecanico);
    }

    public void eliminar(String id) {
        obtener(id);
        if (ordenes.countByMecanico(id) > 0) {
            throw new NegocioException("No se puede eliminar un mecánico con órdenes asociadas");
        }
        mecanicos.delete(id);
    }

    private void validar(Mecanico mecanico) {
        Validar.requerido(mecanico.getId(), "identificador");
        Validar.requerido(mecanico.getNombre(), "nombre");
    }
}

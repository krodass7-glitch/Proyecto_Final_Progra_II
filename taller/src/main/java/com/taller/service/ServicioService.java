package com.taller.service;

import com.taller.dao.ServicioDao;
import com.taller.exception.NegocioException;
import com.taller.model.Servicio;
import com.taller.util.Validar;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class ServicioService {
    private final ServicioDao servicios;

    public ServicioService(ServicioDao servicios) {
        this.servicios = servicios;
    }

    public List<Servicio> listar() {
        return servicios.findAll();
    }

    public Servicio obtener(String id) {
        return servicios.findById(id).orElseThrow(() -> new NegocioException("El servicio " + id + " no existe"));
    }

    public void registrar(Servicio servicio) {
        validar(servicio);
        if (servicios.findById(servicio.getId()).isPresent()) {
            throw new NegocioException("Ya existe un servicio con el identificador " + servicio.getId());
        }
        servicios.insert(servicio);
    }

    public void modificar(Servicio servicio) {
        validar(servicio);
        obtener(servicio.getId());
        servicios.update(servicio);
    }

    public void eliminar(String id) {
        obtener(id);
        servicios.delete(id);
    }

    private void validar(Servicio servicio) {
        Validar.requerido(servicio.getId(), "identificador");
        Validar.requerido(servicio.getDescripcion(), "descripción");
        Validar.noNegativo(servicio.getCostoManoObra(), "costo de mano de obra");
    }
}

package com.taller.service;

import com.taller.dao.ClienteDao;
import com.taller.dao.VehiculoDao;
import com.taller.exception.NegocioException;
import com.taller.model.Cliente;
import com.taller.util.Validar;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class ClienteService {
    private final ClienteDao clientes;
    private final VehiculoDao vehiculos;

    public ClienteService(ClienteDao clientes, VehiculoDao vehiculos) {
        this.clientes = clientes;
        this.vehiculos = vehiculos;
    }

    public List<Cliente> listar() {
        return clientes.findAll();
    }

    public Cliente obtener(String id) {
        return clientes.findById(id).orElseThrow(() -> new NegocioException("El cliente " + id + " no existe"));
    }

    public void registrar(Cliente cliente) {
        validar(cliente);
        if (clientes.findById(cliente.getId()).isPresent()) {
            throw new NegocioException("Ya existe un cliente con el identificador " + cliente.getId());
        }
        clientes.insert(cliente);
    }

    public void modificar(Cliente cliente) {
        validar(cliente);
        obtener(cliente.getId());
        clientes.update(cliente);
    }

    public void eliminar(String id) {
        obtener(id);
        if (vehiculos.countByCliente(id) > 0) {
            throw new NegocioException("No se puede eliminar un cliente con vehículos asociados");
        }
        clientes.delete(id);
    }

    private void validar(Cliente cliente) {
        Validar.requerido(cliente.getId(), "identificador");
        Validar.requerido(cliente.getNombre(), "nombre");
    }
}

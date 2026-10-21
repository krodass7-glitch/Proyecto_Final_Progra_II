package com.taller.service;

import com.taller.dao.OrdenDao;
import com.taller.dao.VehiculoDao;
import com.taller.exception.NegocioException;
import com.taller.model.OrdenTrabajo;
import com.taller.model.Vehiculo;
import com.taller.util.Validar;
import java.time.Year;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class VehiculoService {
    private final VehiculoDao vehiculos;
    private final OrdenDao ordenes;
    private final ClienteService clientes;

    public VehiculoService(VehiculoDao vehiculos, OrdenDao ordenes, ClienteService clientes) {
        this.vehiculos = vehiculos;
        this.ordenes = ordenes;
        this.clientes = clientes;
    }

    public List<Vehiculo> listar() {
        return vehiculos.findAll();
    }

    public Vehiculo obtener(String placa) {
        return vehiculos.findByPlaca(placa).orElseThrow(() -> new NegocioException("El vehículo " + placa + " no existe"));
    }

    public void registrar(String placa, String marca, String modelo, int anio, String clienteId) {
        Validar.requerido(placa, "placa");
        Validar.requerido(marca, "marca");
        Validar.requerido(modelo, "modelo");
        Validar.requerido(clienteId, "cliente");
        if (anio < 1900 || anio > Year.now().getValue() + 1) {
            throw new NegocioException("El año del vehículo no es válido");
        }
        Vehiculo vehiculo = new Vehiculo();
        vehiculo.setPlaca(placa);
        if (vehiculos.findByPlaca(vehiculo.getPlaca()).isPresent()) {
            throw new NegocioException("Ya existe un vehículo con la placa " + vehiculo.getPlaca());
        }
        vehiculo.setMarca(marca.trim());
        vehiculo.setModelo(modelo.trim());
        vehiculo.setAnio(anio);
        vehiculo.setCliente(clientes.obtener(clienteId));
        vehiculos.insert(vehiculo);
    }

    public void eliminar(String placa) {
        obtener(placa);
        if (ordenes.countByPlaca(placa) > 0) {
            throw new NegocioException("No se puede eliminar un vehículo con órdenes registradas");
        }
        vehiculos.delete(placa);
    }

    public List<OrdenTrabajo> historial(String placa) {
        obtener(placa);
        return ordenes.findByPlaca(placa);
    }
}

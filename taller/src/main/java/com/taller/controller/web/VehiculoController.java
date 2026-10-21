package com.taller.controller.web;

import com.taller.service.ClienteService;
import com.taller.service.VehiculoService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/vehiculos")
public class VehiculoController {
    private final VehiculoService servicio;
    private final ClienteService clientes;

    public VehiculoController(VehiculoService servicio, ClienteService clientes) {
        this.servicio = servicio;
        this.clientes = clientes;
    }

    @GetMapping
    public String listar(Model modelo) {
        modelo.addAttribute("vehiculos", servicio.listar());
        modelo.addAttribute("clientes", clientes.listar());
        return "vehiculos";
    }

    @PostMapping("/guardar")
    public String guardar(@RequestParam String placa, @RequestParam String marca, @RequestParam String modelo,
                          @RequestParam int anio, @RequestParam String clienteId, RedirectAttributes atributos) {
        servicio.registrar(placa, marca, modelo, anio, clienteId);
        atributos.addFlashAttribute("ok", "Vehículo registrado correctamente");
        return "redirect:/vehiculos";
    }

    @PostMapping("/eliminar")
    public String eliminar(@RequestParam String placa, RedirectAttributes atributos) {
        servicio.eliminar(placa);
        atributos.addFlashAttribute("ok", "Vehículo eliminado");
        return "redirect:/vehiculos";
    }
}

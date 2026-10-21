package com.taller.controller.web;

import com.taller.model.Cliente;
import com.taller.service.ClienteService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/clientes")
public class ClienteController {
    private final ClienteService servicio;

    public ClienteController(ClienteService servicio) {
        this.servicio = servicio;
    }

    @GetMapping
    public String listar(@RequestParam(required = false) String editar, Model modelo) {
        modelo.addAttribute("clientes", servicio.listar());
        modelo.addAttribute("cliente", editar == null ? new Cliente() : servicio.obtener(editar));
        modelo.addAttribute("edicion", editar != null);
        return "clientes";
    }

    @PostMapping("/guardar")
    public String guardar(@ModelAttribute Cliente cliente, @RequestParam boolean edicion, RedirectAttributes atributos) {
        if (edicion) {
            servicio.modificar(cliente);
        } else {
            servicio.registrar(cliente);
        }
        atributos.addFlashAttribute("ok", "Cliente guardado correctamente");
        return "redirect:/clientes";
    }

    @PostMapping("/eliminar")
    public String eliminar(@RequestParam String id, RedirectAttributes atributos) {
        servicio.eliminar(id);
        atributos.addFlashAttribute("ok", "Cliente eliminado");
        return "redirect:/clientes";
    }
}

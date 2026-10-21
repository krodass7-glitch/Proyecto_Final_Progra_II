package com.taller.controller.web;

import com.taller.model.Servicio;
import com.taller.service.ServicioService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/servicios")
public class ServicioController {
    private final ServicioService servicio;

    public ServicioController(ServicioService servicio) {
        this.servicio = servicio;
    }

    @GetMapping
    public String listar(@RequestParam(required = false) String editar, Model modelo) {
        modelo.addAttribute("servicios", servicio.listar());
        modelo.addAttribute("servicio", editar == null ? new Servicio() : servicio.obtener(editar));
        modelo.addAttribute("edicion", editar != null);
        return "servicios";
    }

    @PostMapping("/guardar")
    public String guardar(@ModelAttribute Servicio servicio, @RequestParam boolean edicion, RedirectAttributes atributos) {
        if (edicion) {
            this.servicio.modificar(servicio);
        } else {
            this.servicio.registrar(servicio);
        }
        atributos.addFlashAttribute("ok", "Servicio guardado correctamente");
        return "redirect:/servicios";
    }

    @PostMapping("/eliminar")
    public String eliminar(@RequestParam String id, RedirectAttributes atributos) {
        servicio.eliminar(id);
        atributos.addFlashAttribute("ok", "Servicio eliminado");
        return "redirect:/servicios";
    }
}

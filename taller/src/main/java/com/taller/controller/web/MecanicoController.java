package com.taller.controller.web;

import com.taller.model.Mecanico;
import com.taller.service.MecanicoService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/mecanicos")
public class MecanicoController {
    private final MecanicoService servicio;

    public MecanicoController(MecanicoService servicio) {
        this.servicio = servicio;
    }

    @GetMapping
    public String listar(@RequestParam(required = false) String editar, Model modelo) {
        modelo.addAttribute("mecanicos", servicio.listar());
        modelo.addAttribute("mecanico", editar == null ? new Mecanico() : servicio.obtener(editar));
        modelo.addAttribute("edicion", editar != null);
        return "mecanicos";
    }

    @PostMapping("/guardar")
    public String guardar(@ModelAttribute Mecanico mecanico, @RequestParam boolean edicion, RedirectAttributes atributos) {
        if (edicion) {
            servicio.modificar(mecanico);
        } else {
            servicio.registrar(mecanico);
        }
        atributos.addFlashAttribute("ok", "Mecánico guardado correctamente");
        return "redirect:/mecanicos";
    }

    @PostMapping("/eliminar")
    public String eliminar(@RequestParam String id, RedirectAttributes atributos) {
        servicio.eliminar(id);
        atributos.addFlashAttribute("ok", "Mecánico eliminado");
        return "redirect:/mecanicos";
    }
}

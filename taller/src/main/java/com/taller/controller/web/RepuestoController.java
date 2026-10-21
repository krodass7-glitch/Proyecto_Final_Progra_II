package com.taller.controller.web;

import com.taller.model.Repuesto;
import com.taller.service.RepuestoService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/repuestos")
public class RepuestoController {
    private final RepuestoService servicio;

    public RepuestoController(RepuestoService servicio) {
        this.servicio = servicio;
    }

    @GetMapping
    public String listar(@RequestParam(required = false) String editar,
                         @RequestParam(required = false) String filtro, Model modelo) {
        boolean soloBajos = "bajo".equals(filtro);
        modelo.addAttribute("repuestos", soloBajos ? servicio.bajoInventario() : servicio.listar());
        modelo.addAttribute("soloBajos", soloBajos);
        modelo.addAttribute("repuesto", editar == null ? new Repuesto() : servicio.obtener(editar));
        modelo.addAttribute("edicion", editar != null);
        return "repuestos";
    }

    @PostMapping("/guardar")
    public String guardar(@ModelAttribute Repuesto repuesto, @RequestParam boolean edicion, RedirectAttributes atributos) {
        if (edicion) {
            servicio.modificar(repuesto);
        } else {
            servicio.registrar(repuesto);
        }
        atributos.addFlashAttribute("ok", "Repuesto guardado correctamente");
        return "redirect:/repuestos";
    }

    @PostMapping("/abastecer")
    public String abastecer(@RequestParam String id, @RequestParam int cantidad, RedirectAttributes atributos) {
        servicio.abastecer(id, cantidad);
        atributos.addFlashAttribute("ok", "Se agregaron " + cantidad + " unidades al repuesto " + id);
        return "redirect:/repuestos";
    }
}

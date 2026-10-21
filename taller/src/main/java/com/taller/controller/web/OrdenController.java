package com.taller.controller.web;

import com.taller.model.EstadoOrden;
import com.taller.model.OrdenTrabajo;
import com.taller.service.MecanicoService;
import com.taller.service.OrdenService;
import com.taller.service.RepuestoService;
import com.taller.service.ServicioService;
import com.taller.service.VehiculoService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/ordenes")
public class OrdenController {
    private final OrdenService ordenes;
    private final VehiculoService vehiculos;
    private final MecanicoService mecanicos;
    private final ServicioService servicios;
    private final RepuestoService repuestos;

    public OrdenController(OrdenService ordenes, VehiculoService vehiculos, MecanicoService mecanicos,
                           ServicioService servicios, RepuestoService repuestos) {
        this.ordenes = ordenes;
        this.vehiculos = vehiculos;
        this.mecanicos = mecanicos;
        this.servicios = servicios;
        this.repuestos = repuestos;
    }

    @GetMapping
    public String listar(@RequestParam(required = false) String estado, Model modelo) {
        boolean sinFiltro = estado == null || estado.isBlank();
        modelo.addAttribute("ordenes", ordenes.listar(sinFiltro ? null : EstadoOrden.desde(estado)));
        modelo.addAttribute("estados", EstadoOrden.values());
        modelo.addAttribute("filtro", sinFiltro ? "" : estado);
        modelo.addAttribute("vehiculos", vehiculos.listar());
        modelo.addAttribute("mecanicos", mecanicos.listar());
        return "ordenes";
    }

    @PostMapping("/crear")
    public String crear(@RequestParam String placa, @RequestParam String mecanicoId,
                        @RequestParam String descripcion, RedirectAttributes atributos) {
        int id = ordenes.crear(placa, mecanicoId, descripcion);
        atributos.addFlashAttribute("ok", "Orden #" + id + " creada en estado PENDIENTE");
        return "redirect:/ordenes/" + id;
    }

    @GetMapping("/{id}")
    public String detalle(@PathVariable int id, Model modelo) {
        OrdenTrabajo orden = ordenes.obtener(id);
        modelo.addAttribute("orden", orden);
        modelo.addAttribute("serviciosDisponibles", servicios.listar());
        modelo.addAttribute("repuestosDisponibles", repuestos.listar());
        modelo.addAttribute("comprobante", ordenes.comprobante(id).orElse(null));
        return "orden";
    }

    @PostMapping("/{id}/iniciar")
    public String iniciar(@PathVariable int id, RedirectAttributes atributos) {
        ordenes.iniciar(id);
        atributos.addFlashAttribute("ok", "La orden pasó a EN PROCESO");
        return "redirect:/ordenes/" + id;
    }

    @PostMapping("/{id}/servicio")
    public String agregarServicio(@PathVariable int id, @RequestParam String servicioId, RedirectAttributes atributos) {
        ordenes.agregarServicio(id, servicioId);
        atributos.addFlashAttribute("ok", "Servicio registrado en la orden");
        return "redirect:/ordenes/" + id;
    }

    @PostMapping("/{id}/servicio/quitar")
    public String quitarServicio(@PathVariable int id, @RequestParam String servicioId, RedirectAttributes atributos) {
        ordenes.quitarServicio(id, servicioId);
        atributos.addFlashAttribute("ok", "Servicio retirado de la orden");
        return "redirect:/ordenes/" + id;
    }

    @PostMapping("/{id}/repuesto")
    public String asignarRepuesto(@PathVariable int id, @RequestParam String repuestoId,
                                  @RequestParam int cantidad, RedirectAttributes atributos) {
        ordenes.asignarRepuesto(id, repuestoId, cantidad);
        atributos.addFlashAttribute("ok", "Repuesto asignado y descontado del inventario");
        return "redirect:/ordenes/" + id;
    }

    @PostMapping("/{id}/repuesto/quitar")
    public String retirarRepuesto(@PathVariable int id, @RequestParam String repuestoId, RedirectAttributes atributos) {
        ordenes.retirarRepuesto(id, repuestoId);
        atributos.addFlashAttribute("ok", "Repuesto retirado; las unidades regresaron al inventario");
        return "redirect:/ordenes/" + id;
    }

    @PostMapping("/{id}/finalizar")
    public String finalizar(@PathVariable int id, RedirectAttributes atributos) {
        ordenes.finalizar(id);
        atributos.addFlashAttribute("ok", "Orden finalizada y comprobante generado");
        return "redirect:/ordenes/" + id;
    }

    @PostMapping("/{id}/cancelar")
    public String cancelar(@PathVariable int id, RedirectAttributes atributos) {
        ordenes.cancelar(id);
        atributos.addFlashAttribute("ok", "Orden cancelada; los repuestos regresaron al inventario");
        return "redirect:/ordenes/" + id;
    }
}

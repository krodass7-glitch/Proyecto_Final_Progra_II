package com.taller.controller.web;

import com.taller.model.EstadoOrden;
import com.taller.service.MecanicoService;
import com.taller.service.PdfService;
import com.taller.service.ReporteService;
import com.taller.service.VehiculoService;
import java.util.Map;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

@Controller
@RequestMapping("/reportes")
public class ReporteController {
    private final ReporteService reportes;
    private final PdfService pdf;
    private final MecanicoService mecanicos;
    private final VehiculoService vehiculos;

    public ReporteController(ReporteService reportes, PdfService pdf, MecanicoService mecanicos, VehiculoService vehiculos) {
        this.reportes = reportes;
        this.pdf = pdf;
        this.mecanicos = mecanicos;
        this.vehiculos = vehiculos;
    }

    @GetMapping
    public String menu(Model modelo) {
        modelo.addAttribute("estados", EstadoOrden.values());
        modelo.addAttribute("mecanicos", mecanicos.listar());
        modelo.addAttribute("vehiculos", vehiculos.listar());
        return "reportes";
    }

    @GetMapping(value = "/{tipo}", params = "formato=pdf")
    public ResponseEntity<byte[]> descargar(@PathVariable String tipo, @RequestParam Map<String, String> parametros) {
        byte[] contenido = pdf.generar(reportes.construir(tipo, parametros));
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"reporte-" + tipo + ".pdf\"")
                .contentType(MediaType.APPLICATION_PDF)
                .body(contenido);
    }

    @GetMapping("/{tipo}")
    public String ver(@PathVariable String tipo, @RequestParam Map<String, String> parametros, Model modelo) {
        modelo.addAttribute("reporte", reportes.construir(tipo, parametros));
        modelo.addAttribute("pdfUrl", ServletUriComponentsBuilder.fromCurrentRequest()
                .replaceQueryParam("formato", "pdf").toUriString());
        return "reporte";
    }
}

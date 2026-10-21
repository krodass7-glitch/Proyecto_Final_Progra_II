package com.taller.controller.api;

import com.taller.model.Reporte;
import com.taller.service.ReporteService;
import java.util.Map;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/reportes")
@CrossOrigin(origins = "*")
public class ReporteApiController {
    private final ReporteService reportes;

    public ReporteApiController(ReporteService reportes) {
        this.reportes = reportes;
    }

    @GetMapping("/{tipo}")
    public Reporte obtener(@PathVariable String tipo, @RequestParam Map<String, String> parametros) {
        return reportes.construir(tipo, parametros);
    }
}

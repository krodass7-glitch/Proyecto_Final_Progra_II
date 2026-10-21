package com.taller.controller.web;

import com.taller.exception.NegocioException;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.validation.BindException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@ControllerAdvice(basePackages = "com.taller.controller.web")
public class ErroresWeb {
    @ExceptionHandler({NegocioException.class, BindException.class,
            MethodArgumentTypeMismatchException.class, MissingServletRequestParameterException.class})
    public String manejar(Exception e, HttpServletRequest request, RedirectAttributes atributos) {
        String mensaje = e instanceof NegocioException ? e.getMessage() : "Los datos enviados no son válidos";
        atributos.addFlashAttribute("error", mensaje);
        String origen = request.getHeader("Referer");
        return "redirect:" + (origen != null ? origen : "/");
    }
}

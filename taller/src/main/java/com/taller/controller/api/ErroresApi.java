package com.taller.controller.api;

import com.taller.exception.NegocioException;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice(basePackages = "com.taller.controller.api")
public class ErroresApi {
    @ExceptionHandler(NegocioException.class)
    public ResponseEntity<Map<String, String>> manejar(NegocioException e) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", e.getMessage()));
    }
}

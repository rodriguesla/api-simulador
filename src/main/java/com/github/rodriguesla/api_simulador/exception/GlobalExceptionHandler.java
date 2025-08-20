//package com.github.rodriguesla.api_simulador.exception;
//
//import io.swagger.v3.oas.annotations.Hidden;
//import org.springframework.http.HttpStatus;
//import org.springframework.http.ResponseEntity;
//import org.springframework.web.bind.annotation.ControllerAdvice;
//import org.springframework.web.bind.annotation.ExceptionHandler;
//import org.springframework.web.bind.annotation.RestControllerAdvice;
//
//@ControllerAdvice
//@RestControllerAdvice(basePackages = "com.github.rodriguesla.api_simulador.controller")
//public class GlobalExceptionHandler {
//
//    @Hidden
//    @ExceptionHandler(ValorOuPrazoDesejadoInvalidoException.class)
//    public ResponseEntity<Object> handleValorOuPrazoDesejadoInvalidoException(ValorOuPrazoDesejadoInvalidoException ex) {
//        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
//                new java.util.HashMap<String, Object>() {{
//                    put("error", "InvalidValueOrTerm");
//                    put("message", "Valor desejado ou prazo fora do intervalo permitido.");
//                    put("code", 400);
//                }}
//        );
//    }
//
//    // Add handlers for 401, 403, 404, 500 as needed
//}

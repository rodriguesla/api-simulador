// Java
package com.github.rodriguesla.api_simulador.exception;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerExceptionResolver;
import org.springframework.web.servlet.ModelAndView;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

@Component
public class CustomExceptionResolver implements HandlerExceptionResolver {
    @Override
    public ModelAndView resolveException(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        response.setContentType("application/json");
        response.setStatus(HttpStatus.BAD_REQUEST.value());
        String body;
        if (ex instanceof ValorOuPrazoDesejadoInvalidoException) {
            body = "{\"erro\":\"ValorInvalido\",\"mensagem\":\"" + ex.getMessage() + "\",\"codigo\":400}";
        } else if (ex instanceof RequisicaoInvalidaException) {
            body = "{\"erro\":\"RequisicaoInvalida\",\"mensagem\":\"" + ex.getMessage() + "\",\"codigo\":400}";
        } else {
            response.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
            body = "{\"erro\":\"ErroInterno\",\"mensagem\":\"" + ex.getMessage() + "\",\"codigo\":500}";
        }
        try {
            response.getWriter().write(body);
        } catch (IOException e) {
            // log error if needed
        }
        return new ModelAndView();
    }
}
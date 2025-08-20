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
        String body;
        switch (ex) {
            case ValorOuPrazoDesejadoInvalidoException valorOuPrazoDesejadoInvalidoException -> {
                response.setStatus(HttpStatus.BAD_REQUEST.value());
                body = "{\"erro\":\"ValorInvalido\",\"mensagem\":\"" + ex.getMessage() + "\",\"codigo\":400}";
            }
            case RequisicaoInvalidaException requisicaoInvalidaException -> {
                response.setStatus(HttpStatus.BAD_REQUEST.value());
                body = "{\"erro\":\"RequisicaoInvalida\",\"mensagem\":\"" + ex.getMessage() + "\",\"codigo\":400}";
            }
            case AcessoNegadoException acessoNegadoException -> {
                response.setStatus(HttpStatus.FORBIDDEN.value());
                body = "{\"erro\":\"Proibido\",\"mensagem\":\"Acesso negado.\",\"codigo\":403}";
            }
            case org.springframework.web.servlet.NoHandlerFoundException noHandlerFoundException -> {
                response.setStatus(HttpStatus.NOT_FOUND.value());
                body = "{\"erro\":\"NaoEncontrado\",\"mensagem\":\"Recurso não encontrado.\",\"codigo\":404}";
            }
            default -> {
                response.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
                body = "{\"erro\":\"ErroInterno\",\"mensagem\":\"Erro interno do servidor.\",\"codigo\":500}";
            }
        }
        try {
            response.getWriter().write(body);
        } catch (IOException e) {
            org.slf4j.LoggerFactory.getLogger(CustomExceptionResolver.class)
                    .error("Falha na resposta do erro: {}", e.getMessage(), e);
        }
        return new ModelAndView();
    }
}
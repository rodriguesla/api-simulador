// Java
package com.github.rodriguesla.api_simulador.exception;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.github.rodriguesla.api_simulador.dto.ErroResponseDTO;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerExceptionResolver;
import org.springframework.web.servlet.ModelAndView;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.time.LocalDateTime;

@Component
public class CustomExceptionResolver implements HandlerExceptionResolver {
    @Override
    public ModelAndView resolveException(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        response.setContentType("application/json");
        ErroResponseDTO erroResponse = new ErroResponseDTO();
        erroResponse.setTimestamp(LocalDateTime.now());
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        switch (ex) {
            case ValorOuPrazoDesejadoInvalidoException valorOuPrazoDesejadoInvalidoException -> {
                response.setStatus(HttpStatus.BAD_REQUEST.value());
                erroResponse.setErro("ValorInvalido");
                erroResponse.setMensagem(ex.getMessage());
                erroResponse.setCodigo(400);
            }
            case RequisicaoInvalidaException requisicaoInvalidaException -> {
                response.setStatus(HttpStatus.BAD_REQUEST.value());
                erroResponse.setErro("RequisicaoInvalida");
                erroResponse.setMensagem(ex.getMessage());
                erroResponse.setCodigo(400);
            }
            case AcessoNegadoException acessoNegadoException -> {
                response.setStatus(HttpStatus.FORBIDDEN.value());
                erroResponse.setErro("Proibido");
                erroResponse.setMensagem(ex.getMessage());
                erroResponse.setCodigo(403);
            }
            case SimulacaoNaoEncontradaException simulacaoNaoEncontradaException -> {
                response.setStatus(HttpStatus.NOT_FOUND.value());
                erroResponse.setErro("NaoEncontrado");
                erroResponse.setMensagem(ex.getMessage());
                erroResponse.setCodigo(404);
            }
            default -> {
                response.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
                erroResponse.setErro("ErroInterno");
                erroResponse.setMensagem("Erro interno do servidor.");
                erroResponse.setCodigo(500);
            }
        }
        try {
            String body = mapper.writeValueAsString(erroResponse);
            response.getWriter().write(body);
        } catch (IOException e) {
            try {
                response.getWriter().write("{\"erro\":\"ErroInterno\",\"mensagem\":\"Erro ao serializar resposta de erro.\",\"codigo\":500}");
            } catch (IOException ignored) {}
        }
        return new ModelAndView();
    }
}
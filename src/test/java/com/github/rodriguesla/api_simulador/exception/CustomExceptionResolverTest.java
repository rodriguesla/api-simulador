// src/test/java/com/github/rodriguesla/api_simulador/exception/CustomExceptionResolverTest.java
package com.github.rodriguesla.api_simulador.exception;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.servlet.NoHandlerFoundException;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

class CustomExceptionResolverTest {

    private CustomExceptionResolver resolver;
    private HttpServletRequest request;
    private HttpServletResponse response;
    private StringWriter responseWriter;
    private static final String MSG_VALOR_INVALIDO = "Valor inválido";

    @BeforeEach
    void setUp() throws Exception {
        resolver = new CustomExceptionResolver();
        request = mock(HttpServletRequest.class);
        response = mock(HttpServletResponse.class);
        responseWriter = new StringWriter();
        PrintWriter printWriter = new PrintWriter(responseWriter);
        when(response.getWriter()).thenReturn(printWriter);
    }

    @Test
    void testValorOuPrazoDesejadoInvalidoException() throws Exception {
        Exception ex = new ValorOuPrazoDesejadoInvalidoException(MSG_VALOR_INVALIDO);
        resolver.resolveException(request, response, null, ex);
        verify(response).setStatus(400);
        assertTrue(responseWriter.toString().contains("\"erro\":\"ValorInvalido\""));
        assertTrue(responseWriter.toString().contains("\"mensagem\":\"" + MSG_VALOR_INVALIDO + "\""));
    }

    @Test
    void testRequisicaoInvalidaException() throws Exception {
        Exception ex = new RequisicaoInvalidaException("Requisição inválida");
        resolver.resolveException(request, response, null, ex);
        verify(response).setStatus(400);
        assertTrue(responseWriter.toString().contains("\"erro\":\"RequisicaoInvalida\""));
        assertTrue(responseWriter.toString().contains("\"mensagem\":\"Requisição inválida\""));
    }

    @Test
    void testAcessoNegadoException() throws Exception {
        Exception ex = new AcessoNegadoException("Acesso negado");
        resolver.resolveException(request, response, null, ex);
        verify(response).setStatus(403);
        assertTrue(responseWriter.toString().contains("\"erro\":\"Proibido\""));
        assertTrue(responseWriter.toString().contains("\"mensagem\":\"Acesso negado.\""));
    }

    @Test
    void testSimulacaoNaoEncontradaException() throws Exception {
        Exception ex = new SimulacaoNaoEncontradaException("Recurso não encontrado.");
        resolver.resolveException(request, response, null, ex);
        verify(response).setStatus(404);
        assertTrue(responseWriter.toString().contains("\"erro\":\"NaoEncontrado\""));
        assertTrue(responseWriter.toString().contains("\"mensagem\":\"Recurso não encontrado.\""));
    }

    @Test
    void testGenericException() throws Exception {
        Exception ex = new RuntimeException("Erro genérico");
        resolver.resolveException(request, response, null, ex);
        verify(response).setStatus(500);
        assertTrue(responseWriter.toString().contains("\"erro\":\"ErroInterno\""));
        assertTrue(responseWriter.toString().contains("\"mensagem\":\"Erro interno do servidor.\""));
    }

    @Test
    void testIOExceptionInResponseWriter() throws Exception {
        resolver = new CustomExceptionResolver();
        request = mock(HttpServletRequest.class);
        response = mock(HttpServletResponse.class);
        // Simulate IOException when getWriter() is called
        when(response.getWriter()).thenThrow(new IOException("Test IO error"));
        Exception ex = new ValorOuPrazoDesejadoInvalidoException(MSG_VALOR_INVALIDO);
        // Should not throw, just cover the catch block
        resolver.resolveException(request, response, null, ex);
    }
}
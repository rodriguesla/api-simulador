package com.github.rodriguesla.api_simulador.service;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.util.ContentCachingRequestWrapper;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;

import static org.mockito.Mockito.*;

class SimulacaoAuditInterceptorTest {
    private SimulacaoAuditLogService auditLogService;
    private SimulacaoAuditInterceptor interceptor;
    private HttpServletRequest request;
    private HttpServletResponse response;

    @BeforeEach
    void setUp() {
        auditLogService = mock(SimulacaoAuditLogService.class);
        interceptor = new SimulacaoAuditInterceptor();
        try {
            var field = SimulacaoAuditInterceptor.class.getDeclaredField("simulacaoAuditLogService");
            field.setAccessible(true);
            field.set(interceptor, auditLogService);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        request = mock(HttpServletRequest.class);
        response = mock(HttpServletResponse.class);
    }

    @Test
    void testLogsRelevantRequest() {
        when(request.getRequestURI()).thenReturn("/api/test");
        when(request.getUserPrincipal()).thenReturn(null);
        when(response.getStatus()).thenReturn(200);
        when(request.getMethod()).thenReturn("GET");

        interceptor.afterCompletion(request, response, null, null);
        verify(auditLogService).log(eq("anonymous"), eq("/api/test"), any(), eq("200"), any(), isNull());
    }

    @Test
    void testSkipsStaticResource() {
        when(request.getRequestURI()).thenReturn("/css/styles.css");
        interceptor.afterCompletion(request, response, null, null);
        verify(auditLogService, never()).log(any(), any(), any(), any(), any(), any());
    }

    @Test
    void testSkipsAuditoriaView() {
        when(request.getRequestURI()).thenReturn("/auditoria-view");
        interceptor.afterCompletion(request, response, null, null);
        verify(auditLogService, never()).log(any(), any(), any(), any(), any(), any());
    }

    @Test
    void testLogsPostRequestWithBody() {
        when(request.getRequestURI()).thenReturn("/api/test");
        when(request.getUserPrincipal()).thenReturn(null);
        when(response.getStatus()).thenReturn(200);
        when(request.getMethod()).thenReturn("POST");
        ContentCachingRequestWrapper wrapper = mock(ContentCachingRequestWrapper.class);
        when(wrapper.getRequestURI()).thenReturn("/api/test");
        when(wrapper.getUserPrincipal()).thenReturn(null);
        when(wrapper.getMethod()).thenReturn("POST");
        when(wrapper.getContentAsByteArray()).thenReturn("body-data".getBytes());
        when(wrapper.getCharacterEncoding()).thenReturn("UTF-8");
        when(response.getStatus()).thenReturn(200);

        interceptor.afterCompletion(wrapper, response, null, null);
        verify(auditLogService).log(eq("anonymous"), eq("/api/test"), eq("body-data"), eq("200"), any(), isNull());
    }

    @Test
    void testLogsPostRequestWithUnwrappedBody() throws IOException {
        when(request.getRequestURI()).thenReturn("/api/test");
        when(request.getUserPrincipal()).thenReturn(null);
        when(response.getStatus()).thenReturn(200);
        when(request.getMethod()).thenReturn("POST");
        BufferedReader reader = new BufferedReader(new StringReader("body-line-1\nbody-line-2"));
        when(request.getReader()).thenReturn(reader);

        interceptor.afterCompletion(request, response, null, null);
        verify(auditLogService).log(eq("anonymous"), eq("/api/test"), eq("body-line-1body-line-2"), eq("200"), any(), isNull());
    }

    @Test
    void testLogsErrorStatus() {
        when(request.getRequestURI()).thenReturn("/api/test");
        when(request.getUserPrincipal()).thenReturn(null);
        when(request.getMethod()).thenReturn("GET");
        when(response.getStatus()).thenReturn(400);

        interceptor.afterCompletion(request, response, null, null);
        verify(auditLogService).log(eq("anonymous"), eq("/api/test"), any(), eq("400"), any(), eq("HTTP 400"));
    }

    @Test
    void testLogsException() {
        when(request.getRequestURI()).thenReturn("/api/test");
        when(request.getUserPrincipal()).thenReturn(null);
        when(request.getMethod()).thenReturn("GET");
        when(response.getStatus()).thenReturn(500);
        Exception ex = new Exception("Test error");

        interceptor.afterCompletion(request, response, null, ex);
        verify(auditLogService).log(eq("anonymous"), eq("/api/test"), any(), eq("500"), any(), eq("Test error"));
    }

}

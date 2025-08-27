package com.github.rodriguesla.api_simulador.service;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.util.ContentCachingRequestWrapper;

import java.io.BufferedReader;
import java.io.IOException;

@Component
public class SimulacaoAuditInterceptor implements HandlerInterceptor {
    @Autowired
    private SimulacaoAuditLogService simulacaoAuditLogService;

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        String endpoint = request.getRequestURI();
        if (endpoint.startsWith("/css/") || endpoint.startsWith("/js/") || endpoint.startsWith("/images/") || endpoint.startsWith("/webjars/") || endpoint.startsWith("/static/") || endpoint.equals("/auditoria-view")) {
            return;
        }
        String username = request.getUserPrincipal() != null ? request.getUserPrincipal().getName() : "anonymous";
        String requestData = null;
        try {
            if ("POST".equalsIgnoreCase(request.getMethod()) || "PUT".equalsIgnoreCase(request.getMethod())) {
                if (request instanceof ContentCachingRequestWrapper) {
                    ContentCachingRequestWrapper wrapper = (ContentCachingRequestWrapper) request;
                    byte[] buf = wrapper.getContentAsByteArray();
                    requestData = buf.length > 0 ? new String(buf, wrapper.getCharacterEncoding()) : null;
                } else {
                    requestData = getRequestBody(request);
                }
            }
        } catch (Exception e) {
            requestData = "[erro ao ler corpo da requisição]";
        }
        String responseStatus = String.valueOf(response.getStatus());
        String ip = request.getRemoteAddr();
        String error;
        if (ex != null) {
            error = ex.getMessage();
        } else if (response.getStatus() >= 400) {
            error = "HTTP " + response.getStatus();
        } else {
            error = null;
        }
        simulacaoAuditLogService.log(username, endpoint, requestData, responseStatus, ip, error);
    }

    private String getRequestBody(HttpServletRequest request) {
        StringBuilder sb = new StringBuilder();
        try (BufferedReader reader = request.getReader()) {
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line);
            }
        } catch (IOException e) {
            return null;
        }
        return sb.toString();
    }
}

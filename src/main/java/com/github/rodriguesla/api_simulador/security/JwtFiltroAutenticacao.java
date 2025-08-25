package com.github.rodriguesla.api_simulador.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.github.rodriguesla.api_simulador.dto.ErroResponseDTO;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

@Component
@RequiredArgsConstructor
public class JwtFiltroAutenticacao extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private final ObjectMapper objectMapper;

    private static final List<String> EXCLUDED_PATHS = Arrays.asList(
            "/auth/token",
            "/swagger-ui",
            "/v3/api-docs",
            "/actuator",
            "/simulacoes-view",
            "/simulacoes-por-dia-view",
            "/telemetria-view",
            "/static/css",
            "/css",
            "/favicon.ico"
    );

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        String requestPath = request.getRequestURI();
        if (isExcludedPath(requestPath)) {
            filterChain.doFilter(request, response);
            return;
        }

        String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            sendErrorResponse(response, "Token de acesso requerido. Use o endpoint /auth/token para obter um token.");
            return;
        }

        String token = authHeader.substring(7);
        if (!jwtUtil.validateToken(token)) {
            sendErrorResponse(response, "Token inválido ou expirado. Gere um novo token usando /auth/token.");
            return;
        }

        filterChain.doFilter(request, response);
    }

    private void sendErrorResponse(HttpServletResponse response, String message) throws IOException {
        response.setStatus(403);
        response.setContentType("application/json");

        ErroResponseDTO erroResponse = new ErroResponseDTO();
        erroResponse.setErro("Proibido");
        erroResponse.setMensagem(message);
        erroResponse.setCodigo(403);
        erroResponse.setTimestamp(LocalDateTime.now());

        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

        try {
            String jsonResponse = objectMapper.writeValueAsString(erroResponse);
            response.getWriter().write(jsonResponse);
        } catch (Exception e) {
            response.getWriter().write("{\"erro\":\"Proibido\",\"mensagem\":\"" + message + "\",\"codigo\":403}");
        }
    }

    private boolean isExcludedPath(String requestPath) {
        return EXCLUDED_PATHS.stream().anyMatch(requestPath::startsWith);
    }
}
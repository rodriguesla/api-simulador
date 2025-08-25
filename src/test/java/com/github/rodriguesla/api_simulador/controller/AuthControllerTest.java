package com.github.rodriguesla.api_simulador.controller;

import com.github.rodriguesla.api_simulador.dto.AuthRequestDTO;
import com.github.rodriguesla.api_simulador.dto.AuthResponseDTO;
import com.github.rodriguesla.api_simulador.security.JwtUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AuthControllerTest {
    @Mock
    private JwtUtil jwtUtil;
    @InjectMocks
    private AuthController authController;

    @BeforeEach
    void setUp() {
        try (AutoCloseable mocks = MockitoAnnotations.openMocks(this)) {
            authController = new AuthController(jwtUtil);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    void generateTokenShouldReturnTokenForValidCredentials() {
        AuthRequestDTO request = AuthRequestDTO.builder()
                .usuario("hackathon")
                .password("2025")
                .build();
        when(jwtUtil.generateToken("hackathon")).thenReturn("mocked-token");
        ResponseEntity<?> response = authController.generateToken(request);
        assertEquals(200, response.getStatusCode().value());
        assertNotNull(response.getBody());
        assertInstanceOf(AuthResponseDTO.class, response.getBody());
        AuthResponseDTO body = (AuthResponseDTO) response.getBody();
        assertEquals("mocked-token", body.getToken());
        assertEquals("Bearer", body.getTipo());
        assertEquals("hackathon", body.getUsuario());
        assertEquals(3600, body.getExpiraEm());
    }

    @Test
    void generateTokenShouldReturn401ForInvalidCredentials() {
        AuthRequestDTO request = AuthRequestDTO.builder()
                .usuario("wrong")
                .password("wrong")
                .build();
        ResponseEntity<?> response = authController.generateToken(request);
        assertEquals(401, response.getStatusCode().value());
        assertNotNull(response.getBody());
        assertInstanceOf(com.github.rodriguesla.api_simulador.dto.ErroResponseDTO.class, response.getBody());
        com.github.rodriguesla.api_simulador.dto.ErroResponseDTO error = (com.github.rodriguesla.api_simulador.dto.ErroResponseDTO) response.getBody();
        assertEquals("NaoAutorizado", error.getErro());
        assertEquals("Credenciais inválidas", error.getMensagem());
        assertEquals(401, error.getCodigo());
        assertNotNull(error.getTimestamp());
    }
}

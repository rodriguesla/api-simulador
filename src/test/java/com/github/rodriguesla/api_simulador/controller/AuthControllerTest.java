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
        MockitoAnnotations.openMocks(this);
        authController = new AuthController(jwtUtil);
    }

    @Test
    void generateTokenShouldReturnTokenForValidCredentials() {
        AuthRequestDTO request = AuthRequestDTO.builder()
                .usuario("hackathon")
                .password("2025")
                .build();
        when(jwtUtil.generateToken("hackathon")).thenReturn("mocked-token");
        ResponseEntity<AuthResponseDTO> response = authController.generateToken(request);
        assertEquals(200, response.getStatusCodeValue());
        assertNotNull(response.getBody());
        assertEquals("mocked-token", response.getBody().getToken());
        assertEquals("Bearer", response.getBody().getTipo());
        assertEquals("hackathon", response.getBody().getUsuario());
        assertEquals(3600, response.getBody().getExpiraEm());
    }

    @Test
    void generateTokenShouldReturn401ForInvalidCredentials() {
        AuthRequestDTO request = AuthRequestDTO.builder()
                .usuario("wrong")
                .password("wrong")
                .build();
        ResponseEntity<AuthResponseDTO> response = authController.generateToken(request);
        assertEquals(401, response.getStatusCodeValue());
        assertNull(response.getBody());
    }
}


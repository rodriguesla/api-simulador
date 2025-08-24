package com.github.rodriguesla.api_simulador.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class JwtFiltroAutenticacaoTest {
    @Mock
    private JwtUtil jwtUtil;
    @Mock
    private FilterChain filterChain;
    @InjectMocks
    private JwtFiltroAutenticacao jwtFiltroAutenticacao;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        jwtFiltroAutenticacao = new JwtFiltroAutenticacao(jwtUtil);
    }

    @Test
    void testExcludedPathShouldPassThrough() throws ServletException, IOException {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRequestURI("/auth/token");
        MockHttpServletResponse response = new MockHttpServletResponse();
        jwtFiltroAutenticacao.doFilterInternal(request, response, filterChain);
        verify(filterChain, times(1)).doFilter(request, response);
    }

    @Test
    void testValidJwtShouldPassThrough() throws ServletException, IOException {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRequestURI("/api/test");
        request.addHeader("Authorization", "Bearer valid.jwt.token");
        when(jwtUtil.validateToken("valid.jwt.token")).thenReturn(true);
        MockHttpServletResponse response = new MockHttpServletResponse();
        jwtFiltroAutenticacao.doFilterInternal(request, response, filterChain);
        verify(filterChain, times(1)).doFilter(request, response);
    }

    @Test
    void testInvalidJwtShouldReturnError() throws ServletException, IOException {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRequestURI("/api/test");
        request.addHeader("Authorization", "Bearer invalid.jwt.token");
        when(jwtUtil.validateToken("invalid.jwt.token")).thenReturn(false);
        MockHttpServletResponse response = new MockHttpServletResponse();
        jwtFiltroAutenticacao.doFilterInternal(request, response, filterChain);
        assertEquals(403, response.getStatus());
        assertTrue(response.getContentAsString().contains("Token inválido ou expirado"));
        verify(filterChain, never()).doFilter(request, response);
    }

    @Test
    void testMissingJwtShouldReturnError() throws ServletException, IOException {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRequestURI("/api/test");
        MockHttpServletResponse response = new MockHttpServletResponse();
        jwtFiltroAutenticacao.doFilterInternal(request, response, filterChain);
        assertEquals(403, response.getStatus());
        assertTrue(response.getContentAsString().contains("Token de acesso requerido"));
        verify(filterChain, never()).doFilter(request, response);
    }

    @Test
    void testEmptyBearerTokenShouldReturnError() throws ServletException, IOException {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRequestURI("/api/test");
        request.addHeader("Authorization", "Bearer ");
        when(jwtUtil.validateToken("")).thenReturn(false);
        MockHttpServletResponse response = new MockHttpServletResponse();
        jwtFiltroAutenticacao.doFilterInternal(request, response, filterChain);
        assertEquals(403, response.getStatus());
        assertTrue(response.getContentAsString().contains("Token inválido ou expirado"));
        verify(filterChain, never()).doFilter(request, response);
    }

    @Test
    void testBearerWithoutSpaceShouldReturnError() throws ServletException, IOException {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRequestURI("/api/test");
        request.addHeader("Authorization", "Bearer");
        MockHttpServletResponse response = new MockHttpServletResponse();
        jwtFiltroAutenticacao.doFilterInternal(request, response, filterChain);
        assertEquals(403, response.getStatus());
        assertTrue(response.getContentAsString().contains("Token de acesso requerido"));
        verify(filterChain, never()).doFilter(request, response);
    }

    @Test
    void testNonBearerAuthorizationShouldReturnError() throws ServletException, IOException {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRequestURI("/api/test");
        request.addHeader("Authorization", "Basic something");
        MockHttpServletResponse response = new MockHttpServletResponse();
        jwtFiltroAutenticacao.doFilterInternal(request, response, filterChain);
        assertEquals(403, response.getStatus());
        assertTrue(response.getContentAsString().contains("Token de acesso requerido"));
        verify(filterChain, never()).doFilter(request, response);
    }

    @Test
    void testJwtUtilThrowsExceptionShouldPropagate() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRequestURI("/api/test");
        request.addHeader("Authorization", "Bearer valid.jwt.token");
        when(jwtUtil.validateToken("valid.jwt.token")).thenThrow(new RuntimeException("JWT error"));
        MockHttpServletResponse response = new MockHttpServletResponse();
        assertThrows(RuntimeException.class, () -> jwtFiltroAutenticacao.doFilterInternal(request, response, filterChain));
    }

    @Test
    void testFilterChainThrowsServletExceptionShouldPropagate() throws IOException, ServletException{
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRequestURI("/api/test");
        request.addHeader("Authorization", "Bearer valid.jwt.token");
        when(jwtUtil.validateToken("valid.jwt.token")).thenReturn(true);
        MockHttpServletResponse response = new MockHttpServletResponse();
        doThrow(new ServletException("Filter error")).when(filterChain).doFilter(request, response);
        assertThrows(ServletException.class, () -> jwtFiltroAutenticacao.doFilterInternal(request, response, filterChain));
    }

    @Test
    void testFilterChainThrowsIOExceptionShouldPropagate() throws IOException, ServletException {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRequestURI("/api/test");
        request.addHeader("Authorization", "Bearer valid.jwt.token");
        when(jwtUtil.validateToken("valid.jwt.token")).thenReturn(true);
        MockHttpServletResponse response = new MockHttpServletResponse();
        doThrow(new IOException("IO error")).when(filterChain).doFilter(request, response);
        assertThrows(IOException.class, () -> jwtFiltroAutenticacao.doFilterInternal(request, response, filterChain));
    }

    @Test
    void testSingleCharTokenShouldReturnError() throws ServletException, IOException {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRequestURI("/api/test");
        request.addHeader("Authorization", "Bearer a");
        when(jwtUtil.validateToken("a")).thenReturn(false);
        MockHttpServletResponse response = new MockHttpServletResponse();
        jwtFiltroAutenticacao.doFilterInternal(request, response, filterChain);
        assertEquals(403, response.getStatus());
        assertTrue(response.getContentAsString().contains("Token inválido ou expirado"));
        verify(filterChain, never()).doFilter(request, response);
    }

    @Test
    void testExcludedPathWithAuthorizationHeaderShouldPassThrough() throws ServletException, IOException {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRequestURI("/auth/token");
        request.addHeader("Authorization", "Bearer valid.jwt.token");
        MockHttpServletResponse response = new MockHttpServletResponse();
        jwtFiltroAutenticacao.doFilterInternal(request, response, filterChain);
        verify(filterChain, times(1)).doFilter(request, response);
    }

    @Test
    void isExcludedPathShouldReturnFalseForNonExcludedPaths() throws Exception {
        JwtFiltroAutenticacao filtro = new JwtFiltroAutenticacao(jwtUtil);
        var method = JwtFiltroAutenticacao.class.getDeclaredMethod("isExcludedPath", String.class);
        method.setAccessible(true);
        assertFalse((Boolean) method.invoke(filtro, "/api/test"));
        assertFalse((Boolean) method.invoke(filtro, "/other/path"));
        assertFalse((Boolean) method.invoke(filtro, "/auth"));
    }

    @Test
    void isExcludedPathShouldReturnTrueForExcludedPaths() throws Exception {
        JwtFiltroAutenticacao filtro = new JwtFiltroAutenticacao(jwtUtil);
        var method = JwtFiltroAutenticacao.class.getDeclaredMethod("isExcludedPath", String.class);
        method.setAccessible(true);
        assertTrue((Boolean) method.invoke(filtro, "/auth/token"));
        assertTrue((Boolean) method.invoke(filtro, "/auth/tokensomethingelse"));
        assertTrue((Boolean) method.invoke(filtro, "/swagger-ui"));
        assertTrue((Boolean) method.invoke(filtro, "/v3/api-docs"));
        assertTrue((Boolean) method.invoke(filtro, "/actuator"));
    }
}

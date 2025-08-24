package com.github.rodriguesla.api_simulador.config;

import com.github.rodriguesla.api_simulador.security.JwtFiltroAutenticacao;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.web.servlet.FilterRegistrationBean;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class SecurityConfigTest {
    private JwtFiltroAutenticacao jwtFiltroAutenticacao;
    private SecurityConfig securityConfig;

    @BeforeEach
    void setUp() {
        jwtFiltroAutenticacao = mock(JwtFiltroAutenticacao.class);
        securityConfig = new SecurityConfig(jwtFiltroAutenticacao);
    }

    @Test
    void jwtFilterBeanShouldBeConfiguredCorrectly() {
        FilterRegistrationBean<JwtFiltroAutenticacao> bean = securityConfig.jwtFilter();
        assertEquals(jwtFiltroAutenticacao, bean.getFilter());
        assertTrue(bean.getUrlPatterns().contains("/*"));
        assertEquals(1, bean.getOrder());
    }
}


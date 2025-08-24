package com.github.rodriguesla.api_simulador.config;

import com.github.rodriguesla.api_simulador.security.JwtFiltroAutenticacao;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtFiltroAutenticacao jwtFiltroAutenticacao;

    @Bean
    public FilterRegistrationBean<JwtFiltroAutenticacao> jwtFilter() {
        FilterRegistrationBean<JwtFiltroAutenticacao> registrationBean = new FilterRegistrationBean<>();
        registrationBean.setFilter(jwtFiltroAutenticacao);
        registrationBean.addUrlPatterns("/*");
        registrationBean.setOrder(1);
        return registrationBean;
    }
}

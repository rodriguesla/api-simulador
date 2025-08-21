package com.github.rodriguesla.api_simulador.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {
    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("API Simulador de Crédito")
                        .version("1.0.0")
                        .description("API pública para simulação de empréstimos, disponível para todos os brasileiros. Permite consultar condições de negociação de crédito de forma digital.")
                        .contact(new Contact()
                                .name("Luciano Aymone Rodrigues")
                                .email("luciano.a.rodrigues@caixa.gov.br")
                                .url("https://github.com/rodriguesla/api-simulador"))
                        .license(new License()
                                .name("Caixa Econômica Federal")
                                .url("https://www.caixa.gov.br"))
                )
                .addServersItem(new Server().url("http://localhost:8080").description("Servidor Local"))
                .addServersItem(new Server().url("http://18.230.17.128:8080").description("Servidor Público"));
    }
}
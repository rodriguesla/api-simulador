package com.github.rodriguesla.api_simulador;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;

@SpringBootApplication
@EntityScan(basePackages = {
    "com.github.rodriguesla.api_simulador.entities.simulacao.model",
    "com.github.rodriguesla.api_simulador.entities.produto.model"
})
public class ApiSimuladorApplication {

    public static void main(String[] args) {
        SpringApplication.run(ApiSimuladorApplication.class, args);
    }

}

package com.github.rodriguesla.api_simulador.dto;

import java.time.LocalDateTime;

import lombok.Data;

@Data
public class ErroResponseDTO {
    private LocalDateTime timestamp;
    private String erro;
    private String mensagem;
    private int codigo;
}
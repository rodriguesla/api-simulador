package com.github.rodriguesla.api_simulador.dto;

import lombok.Data;

@Data
public class ErroResponseDTO {
    private String erro;
    private String mensagem;
    private int codigo;
}
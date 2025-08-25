package com.github.rodriguesla.api_simulador.dto;

import java.time.LocalDateTime;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
@Data
public class ErroResponseDTO {
    @Schema(required = true, description = "Timestamp do erro")
    private LocalDateTime timestamp;
    @Schema(required = true, description = "Tipo do erro")
    private String erro;
    @Schema(required = true, description = "Mensagem detalhada do erro")
    private String mensagem;
    @Schema(required = true, description = "Código HTTP do erro")
    private int codigo;
}
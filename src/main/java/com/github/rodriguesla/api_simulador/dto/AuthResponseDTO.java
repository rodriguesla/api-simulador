package com.github.rodriguesla.api_simulador.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuthResponseDTO {
    @Schema(required = true, description = "Token JWT gerado para autenticação")
    private String token;
    @Schema(required = true, description = "Tipo do token JWT")
    private String tipo;
    @Schema(required = true, description = "Usuário autenticado")
    private String usuario;
    @Schema(required = true, description = "Timestamp de expiração do token JWT")
    private long expiraEm;
}

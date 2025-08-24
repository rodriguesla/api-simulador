package com.github.rodriguesla.api_simulador.controller;

import com.github.rodriguesla.api_simulador.dto.AuthRequestDTO;
import com.github.rodriguesla.api_simulador.dto.AuthResponseDTO;
import com.github.rodriguesla.api_simulador.dto.ErroResponseDTO;
import com.github.rodriguesla.api_simulador.security.JwtUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Autenticação", description = "Endpoints para geração e validação de tokens de acesso")
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final JwtUtil jwtUtil;

    private static final String DEMO_USERNAME = "hackathon";
    private static final String DEMO_PASSWORD = "2025";

    @Operation(summary = "Gera token de acesso", description = "Autentica usuário e retorna token JWT para acesso aos endpoints protegidos")
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            description = "Token gerado com sucesso",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = AuthResponseDTO.class))
        ),
        @ApiResponse(responseCode = "401", description = "Credenciais inválidas", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErroResponseDTO.class)))
    })
    @PostMapping("/token")
    public ResponseEntity<AuthResponseDTO> generateToken(@RequestBody AuthRequestDTO authRequest) {

        if (!DEMO_USERNAME.equals(authRequest.getUsuario()) || !DEMO_PASSWORD.equals(authRequest.getPassword())) {
            return ResponseEntity.status(401).build();
        }

        String token = jwtUtil.generateToken(authRequest.getUsuario());

        AuthResponseDTO response = AuthResponseDTO.builder()
                .token(token)
                .tipo("Bearer")
                .usuario(authRequest.getUsuario())
                .expiraEm(3600)
                .build();

        return ResponseEntity.ok(response);
    }
}

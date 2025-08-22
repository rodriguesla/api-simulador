package com.github.rodriguesla.api_simulador.controller;

import com.github.rodriguesla.api_simulador.dto.*;
import com.github.rodriguesla.api_simulador.service.SimulacaoCalculoService;
import com.github.rodriguesla.api_simulador.service.SimulacaoListaService;
import com.github.rodriguesla.api_simulador.service.SimulacaoPorDiaService;
import com.github.rodriguesla.api_simulador.service.SimulacaoTelemetriaService;
import io.micrometer.core.instrument.MeterRegistry;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Endpoints", description = "Gerencia simulações de crédito e fornece dados de telemetria")
@RestController
@RequestMapping("/simulacoes")
@RequiredArgsConstructor
public class SimulacaoController implements ApiResponseDocumentacao {
    private final SimulacaoCalculoService simulacaoCalculoService;
    private final SimulacaoListaService simulacaoListaService;
    private final SimulacaoPorDiaService simulacaoPorDiaService;
    private final SimulacaoTelemetriaService simulacaoTelemetriaService;
    private final MeterRegistry meterRegistry;

    @Operation(summary = "Realiza uma simulação de crédito", description = "Recebe um envelope JSON, valida, calcula SAC/PRICE, persiste e retorna o resultado.")
    @ApiResponse(
            responseCode = "200",
            description = "Simulação realizada com sucesso",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = SimulacaoResponseDTO.class))
    )
    @PostMapping
    public ResponseEntity<List<SimulacaoResponseDTO>> simular(@RequestBody SimulacaoRequestDTO request) {
        List<SimulacaoResponseDTO> responses = simulacaoCalculoService.simular(request);
        return ResponseEntity.ok(responses);
    }

    @Operation(summary = "Lista todas as simulações realizadas")
    @ApiResponse(
            responseCode = "200",
            description = "Sucesso ao listar simulações",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = SimulacaoListaResponseDTO.class))
    )
    @GetMapping
    public SimulacaoListaResponseDTO listarSimulacoes(
            @RequestParam(defaultValue = "1") int pagina,
            @RequestParam(defaultValue = "200") int qtdRegistrosPagina) {
        return simulacaoListaService.buscarSimulacoesLista(pagina, qtdRegistrosPagina);
    }

    @Operation(summary = "Retorna valores simulados por produto em cada dia")
    @ApiResponse(
            responseCode = "200",
            description = "Valores simulados retornados com sucesso",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = SimulacoesPorDiaResponseDTO.class))
    )
    @GetMapping("/por-dia")
    public SimulacoesPorDiaResponseDTO simulacoesPorDia(@RequestParam String data) {
        return simulacaoPorDiaService.buscarSimulacoesPorDia(data);
    }

    @Operation(summary = "Retorna dados de telemetria (volumes e tempos de resposta)")
    @ApiResponse(
            responseCode = "200",
            description = "Dados de telemetria retornados com sucesso",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = TelemetriaResponseDTO.class))
    )
    @GetMapping("/telemetria")
    public ResponseEntity<TelemetriaResponseDTO> telemetria() {
        TelemetriaResponseDTO response = simulacaoTelemetriaService.getTelemetria(meterRegistry);
        return ResponseEntity.ok(response);
    }
}
package com.github.rodriguesla.api_simulador.controller;

import com.github.rodriguesla.api_simulador.dto.*;
import com.github.rodriguesla.api_simulador.entities.produto.model.Produto;
import com.github.rodriguesla.api_simulador.entities.simulacao.model.Simulacao;
import com.github.rodriguesla.api_simulador.entities.simulacao.repository.SimulacaoRepository;
import com.github.rodriguesla.api_simulador.mapper.SimulacaoMapper;
import com.github.rodriguesla.api_simulador.service.SimulacaoService;
import io.micrometer.core.instrument.MeterRegistry;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Tag(name = "Endpoints", description = "Gerencia simulações de crédito e fornece dados de telemetria")
@RestController
@RequestMapping("/simulacoes")
@RequiredArgsConstructor
public class SimulacaoController {
    private final SimulacaoService simulacaoService;
    private final SimulacaoRepository simulacaoRepository;
    private final MeterRegistry meterRegistry;

    @Operation(summary = "Realiza uma simulação de crédito", description = "Recebe um envelope JSON, valida, calcula SAC/PRICE, persiste e retorna o resultado.")
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Simulação realizada com sucesso",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = SimulacaoResponseDTO.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Requisição inválida",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErroResponseDTO.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Proibido",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErroResponseDTO.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Não encontrado",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErroResponseDTO.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Erro interno do servidor",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErroResponseDTO.class)
                    )
            )
    })
    @PostMapping
    public ResponseEntity<?> simular(@RequestBody SimulacaoRequestDTO request) {
        List<SimulacaoResponseDTO> responses = simulacaoService.simular(request);
        return ResponseEntity.ok(responses);
    }

    @Operation(summary = "Lista todas as simulações realizadas")
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Sucesso ao listar simulações",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = SimulacaoListaResponseDTO.class)

                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Requisição inválida",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErroResponseDTO.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Proibido",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErroResponseDTO.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Não encontrado",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErroResponseDTO.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Erro interno do servidor",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErroResponseDTO.class)
                    )
            )
    })
    @GetMapping
    public SimulacaoListaResponseDTO listarSimulacoes(
            @RequestParam(defaultValue = "1") int pagina,
            @RequestParam(defaultValue = "200") int qtdRegistrosPagina) {

        List<Simulacao> simulacoes = simulacaoService.buscarSimulacoesPaginadas(pagina, qtdRegistrosPagina);

        List<SimulacaoRegistroDTO> registros = simulacoes.stream()
                .map(SimulacaoMapper::toRegistroDTO)
                .collect(Collectors.toList());

        long totalRegistros = simulacaoRepository.count();

        return SimulacaoListaResponseDTO.builder()
                .pagina(pagina)
                .qtdRegistros((int) totalRegistros)
                .qtdRegistrosPagina(qtdRegistrosPagina)
                .registros(registros)
                .build();
    }

    @Operation(summary = "Retorna valores simulados por produto em cada dia")
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Valores simulados retornados com sucesso",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = SimulacoesPorDiaResponseDTO.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Requisição inválida",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErroResponseDTO.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Proibido",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErroResponseDTO.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Não encontrado",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErroResponseDTO.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Erro interno do servidor",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErroResponseDTO.class)
                    )
            )
    })
    @GetMapping("/por-dia")
    public SimulacoesPorDiaResponseDTO simulacoesPorDia(@RequestParam String data) {
        LocalDate dia = LocalDate.parse(data);
        LocalDateTime start = dia.atStartOfDay();
        LocalDateTime end = dia.atTime(LocalTime.MAX);
        List<Simulacao> simulacoes = simulacaoRepository.findByDataSimulacaoBetween(start, end);

        List<Produto> produtos = simulacaoService.buscarTodosProdutos();
        Map<Integer, Produto> produtoMap = produtos.stream()
                .collect(Collectors.toMap(Produto::getId, p -> p));

        return SimulacaoMapper.toSimulacoesPorDiaResponseDTO(data, simulacoes, produtoMap);
    }

    @Operation(summary = "Retorna dados de telemetria (volumes e tempos de resposta)")
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Dados de telemetria retornados com sucesso",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = TelemetriaResponseDTO.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Requisição inválida",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErroResponseDTO.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Proibido",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErroResponseDTO.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Não encontrado",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErroResponseDTO.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Erro interno do servidor",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErroResponseDTO.class)
                    )
            )
    })
    @GetMapping("/telemetria")
    public ResponseEntity<TelemetriaResponseDTO> telemetria() {
        TelemetriaResponseDTO response = simulacaoService.getTelemetria(meterRegistry);
        return ResponseEntity.ok(response);
    }
}

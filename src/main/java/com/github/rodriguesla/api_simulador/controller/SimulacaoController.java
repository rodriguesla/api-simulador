package com.github.rodriguesla.api_simulador.controller;

import com.github.rodriguesla.api_simulador.dto.*;
import com.github.rodriguesla.api_simulador.mapper.SimulacaoMapper;
import com.github.rodriguesla.api_simulador.produto.model.Produto;
import com.github.rodriguesla.api_simulador.service.SimulacaoService;
import com.github.rodriguesla.api_simulador.simulacao.model.Simulacao;
import com.github.rodriguesla.api_simulador.simulacao.repository.SimulacaoRepository;
import io.micrometer.core.instrument.MeterRegistry;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/simulacoes")
@RequiredArgsConstructor
public class SimulacaoController {
    private final SimulacaoService simulacaoService;
    private final SimulacaoRepository simulacaoRepository;
    private final MeterRegistry meterRegistry;

    @Operation(summary = "Realiza uma simulação de crédito", description = "Recebe um envelope JSON, valida, calcula SAC/PRICE, persiste e retorna o resultado.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Simulação realizada com sucesso"),
            @ApiResponse(responseCode = "400", description = "Requisição inválida"),
            @ApiResponse(responseCode = "401", description = "Não autorizado"),
            @ApiResponse(responseCode = "403", description = "Proibido"),
            @ApiResponse(responseCode = "404", description = "Não encontrado"),
            @ApiResponse(responseCode = "500", description = "Erro interno do servidor")
    })
    @PostMapping
    public ResponseEntity<?> simular(@RequestBody SimulacaoRequestDTO request) {
        List<SimulacaoResponseDTO> responses = simulacaoService.simular(request);
        return ResponseEntity.ok(responses);
    }

    @Operation(summary = "Lista todas as simulações realizadas")
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
    @GetMapping("/telemetria")
    public ResponseEntity<TelemetriaResponseDTO> telemetria() {
        TelemetriaResponseDTO response = simulacaoService.getTelemetria(meterRegistry);
        return ResponseEntity.ok(response);
    }
}

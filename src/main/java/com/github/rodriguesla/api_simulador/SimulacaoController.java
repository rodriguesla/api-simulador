package com.github.rodriguesla.api_simulador;

import com.github.rodriguesla.api_simulador.dto.SimulacaoRequestDTO;
import com.github.rodriguesla.api_simulador.dto.SimulacaoResponseDTO;
import com.github.rodriguesla.api_simulador.model.Simulacao;
import com.github.rodriguesla.api_simulador.model.SimulacaoRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/simulacoes")
@RequiredArgsConstructor
public class SimulacaoController {
    private final SimulacaoService simulacaoService;
    private final SimulacaoRepository simulacaoRepository;

    @Operation(summary = "Realiza uma simulação de crédito", description = "Recebe um envelope JSON, valida, calcula SAC/PRICE, persiste e retorna o resultado.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Simulação realizada com sucesso"),
        @ApiResponse(responseCode = "400", description = "Dados inválidos ou produto não encontrado")
    })
    @PostMapping
    public ResponseEntity<?> simular(@RequestBody SimulacaoRequestDTO request) {
        Optional<SimulacaoResponseDTO> response = simulacaoService.simular(request);
        if (response.isPresent()) {
            return ResponseEntity.ok(response.get());
        } else {
            return ResponseEntity.badRequest().body("Nenhum produto encontrado para os parâmetros informados.");
        }
    }

    @Operation(summary = "Lista todas as simulações realizadas")
    @GetMapping
    public List<SimulacaoResponseDTO> listarSimulacoes() {
        return simulacaoRepository.findAll().stream()
                .map(s -> SimulacaoResponseDTO.builder()
                        .nomeProduto(s.getProduto().getNome())
                        .valorSolicitado(s.getValorSolicitado())
                        .prazoMeses(s.getPrazoMeses())
                        .resultadoSac(s.getResultadoSac())
                        .resultadoPrice(s.getResultadoPrice())
                        .build())
                .collect(Collectors.toList());
    }

    @Operation(summary = "Retorna valores simulados por produto em cada dia")
    @GetMapping("/por-dia")
    public List<SimulacaoResponseDTO> simulacoesPorDia(@RequestParam String data) {
        LocalDate dia = LocalDate.parse(data);
        LocalDateTime start = dia.atStartOfDay();
        LocalDateTime end = dia.atTime(LocalTime.MAX);
        return simulacaoRepository.findByDataSimulacaoBetween(start, end).stream()
                .map(s -> SimulacaoResponseDTO.builder()
                        .nomeProduto(s.getProduto().getNome())
                        .valorSolicitado(s.getValorSolicitado())
                        .prazoMeses(s.getPrazoMeses())
                        .resultadoSac(s.getResultadoSac())
                        .resultadoPrice(s.getResultadoPrice())
                        .build())
                .collect(Collectors.toList());
    }

    @Operation(summary = "Retorna dados de telemetria (volumes e tempos de resposta)")
    @GetMapping("/telemetria")
    public ResponseEntity<?> telemetria() {
        // Use Spring Actuator for real metrics; here is a stub
        long total = simulacaoRepository.count();
        return ResponseEntity.ok("Total de simulações: " + total);
    }
}


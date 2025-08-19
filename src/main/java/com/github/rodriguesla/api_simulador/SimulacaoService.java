package com.github.rodriguesla.api_simulador;

import com.github.rodriguesla.api_simulador.dto.SimulacaoRequestDTO;
import com.github.rodriguesla.api_simulador.dto.SimulacaoResponseDTO;
import com.github.rodriguesla.api_simulador.model.Produto;
import com.github.rodriguesla.api_simulador.model.ProdutoRepository;
import com.github.rodriguesla.api_simulador.model.Simulacao;
import com.github.rodriguesla.api_simulador.model.SimulacaoRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

@Service
public class SimulacaoService {
    private final ProdutoRepository produtoRepository;
    private final SimulacaoRepository simulacaoRepository;
    private final EventHubProducerService eventHubProducerService;

    public SimulacaoService(ProdutoRepository produtoRepository, SimulacaoRepository simulacaoRepository, EventHubProducerService eventHubProducerService) {
        this.produtoRepository = produtoRepository;
        this.simulacaoRepository = simulacaoRepository;
        this.eventHubProducerService = eventHubProducerService;
    }

    public Optional<SimulacaoResponseDTO> simular(SimulacaoRequestDTO request) {
        BigDecimal valorSolicitado = BigDecimal.valueOf(request.getValorSolicitado());
        Short prazoMeses = request.getPrazoMeses().shortValue();
        Produto produto = produtoRepository.findAll().stream()
                .filter(p -> valorSolicitado.compareTo(p.getValorMinimo()) >= 0 &&
                        (p.getValorMaximo() == null || valorSolicitado.compareTo(p.getValorMaximo()) <= 0))
                .filter(p -> prazoMeses >= p.getPrazoMinimo() &&
                        (p.getPrazoMaximo() == null || prazoMeses <= p.getPrazoMaximo()))
                .findFirst().orElse(null);
        if (produto == null) return Optional.empty();

        // Calculate SAC and PRICE
        BigDecimal resultadoSac = calcularSac(valorSolicitado, produto.getTaxaJuros(), prazoMeses);
        BigDecimal resultadoPrice = calcularPrice(valorSolicitado, produto.getTaxaJuros(), prazoMeses);

        // Persist simulation
        Simulacao simulacao = Simulacao.builder()
                .produto(produto)
                .valorSolicitado(valorSolicitado.doubleValue())
                .prazoMeses(prazoMeses.intValue())
                .resultadoSac(resultadoSac.doubleValue())
                .resultadoPrice(resultadoPrice.doubleValue())
                .dataSimulacao(LocalDateTime.now())
                .envelopeJson(null)
                .build();
        simulacaoRepository.save(simulacao);

        SimulacaoResponseDTO responseDTO = SimulacaoResponseDTO.builder()
                .nomeProduto(produto.getNome())
                .valorSolicitado(valorSolicitado.doubleValue())
                .prazoMeses(prazoMeses.intValue())
                .resultadoSac(resultadoSac.doubleValue())
                .resultadoPrice(resultadoPrice.doubleValue())
                .build();

        // Send to EventHub
        eventHubProducerService.sendSimulation(responseDTO);

        return Optional.of(responseDTO);
    }

    private BigDecimal calcularSac(BigDecimal valor, BigDecimal taxa, Short prazo) {
        BigDecimal amortizacao = valor.divide(BigDecimal.valueOf(prazo), 10, BigDecimal.ROUND_HALF_UP);
        BigDecimal saldoDevedor = valor;
        BigDecimal totalPago = BigDecimal.ZERO;
        for (int i = 0; i < prazo; i++) {
            BigDecimal juros = saldoDevedor.multiply(taxa);
            totalPago = totalPago.add(amortizacao).add(juros);
            saldoDevedor = saldoDevedor.subtract(amortizacao);
        }
        return totalPago.setScale(2, BigDecimal.ROUND_HALF_UP);
    }

    private BigDecimal calcularPrice(BigDecimal valor, BigDecimal taxa, Short prazo) {
        BigDecimal um = BigDecimal.ONE;
        BigDecimal taxaMaisUm = um.add(taxa);
        BigDecimal potencia = taxaMaisUm.pow(prazo);
        BigDecimal parcela = valor.multiply(taxa).multiply(potencia)
                .divide(potencia.subtract(um), 10, BigDecimal.ROUND_HALF_UP);
        return parcela.multiply(BigDecimal.valueOf(prazo)).setScale(2, BigDecimal.ROUND_HALF_UP);
    }
}

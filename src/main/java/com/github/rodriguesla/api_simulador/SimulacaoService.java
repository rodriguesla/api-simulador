package com.github.rodriguesla.api_simulador;

import com.github.rodriguesla.api_simulador.dto.ParcelaDTO;
import com.github.rodriguesla.api_simulador.dto.SimulacaoRequestDTO;
import com.github.rodriguesla.api_simulador.dto.SimulacaoResponseDTO;
import com.github.rodriguesla.api_simulador.dto.TipoSimulacaoDTO;
import com.github.rodriguesla.api_simulador.model.Produto;
import com.github.rodriguesla.api_simulador.model.ProdutoRepository;
import com.github.rodriguesla.api_simulador.model.Simulacao;
import com.github.rodriguesla.api_simulador.model.SimulacaoRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Service
public class SimulacaoService {
    private static final Logger logger = LoggerFactory.getLogger(SimulacaoService.class);

    private final ProdutoRepository produtoRepository;
    private final SimulacaoRepository simulacaoRepository;
//    private final EventHubProducerService eventHubProducerService;

    public SimulacaoService(ProdutoRepository produtoRepository, SimulacaoRepository simulacaoRepository
                            ) {
        this.produtoRepository = produtoRepository;
        this.simulacaoRepository = simulacaoRepository;
//        this.eventHubProducerService = eventHubProducerService;
    }

    public Optional<SimulacaoResponseDTO> simular(SimulacaoRequestDTO request) {
        logger.info("Recebendo solicitação de simulação: valor={}, prazo={}", request.getValorDesejado(), request.getPrazo());

        BigDecimal valorDesejado = BigDecimal.valueOf(request.getValorDesejado());
        Short prazo = request.getPrazo().shortValue();
        Produto produto = produtoRepository.findAll().stream()
                .filter(p -> valorDesejado.compareTo(p.getValorMinimo()) >= 0 &&
                        (p.getValorMaximo() == null || valorDesejado.compareTo(p.getValorMaximo()) <= 0))
                .filter(p -> prazo >= p.getPrazoMinimo() &&
                        (p.getPrazoMaximo() == null || prazo <= p.getPrazoMaximo()))
                .findFirst().orElse(null);
        if (produto == null) {
            logger.warn("Nenhum produto encontrado para os parâmetros informados.");

            return Optional.empty();
        }
        logger.info("Produto encontrado: {} (ID: {})", produto.getNome(), produto.getId());
        // Calcula SAC e PRICE
        List<ParcelaDTO> sacParcelas = calcularSacParcelas(valorDesejado, produto.getTaxaJuros(), prazo);
        List<ParcelaDTO> priceParcelas = calcularPriceParcelas(valorDesejado, produto.getTaxaJuros(), prazo);

        // Persiste simulacao
        Simulacao simulacao = Simulacao.builder()
                .produto(produto)
                .valorDesejado(valorDesejado.doubleValue())
                .prazo(prazo.intValue())
                .resultadoSac(sacParcelas.stream().mapToDouble(ParcelaDTO::getValorPrestacao).sum())
                .resultadoPrice(priceParcelas.stream().mapToDouble(ParcelaDTO::getValorPrestacao).sum())
                .dataSimulacao(LocalDateTime.now())
                .envelopeJson(null)
                .build();
        simulacaoRepository.save(simulacao);
        logger.info("Simulação persistida com sucesso. Data: {}", simulacao.getDataSimulacao());

        // Contruindo array resultadoSimulacao
        List<TipoSimulacaoDTO> resultadoSimulacao = new ArrayList<>();
        resultadoSimulacao.add(TipoSimulacaoDTO.builder()
                .tipo("SAC")
                .parcelas(sacParcelas)
                .build());
        resultadoSimulacao.add(TipoSimulacaoDTO.builder()
                .tipo("PRICE")
                .parcelas(priceParcelas)
                .build());

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd");
        String idSimulacaoStr = simulacao.getDataSimulacao().format(formatter);

        SimulacaoResponseDTO responseDTO = SimulacaoResponseDTO.builder()
                .idSimulacao(idSimulacaoStr)
                .codigoProduto(produto.getId())
                .descricaoProduto(produto.getNome())
                .taxaJuros(produto.getTaxaJuros().doubleValue())
                .resultadoSimulacao(resultadoSimulacao)
                .build();

        // Envia para EventHub
//        eventHubProducerService.sendSimulation(responseDTO);

        return Optional.of(responseDTO);
    }

    private List<ParcelaDTO> calcularSacParcelas(BigDecimal valor, BigDecimal taxa, Short prazo) {
        List<ParcelaDTO> parcelas = new ArrayList<>();
        BigDecimal amortizacao = valor.divide(BigDecimal.valueOf(prazo), 10, BigDecimal.ROUND_HALF_UP);
        BigDecimal saldoDevedor = valor;
        for (int i = 1; i <= prazo; i++) {
            BigDecimal juros = saldoDevedor.multiply(taxa);
            BigDecimal prestacao = amortizacao.add(juros);
            parcelas.add(ParcelaDTO.builder()
                    .numero(i)
                    .valorAmortizacao(amortizacao.setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue())
                    .valorJuros(juros.setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue())
                    .valorPrestacao(prestacao.setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue())
                    .build());
            saldoDevedor = saldoDevedor.subtract(amortizacao);
        }
        return parcelas;
    }

    private List<ParcelaDTO> calcularPriceParcelas(BigDecimal valor, BigDecimal taxa, Short prazo) {
        List<ParcelaDTO> parcelas = new ArrayList<>();
        BigDecimal um = BigDecimal.ONE;
        BigDecimal taxaMaisUm = um.add(taxa);
        BigDecimal potencia = taxaMaisUm.pow(prazo);
        BigDecimal parcelaFixa = valor.multiply(taxa).multiply(potencia)
                .divide(potencia.subtract(um), 10, BigDecimal.ROUND_HALF_UP);
        BigDecimal saldoDevedor = valor;
        for (int i = 1; i <= prazo; i++) {
            BigDecimal juros = saldoDevedor.multiply(taxa);
            BigDecimal amortizacao = parcelaFixa.subtract(juros);
            parcelas.add(ParcelaDTO.builder()
                    .numero(i)
                    .valorAmortizacao(amortizacao.setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue())
                    .valorJuros(juros.setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue())
                    .valorPrestacao(parcelaFixa.setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue())
                    .build());
            saldoDevedor = saldoDevedor.subtract(amortizacao);
        }
        return parcelas;
    }

    public List<Simulacao> buscarSimulacoesPaginadas(int pagina, int qtdRegistrosPagina) {
        logger.info("Listando simulações: página={}, quantidade por página={}", pagina, qtdRegistrosPagina);

        if (pagina < 1) pagina = 1;
        if (qtdRegistrosPagina < 1) qtdRegistrosPagina = 20; // or any default

        int offset = (pagina - 1) * qtdRegistrosPagina;
        List<Simulacao> simulacoes = simulacaoRepository.findAll();
        return simulacoes.stream()
                .skip(offset)
                .limit(qtdRegistrosPagina)
                .collect(Collectors.toList());
    }
}

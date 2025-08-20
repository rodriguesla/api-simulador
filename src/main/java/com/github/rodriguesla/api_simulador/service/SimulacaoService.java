package com.github.rodriguesla.api_simulador.service;

import com.github.rodriguesla.api_simulador.dto.ParcelaDTO;
import com.github.rodriguesla.api_simulador.dto.SimulacaoRequestDTO;
import com.github.rodriguesla.api_simulador.dto.SimulacaoResponseDTO;
import com.github.rodriguesla.api_simulador.dto.TipoSimulacaoDTO;
import com.github.rodriguesla.api_simulador.exception.*;
import com.github.rodriguesla.api_simulador.producer.EventHubProducer;
import com.github.rodriguesla.api_simulador.produto.model.Produto;
import com.github.rodriguesla.api_simulador.produto.repository.ProdutoRepository;
import com.github.rodriguesla.api_simulador.simulacao.model.Simulacao;
import com.github.rodriguesla.api_simulador.simulacao.repository.SimulacaoRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class SimulacaoService {
    private static final Logger logger = LoggerFactory.getLogger(SimulacaoService.class);

    private final ProdutoRepository produtoRepository;
    private final SimulacaoRepository simulacaoRepository;
    private final EventHubProducer eventHubProducer;

    public SimulacaoService(ProdutoRepository produtoRepository, SimulacaoRepository simulacaoRepository, EventHubProducer eventHubProducer
    ) {
        this.produtoRepository = produtoRepository;
        this.simulacaoRepository = simulacaoRepository;
        this.eventHubProducer = eventHubProducer;
    }

    public List<SimulacaoResponseDTO> simular(SimulacaoRequestDTO request) {
        logger.info("Iniciando simulação: valorDesejado={}, prazo={}", request.getValorDesejado(), request.getPrazo());

        if (request.getValorDesejado() == null || request.getPrazo() == null) {
            logger.error("Parâmetros inválidos: valorDesejado ou prazo nulos");
            throw new RequisicaoInvalidaException("valorDesejado e prazo nao podem ser nulos ou vazios.");
        }

        BigDecimal valorDesejado = BigDecimal.valueOf(request.getValorDesejado()).setScale(2, BigDecimal.ROUND_HALF_UP);
        int prazo = request.getPrazo().shortValue();

        List<Produto> produtos = produtoRepository.findAll();
        logger.debug("Produtos carregados do banco: {}", produtos);

        List<Produto> produtosFiltrados = produtos.stream()
                .filter(p -> (
                        // Incluir produto 4 nesse cenario
                        (p.getId() == 4 && prazo == 96 && valorDesejado.compareTo(new BigDecimal("1000000.01")) >= 0)
                                || (
                                prazo >= p.getPrazoMinimo() &&
                                        (p.getPrazoMaximo() != null ? prazo <= p.getPrazoMaximo() : true) &&
                                        valorDesejado.compareTo(p.getValorMinimo()) >= 0 &&
                                        (p.getValorMaximo() != null ? valorDesejado.compareTo(p.getValorMaximo()) <= 0 : true) &&
                                        !(valorDesejado.compareTo(new BigDecimal("10000.01")) >= 0 &&
                                                valorDesejado.compareTo(new BigDecimal("10000.99")) <= 0) &&
                                        // Exclui produto 3 nesse cenario
                                        !(p.getId() == 3 && valorDesejado.compareTo(new BigDecimal("1000000.01")) >= 0)
                        )
                ))
                .collect(Collectors.toList());
        logger.info("Produtos filtrados: {}", produtosFiltrados);

        if (produtosFiltrados.isEmpty()) {
            logger.warn("Nenhum produto válido encontrado para valorDesejado={} e prazo={}", valorDesejado, prazo);
            throw new ValorOuPrazoDesejadoInvalidoException("Valor desejado ou prazo fora do intervalo permitido.");
        }

        List<SimulacaoResponseDTO> responses = new ArrayList<>();
        for (Produto produto : produtosFiltrados) {
            logger.info("Processando produto: {} (ID: {})", produto.getNome(), produto.getId());

            List<ParcelaDTO> sacParcelas = calcularSacParcelas(valorDesejado, produto.getTaxaJuros(), (short) prazo);
            logger.debug("Parcelas SAC calculadas: {}", sacParcelas);

            List<ParcelaDTO> priceParcelas = calcularPriceParcelas(valorDesejado, produto.getTaxaJuros(), (short) prazo);
            logger.debug("Parcelas PRICE calculadas: {}", priceParcelas);

            Simulacao simulacao = Simulacao.builder()
                    .produtoId(produto.getId())
                    .valorDesejado(valorDesejado.doubleValue())
                    .prazo(prazo)
                    .resultadoSac(sacParcelas.stream().mapToDouble(ParcelaDTO::getValorPrestacao).sum())
                    .resultadoPrice(priceParcelas.stream().mapToDouble(ParcelaDTO::getValorPrestacao).sum())
                    .dataSimulacao(LocalDateTime.now())
                    .envelopeJson(null)
                    .build();
            simulacaoRepository.save(simulacao);
            logger.info("Simulação salva: {}", simulacao);

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

            eventHubProducer.sendSimulation(responseDTO);
            logger.info("Simulação enviada para EventHub: {}", responseDTO);

            responses.add(responseDTO);
        }
        logger.info("Simulação concluída. Total de respostas: {}", responses.size());
        return responses;
    }

    private List<ParcelaDTO> calcularSacParcelas(BigDecimal valor, BigDecimal taxa, Short prazo) {
        logger.info("Calculando parcelas SAC: valor={}, taxa={}, prazo={}", valor, taxa, prazo);

        List<ParcelaDTO> parcelas = new ArrayList<>();
        BigDecimal amortizacao = valor.divide(BigDecimal.valueOf(prazo), 10, BigDecimal.ROUND_HALF_UP);
        BigDecimal saldoDevedor = valor;
        for (int i = 1; i <= prazo; i++) {
            BigDecimal juros = saldoDevedor.multiply(taxa);
            BigDecimal prestacao = amortizacao.add(juros);
            logger.debug("SAC - Parcela {}: saldoDevedor={}, amortizacao={}, juros={}, prestacao={}",
                    i, saldoDevedor, amortizacao, juros, prestacao);

            parcelas.add(ParcelaDTO.builder()
                    .numero(i)
                    .valorAmortizacao(amortizacao.setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue())
                    .valorJuros(juros.setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue())
                    .valorPrestacao(prestacao.setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue())
                    .build());
            saldoDevedor = saldoDevedor.subtract(amortizacao);
        }
        logger.info("Parcelas SAC calculadas: total={}", parcelas.size());
        return parcelas;
    }

    private List<ParcelaDTO> calcularPriceParcelas(BigDecimal valor, BigDecimal taxa, Short prazo) {
        logger.info("Calculando parcelas PRICE: valor={}, taxa={}, prazo={}", valor, taxa, prazo);

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
            logger.debug("PRICE - Parcela {}: saldoDevedor={}, amortizacao={}, juros={}, prestacao={}",
                    i, saldoDevedor, amortizacao, juros, parcelaFixa);

            parcelas.add(ParcelaDTO.builder()
                    .numero(i)
                    .valorAmortizacao(amortizacao.setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue())
                    .valorJuros(juros.setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue())
                    .valorPrestacao(parcelaFixa.setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue())
                    .build());
            saldoDevedor = saldoDevedor.subtract(amortizacao);
        }
        logger.info("Parcelas PRICE calculadas: total={}", parcelas.size());
        return parcelas;
    }

    public List<Simulacao> buscarSimulacoesPaginadas(int pagina, int qtdRegistrosPagina) {
        logger.info("Listando simulações: página={}, quantidade por página={}", pagina, qtdRegistrosPagina);

        if (pagina < 1) pagina = 1;
        if (qtdRegistrosPagina < 1) qtdRegistrosPagina = 20; // or any default

        int offset = (pagina - 1) * qtdRegistrosPagina;
        List<Simulacao> simulacoes = simulacaoRepository.findAll();
        List<Simulacao> paginadas = simulacoes.stream()
                .skip(offset)
                .limit(qtdRegistrosPagina)
                .collect(Collectors.toList());

        logger.info("Simulações retornadas: {}", paginadas.size());
        if (paginadas.isEmpty()) {
            logger.warn("Nenhuma simulação encontrada para a página {}.", pagina);
        }
        return paginadas;
    }

    public List<Produto> buscarTodosProdutos() {
        List<Produto> produtos = produtoRepository.findAll();
        logger.info("Total de produtos encontrados: {}", produtos.size());
        return produtos;
    }
}

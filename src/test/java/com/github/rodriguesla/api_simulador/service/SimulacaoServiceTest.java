package com.github.rodriguesla.api_simulador.service;

import com.github.rodriguesla.api_simulador.dto.*;
import com.github.rodriguesla.api_simulador.entities.produto.model.Produto;
import com.github.rodriguesla.api_simulador.entities.simulacao.model.Simulacao;
import com.github.rodriguesla.api_simulador.entities.produto.repository.ProdutoRepository;
import com.github.rodriguesla.api_simulador.entities.simulacao.repository.SimulacaoRepository;
import com.github.rodriguesla.api_simulador.exception.RequisicaoInvalidaException;
import com.github.rodriguesla.api_simulador.exception.ValorOuPrazoDesejadoInvalidoException;
import com.github.rodriguesla.api_simulador.producer.EventHubProducer;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class SimulacaoServiceTest {

    @Test
    void simularValidRequestReturnsResponse() {
        ProdutoRepository produtoRepository = mock(ProdutoRepository.class);
        SimulacaoRepository simulacaoRepository = mock(SimulacaoRepository.class);
        EventHubProducer eventHubProducer = mock(EventHubProducer.class);

        Produto produto = Produto.builder()
                .id(1)
                .nome("Produto Teste")
                .taxaJuros(new BigDecimal("0.01"))
                .prazoMinimo((short) 1)
                .prazoMaximo((short) 12)
                .valorMinimo(new BigDecimal("1000"))
                .valorMaximo(new BigDecimal("10000"))
                .build();

        when(produtoRepository.findAll()).thenReturn(Collections.singletonList(produto));

        SimulacaoService service = new SimulacaoService(produtoRepository, simulacaoRepository, eventHubProducer);

        SimulacaoRequestDTO request = new SimulacaoRequestDTO();
        request.setValorDesejado(5000.0);
        request.setPrazo(12);

        List<SimulacaoResponseDTO> responses = service.simular(request);

        assertEquals(1, responses.size());
        SimulacaoResponseDTO response = responses.get(0);
        assertEquals(produto.getId(), response.getCodigoProduto());
        assertEquals(produto.getNome(), response.getDescricaoProduto());
        assertEquals(produto.getTaxaJuros().doubleValue(), response.getTaxaJuros());

        verify(simulacaoRepository, times(1)).save(any(Simulacao.class));
        verify(eventHubProducer, times(1)).sendSimulation(any(SimulacaoResponseDTO.class));
    }

    @Test
    void simularNullValuesThrowsException() {
        ProdutoRepository produtoRepository = mock(ProdutoRepository.class);
        SimulacaoRepository simulacaoRepository = mock(SimulacaoRepository.class);
        EventHubProducer eventHubProducer = mock(EventHubProducer.class);

        SimulacaoService service = new SimulacaoService(produtoRepository, simulacaoRepository, eventHubProducer);

        SimulacaoRequestDTO request = new SimulacaoRequestDTO();
        request.setValorDesejado(null);
        request.setPrazo(null);

        assertThrows(RequisicaoInvalidaException.class, () -> service.simular(request));
    }

    @Test
    void simularInvalidPrazoThrowsException() {
        ProdutoRepository produtoRepository = mock(ProdutoRepository.class);
        SimulacaoRepository simulacaoRepository = mock(SimulacaoRepository.class);
        EventHubProducer eventHubProducer = mock(EventHubProducer.class);

        SimulacaoService service = new SimulacaoService(produtoRepository, simulacaoRepository, eventHubProducer);

        SimulacaoRequestDTO request = new SimulacaoRequestDTO();
        request.setValorDesejado(5000.0);
        request.setPrazo(0);

        assertThrows(ValorOuPrazoDesejadoInvalidoException.class, () -> service.simular(request));
    }

    private List<ParcelaDTO> invokeCalcularSacParcelas(SimulacaoService service, BigDecimal valor, BigDecimal taxa, short prazo) throws Exception {
        var method = SimulacaoService.class.getDeclaredMethod("calcularSacParcelas", BigDecimal.class, BigDecimal.class, Short.class);
        method.setAccessible(true);
        @SuppressWarnings("unchecked")
        List<ParcelaDTO> parcelas = (List<ParcelaDTO>) method.invoke(service, valor, taxa, prazo);
        return parcelas;
    }

    @Test
    void calcularSacParcelasReturnsCorrectParcelas() throws Exception {
        SimulacaoService service = new SimulacaoService(null, null, null);
        BigDecimal valor = new BigDecimal("1000.00");
        BigDecimal taxa = new BigDecimal("0.01");
        short prazo = 2;

        List<ParcelaDTO> parcelas = invokeCalcularSacParcelas(service, valor, taxa, prazo);

        assertEquals(2, parcelas.size());
        assertEquals(1, parcelas.get(0).getNumero());
        assertEquals(500.00, parcelas.get(0).getValorAmortizacao());
        assertEquals(10.00, parcelas.get(0).getValorJuros());
        assertEquals(510.00, parcelas.get(0).getValorPrestacao());
        assertEquals(2, parcelas.get(1).getNumero());
        assertEquals(500.00, parcelas.get(1).getValorAmortizacao());
        assertEquals(5.00, parcelas.get(1).getValorJuros());
        assertEquals(505.00, parcelas.get(1).getValorPrestacao());
    }

    @Test
    void calcularSacParcelasZeroPrazoThrowsException() {
        SimulacaoService service = new SimulacaoService(null, null, null);
        BigDecimal valor = new BigDecimal("1000.00");
        BigDecimal taxa = new BigDecimal("0.01");
        short prazo = 0;

        assertThrows(ValorOuPrazoDesejadoInvalidoException.class, () -> {
            try {
                invokeCalcularSacParcelas(service, valor, taxa, prazo);
            } catch (Exception e) {
                Throwable cause = e.getCause();
                if (cause instanceof ValorOuPrazoDesejadoInvalidoException) {
                    throw (ValorOuPrazoDesejadoInvalidoException) cause;
                }
                throw new RuntimeException(e);
            }
        });
    }

    @Test
    void calcularSacParcelasNegativePrazoThrowsException() {
        SimulacaoService service = new SimulacaoService(null, null, null);
        BigDecimal valor = new BigDecimal("1000.00");
        BigDecimal taxa = new BigDecimal("0.01");
        short prazo = -1;

        assertThrows(ValorOuPrazoDesejadoInvalidoException.class, () -> {
            try {
                invokeCalcularSacParcelas(service, valor, taxa, prazo);
            } catch (Exception e) {
                // Unwrap reflection exception
                Throwable cause = e.getCause();
                if (cause instanceof ValorOuPrazoDesejadoInvalidoException) {
                    throw (ValorOuPrazoDesejadoInvalidoException) cause;
                }
                throw new RuntimeException(e);
            }
        });
    }

    private List<ParcelaDTO> invokeCalcularPriceParcelas(SimulacaoService service, BigDecimal valor, BigDecimal taxa, short prazo) throws Exception {
        var method = SimulacaoService.class.getDeclaredMethod("calcularPriceParcelas", BigDecimal.class, BigDecimal.class, Short.class);
        method.setAccessible(true);
        return (List<ParcelaDTO>) method.invoke(service, valor, taxa, prazo);
    }

    @Test
    void calcularPriceParcelasReturnsCorrectParcelas() throws Exception {
        SimulacaoService service = new SimulacaoService(null, null, null);
        BigDecimal valor = new BigDecimal("1000.00");
        BigDecimal taxa = new BigDecimal("0.01");
        short prazo = 2;

        List<ParcelaDTO> parcelas = invokeCalcularPriceParcelas(service, valor, taxa, prazo);

        assertEquals(2, parcelas.size());
        assertEquals(1, parcelas.get(0).getNumero());
        // You may want to check expected values for amortizacao, juros, prestacao here
        assertEquals(2, parcelas.get(1).getNumero());
    }

    @Test
    void calcularPriceParcelasZeroPrazoThrowsException() {
        SimulacaoService service = new SimulacaoService(null, null, null);
        BigDecimal valor = new BigDecimal("1000.00");
        BigDecimal taxa = new BigDecimal("0.01");
        short prazo = 0;

        assertThrows(ValorOuPrazoDesejadoInvalidoException.class, () -> {
            try {
                invokeCalcularPriceParcelas(service, valor, taxa, prazo);
            } catch (Exception e) {
                Throwable cause = e.getCause();
                if (cause instanceof ValorOuPrazoDesejadoInvalidoException) {
                    throw (ValorOuPrazoDesejadoInvalidoException) cause;
                }
                throw new RuntimeException(e);
            }
        });
    }

    @Test
    void calcularPriceParcelasNegativePrazoThrowsException() {
        SimulacaoService service = new SimulacaoService(null, null, null);
        BigDecimal valor = new BigDecimal("1000.00");
        BigDecimal taxa = new BigDecimal("0.01");
        short prazo = -1;

        assertThrows(ValorOuPrazoDesejadoInvalidoException.class, () -> {
            try {
                invokeCalcularPriceParcelas(service, valor, taxa, prazo);
            } catch (Exception e) {
                Throwable cause = e.getCause();
                if (cause instanceof ValorOuPrazoDesejadoInvalidoException) {
                    throw (ValorOuPrazoDesejadoInvalidoException) cause;
                }
                throw new RuntimeException(e);
            }
        });
    }

    @Test
    void buscarSimulacoesPaginadasReturnsCorrectPage() {
        SimulacaoRepository simulacaoRepository = mock(SimulacaoRepository.class);
        ProdutoRepository produtoRepository = mock(ProdutoRepository.class);
        EventHubProducer eventHubProducer = mock(EventHubProducer.class);

        Simulacao simulacao1 = Simulacao.builder().id(1L).build();
        Simulacao simulacao2 = Simulacao.builder().id(2L).build();
        Simulacao simulacao3 = Simulacao.builder().id(3L).build();

        when(simulacaoRepository.findAll()).thenReturn(List.of(simulacao1, simulacao2, simulacao3));

        SimulacaoService service = new SimulacaoService(produtoRepository, simulacaoRepository, eventHubProducer);

        List<Simulacao> page1 = service.buscarSimulacoesPaginadas(1, 2);
        assertEquals(2, page1.size());
        assertEquals(1L, page1.get(0).getId());
        assertEquals(2L, page1.get(1).getId());

        List<Simulacao> page2 = service.buscarSimulacoesPaginadas(2, 2);
        assertEquals(1, page2.size());
        assertEquals(3L, page2.get(0).getId());
    }

    @Test
    void buscarSimulacoesPaginadasHandlesInvalidParams() {
        SimulacaoRepository simulacaoRepository = mock(SimulacaoRepository.class);
        ProdutoRepository produtoRepository = mock(ProdutoRepository.class);
        EventHubProducer eventHubProducer = mock(EventHubProducer.class);

        Simulacao simulacao = Simulacao.builder().id(1L).build();
        when(simulacaoRepository.findAll()).thenReturn(List.of(simulacao));

        SimulacaoService service = new SimulacaoService(produtoRepository, simulacaoRepository, eventHubProducer);

        List<Simulacao> result = service.buscarSimulacoesPaginadas(0, 0);
        assertEquals(1, result.size());
        assertEquals(1L, result.get(0).getId());
    }

    @Test
    void buscarSimulacoesPaginadasReturnsEmptyListIfNoSimulacoes() {
        SimulacaoRepository simulacaoRepository = mock(SimulacaoRepository.class);
        ProdutoRepository produtoRepository = mock(ProdutoRepository.class);
        EventHubProducer eventHubProducer = mock(EventHubProducer.class);

        when(simulacaoRepository.findAll()).thenReturn(List.of());

        SimulacaoService service = new SimulacaoService(produtoRepository, simulacaoRepository, eventHubProducer);

        List<Simulacao> result = service.buscarSimulacoesPaginadas(1, 10);
        assertTrue(result.isEmpty());
    }

    @Test
    void buscarTodosProdutosReturnsAllProducts() {
        ProdutoRepository produtoRepository = mock(ProdutoRepository.class);
        SimulacaoRepository simulacaoRepository = mock(SimulacaoRepository.class);
        EventHubProducer eventHubProducer = mock(EventHubProducer.class);

        Produto produto1 = Produto.builder().id(1).nome("Produto 1").build();
        Produto produto2 = Produto.builder().id(2).nome("Produto 2").build();

        when(produtoRepository.findAll()).thenReturn(List.of(produto1, produto2));

        SimulacaoService service = new SimulacaoService(produtoRepository, simulacaoRepository, eventHubProducer);

        List<Produto> produtos = service.buscarTodosProdutos();

        assertEquals(2, produtos.size());
        assertEquals(produto1, produtos.get(0));
        assertEquals(produto2, produtos.get(1));
        verify(produtoRepository, times(1)).findAll();
    }

    @Test
    void getTelemetriaReturnsCorrectMetrics() {
        MeterRegistry meterRegistry = mock(MeterRegistry.class);
        Timer timer = mock(Timer.class);
        io.micrometer.core.instrument.search.Search search = mock(io.micrometer.core.instrument.search.Search.class);

        // Chain: meterRegistry.find(...).tag(...).timer()
        when(meterRegistry.find("http.server.requests")).thenReturn(search);
        when(search.tag("uri", "/simulacoes")).thenReturn(search);
        when(search.timer()).thenReturn(timer);

        when(timer.count()).thenReturn(10L);
        when(timer.mean(TimeUnit.MILLISECONDS)).thenReturn(100.0);
        when(timer.max(TimeUnit.MILLISECONDS)).thenReturn(200.0);
        when(timer.percentile(0.01, TimeUnit.MILLISECONDS)).thenReturn(50.0);

        // For success rate calculation
        when(search.meters()).thenReturn(Collections.singletonList(timer));
        io.micrometer.core.instrument.Meter.Id meterId = mock(io.micrometer.core.instrument.Meter.Id.class);
        when(timer.getId()).thenReturn(meterId);
        when(meterId.getTag("status")).thenReturn("200");

        SimulacaoService service = new SimulacaoService(null, null, null);

        TelemetriaResponseDTO response = service.getTelemetria(meterRegistry);

        assertNotNull(response);
        assertEquals(1, response.getListaEndpoints().size());
        EndpointTelemetriaDTO endpoint = response.getListaEndpoints().get(0);
        assertEquals("API Simulador", endpoint.getNomeApi());
        assertEquals(10, endpoint.getQtdRequisicoes());
        assertEquals(100, endpoint.getTempoMedio());
        assertEquals(50, endpoint.getTempoMinimo());
        assertEquals(200, endpoint.getTempoMaximo());
        assertTrue(endpoint.getPercentualSucesso() > 0.0);
    }

    @Test
    void getTelemetriaHandlesNoTimer() {
        MeterRegistry meterRegistry = mock(MeterRegistry.class);
        io.micrometer.core.instrument.search.Search search = mock(io.micrometer.core.instrument.search.Search.class);

        when(meterRegistry.find("http.server.requests")).thenReturn(search);
        when(search.tag("uri", "/simulacoes")).thenReturn(search);
        when(search.timer()).thenReturn(null);
        when(search.meters()).thenReturn(Collections.emptyList());

        SimulacaoService service = new SimulacaoService(null, null, null);

        TelemetriaResponseDTO response = service.getTelemetria(meterRegistry);

        assertNotNull(response);
        assertEquals(1, response.getListaEndpoints().size());
        EndpointTelemetriaDTO endpoint = response.getListaEndpoints().get(0);
        assertEquals(0, endpoint.getQtdRequisicoes());
        assertEquals(0, endpoint.getTempoMedio());
        assertEquals(0, endpoint.getTempoMinimo());
        assertEquals(0, endpoint.getTempoMaximo());
        assertEquals(0.0, endpoint.getPercentualSucesso());
    }
}
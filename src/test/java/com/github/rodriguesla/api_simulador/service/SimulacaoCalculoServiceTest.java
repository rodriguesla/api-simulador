package com.github.rodriguesla.api_simulador.service;

import com.github.rodriguesla.api_simulador.dto.ParcelaDTO;
import com.github.rodriguesla.api_simulador.dto.SimulacaoRequestDTO;
import com.github.rodriguesla.api_simulador.dto.SimulacaoResponseDTO;
import com.github.rodriguesla.api_simulador.entities.produto.model.Produto;
import com.github.rodriguesla.api_simulador.entities.produto.repository.ProdutoRepository;
import com.github.rodriguesla.api_simulador.entities.simulacao.model.Simulacao;
import com.github.rodriguesla.api_simulador.entities.simulacao.repository.SimulacaoRepository;
import com.github.rodriguesla.api_simulador.exception.RequisicaoInvalidaException;
import com.github.rodriguesla.api_simulador.exception.ValorOuPrazoDesejadoInvalidoException;
import com.github.rodriguesla.api_simulador.producer.EventHubProducer;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class SimulacaoCalculoServiceTest {

    private static final BigDecimal TEST_VALOR = new BigDecimal("1000.00");
    private static final BigDecimal TEST_TAXA = new BigDecimal("0.01");
    private static final BigDecimal PRODUTO_VALOR_MINIMO = new BigDecimal("1000");
    private static final BigDecimal PRODUTO_VALOR_MAXIMO = new BigDecimal("10000");

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
                .valorMinimo(PRODUTO_VALOR_MINIMO)
                .valorMaximo(PRODUTO_VALOR_MAXIMO)
                .build();

        when(produtoRepository.findAll()).thenReturn(Collections.singletonList(produto));

        SimulacaoCalculoService service = new SimulacaoCalculoService(produtoRepository, simulacaoRepository, eventHubProducer);

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

        SimulacaoCalculoService service = new SimulacaoCalculoService(produtoRepository, simulacaoRepository, eventHubProducer);

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

        SimulacaoCalculoService service = new SimulacaoCalculoService(produtoRepository, simulacaoRepository, eventHubProducer);

        SimulacaoRequestDTO request = new SimulacaoRequestDTO();
        request.setValorDesejado(5000.0);
        request.setPrazo(0);

        assertThrows(ValorOuPrazoDesejadoInvalidoException.class, () -> service.simular(request));
    }

    @Test
    void simularNoMatchingProductThrowsException() {
        ProdutoRepository produtoRepository = mock(ProdutoRepository.class);
        SimulacaoRepository simulacaoRepository = mock(SimulacaoRepository.class);
        EventHubProducer eventHubProducer = mock(EventHubProducer.class);

        Produto produto = Produto.builder()
                .id(1)
                .nome("Produto Teste")
                .taxaJuros(TEST_TAXA)
                .prazoMinimo((short) 1)
                .prazoMaximo((short) 12)
                .valorMinimo(PRODUTO_VALOR_MINIMO)
                .valorMaximo(PRODUTO_VALOR_MAXIMO)
                .build();
        // Only one product, but request will be out of range
        when(produtoRepository.findAll()).thenReturn(Collections.singletonList(produto));

        SimulacaoCalculoService service = new SimulacaoCalculoService(produtoRepository, simulacaoRepository, eventHubProducer);

        SimulacaoRequestDTO request = new SimulacaoRequestDTO();
        request.setValorDesejado(999.0); // less than valorMinimo
        request.setPrazo(13); // greater than prazoMaximo

        assertThrows(ValorOuPrazoDesejadoInvalidoException.class, () -> service.simular(request));
    }

    private List<ParcelaDTO> invokeCalcularSacParcelas(SimulacaoCalculoService service, BigDecimal valor, BigDecimal taxa, short prazo) throws Exception {
        var method = SimulacaoCalculoService.class.getDeclaredMethod("calcularSacParcelas", BigDecimal.class, BigDecimal.class, Short.class);
        method.setAccessible(true);
        @SuppressWarnings("unchecked")
        List<ParcelaDTO> parcelas = (List<ParcelaDTO>) method.invoke(service, valor, taxa, prazo);
        return parcelas;
    }

    @Test
    void calcularSacParcelasReturnsCorrectParcelas() throws Exception {
        SimulacaoCalculoService service = new SimulacaoCalculoService(null, null, null);
        short prazo = 2;

        List<ParcelaDTO> parcelas = invokeCalcularSacParcelas(service, TEST_VALOR, TEST_TAXA, prazo);

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
        SimulacaoCalculoService service = new SimulacaoCalculoService(null, null, null);
        short prazo = 0;

        assertThrows(ValorOuPrazoDesejadoInvalidoException.class, () -> {
            try {
                invokeCalcularSacParcelas(service, TEST_VALOR, TEST_TAXA, prazo);
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
        SimulacaoCalculoService service = new SimulacaoCalculoService(null, null, null);
        short prazo = -1;

        assertThrows(ValorOuPrazoDesejadoInvalidoException.class, () -> {
            try {
                invokeCalcularSacParcelas(service, TEST_VALOR, TEST_TAXA, prazo);
            } catch (Exception e) {
                Throwable cause = e.getCause();
                if (cause instanceof ValorOuPrazoDesejadoInvalidoException) {
                    throw (ValorOuPrazoDesejadoInvalidoException) cause;
                }
                throw new RuntimeException(e);
            }
        });
    }

    private List<ParcelaDTO> invokeCalcularPriceParcelas(SimulacaoCalculoService service, BigDecimal valor, BigDecimal taxa, short prazo) throws Exception {
        var method = SimulacaoCalculoService.class.getDeclaredMethod("calcularPriceParcelas", BigDecimal.class, BigDecimal.class, Short.class);
        method.setAccessible(true);
        return (List<ParcelaDTO>) method.invoke(service, valor, taxa, prazo);
    }

    @Test
    void calcularPriceParcelasReturnsCorrectParcelas() throws Exception {
        SimulacaoCalculoService service = new SimulacaoCalculoService(null, null, null);
        short prazo = 2;

        List<ParcelaDTO> parcelas = invokeCalcularPriceParcelas(service, TEST_VALOR, TEST_TAXA, prazo);

        assertEquals(2, parcelas.size());
        assertEquals(1, parcelas.get(0).getNumero());
        assertEquals(2, parcelas.get(1).getNumero());
    }

    @Test
    void calcularPriceParcelasZeroPrazoThrowsException() {
        SimulacaoCalculoService service = new SimulacaoCalculoService(null, null, null);
        short prazo = 0;

        assertThrows(ValorOuPrazoDesejadoInvalidoException.class, () -> {
            try {
                invokeCalcularPriceParcelas(service, TEST_VALOR, TEST_TAXA, prazo);
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
        SimulacaoCalculoService service = new SimulacaoCalculoService(null, null, null);
        short prazo = -1;

        assertThrows(ValorOuPrazoDesejadoInvalidoException.class, () -> {
            try {
                invokeCalcularPriceParcelas(service, TEST_VALOR, TEST_TAXA, prazo);
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
    void getMinElapsedTimeIsUpdated() {
        ProdutoRepository produtoRepository = mock(ProdutoRepository.class);
        SimulacaoRepository simulacaoRepository = mock(SimulacaoRepository.class);
        EventHubProducer eventHubProducer = mock(EventHubProducer.class);

        Produto produto = Produto.builder()
                .id(1)
                .nome("Produto Teste")
                .taxaJuros(TEST_TAXA)
                .prazoMinimo((short) 1)
                .prazoMaximo((short) 12)
                .valorMinimo(PRODUTO_VALOR_MINIMO)
                .valorMaximo(PRODUTO_VALOR_MAXIMO)
                .build();
        when(produtoRepository.findAll()).thenReturn(Collections.singletonList(produto));

        SimulacaoCalculoService service = new SimulacaoCalculoService(produtoRepository, simulacaoRepository, eventHubProducer);

        assertEquals(Long.MAX_VALUE, service.getMinElapsedTime());

        SimulacaoRequestDTO request = new SimulacaoRequestDTO();
        request.setValorDesejado(5000.0);
        request.setPrazo(12);
        service.simular(request);

        long minElapsed = service.getMinElapsedTime();

        assertTrue(minElapsed < Long.MAX_VALUE && minElapsed >= 0);
    }
}
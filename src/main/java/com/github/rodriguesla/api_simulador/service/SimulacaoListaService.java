package com.github.rodriguesla.api_simulador.service;

import com.github.rodriguesla.api_simulador.dto.SimulacaoListaResponseDTO;
import com.github.rodriguesla.api_simulador.dto.SimulacaoRegistroDTO;
import com.github.rodriguesla.api_simulador.entities.simulacao.model.Simulacao;
import com.github.rodriguesla.api_simulador.entities.simulacao.repository.SimulacaoRepository;
import com.github.rodriguesla.api_simulador.mapper.SimulacaoMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class SimulacaoListaService {
    private static final Logger logger = LoggerFactory.getLogger(SimulacaoListaService.class);
    private final SimulacaoRepository simulacaoRepository;

    public SimulacaoListaService(SimulacaoRepository simulacaoRepository) {
        this.simulacaoRepository = simulacaoRepository;
    }

    public SimulacaoListaResponseDTO buscarSimulacoesLista(int pagina, int qtdRegistrosPagina) {
        List<Simulacao> simulacoes = buscarSimulacoesPaginadas(pagina, qtdRegistrosPagina);
        if (simulacoes.isEmpty()) {
            throw new com.github.rodriguesla.api_simulador.exception.SimulacaoNaoEncontradaException("Nenhuma simulação encontrada.");
        }
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

    public List<Simulacao> buscarSimulacoesPaginadas(int pagina, int qtdRegistrosPagina) {
        logger.info("Listando simulações: página={}, quantidade por página={}", pagina, qtdRegistrosPagina);

        if (pagina < 0) pagina = 0;
        if (qtdRegistrosPagina < 1) qtdRegistrosPagina = 20;

        int offset = pagina * qtdRegistrosPagina;
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
}
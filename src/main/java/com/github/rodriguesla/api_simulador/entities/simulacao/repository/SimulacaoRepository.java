package com.github.rodriguesla.api_simulador.entities.simulacao.repository;

import com.github.rodriguesla.api_simulador.entities.simulacao.model.Simulacao;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface SimulacaoRepository extends JpaRepository<Simulacao, Long> {
    List<Simulacao> findByDataSimulacaoBetween(LocalDateTime start, LocalDateTime end);
}


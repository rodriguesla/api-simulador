package com.github.rodriguesla.api_simulador.entities.simulacao.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Simulacao {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Integer produtoId;
    private Double valorDesejado;
    private Integer prazo;
    private Double resultadoSac;
    private Double resultadoPrice;
    private LocalDateTime dataSimulacao;
    private String envelopeJson;
}
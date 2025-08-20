package com.github.rodriguesla.api_simulador.entities.simulacao.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

import java.time.LocalDateTime;

@Entity
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

    public Simulacao() {
    }

    public Simulacao(Long id, Integer produtoId, Double valorDesejado, Integer prazo, Double resultadoSac, Double resultadoPrice, LocalDateTime dataSimulacao, String envelopeJson) {
        this.id = id;
        this.produtoId = produtoId;
        this.valorDesejado = valorDesejado;
        this.prazo = prazo;
        this.resultadoSac = resultadoSac;
        this.resultadoPrice = resultadoPrice;
        this.dataSimulacao = dataSimulacao;
        this.envelopeJson = envelopeJson;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Integer getProdutoId() {
        return produtoId;
    }

    public void setProdutoId(Integer produtoId) {
        this.produtoId = produtoId;
    }

    public Double getValorDesejado() {
        return valorDesejado;
    }

    public void setValorDesejado(Double valorDesejado) {
        this.valorDesejado = valorDesejado;
    }

    public Integer getPrazo() {
        return prazo;
    }

    public void setPrazo(Integer prazo) {
        this.prazo = prazo;
    }

    public Double getResultadoSac() {
        return resultadoSac;
    }

    public void setResultadoSac(Double resultadoSac) {
        this.resultadoSac = resultadoSac;
    }

    public Double getResultadoPrice() {
        return resultadoPrice;
    }

    public void setResultadoPrice(Double resultadoPrice) {
        this.resultadoPrice = resultadoPrice;
    }

    public LocalDateTime getDataSimulacao() {
        return dataSimulacao;
    }

    public void setDataSimulacao(LocalDateTime dataSimulacao) {
        this.dataSimulacao = dataSimulacao;
    }

    public String getEnvelopeJson() {
        return envelopeJson;
    }

    public void setEnvelopeJson(String envelopeJson) {
        this.envelopeJson = envelopeJson;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private Long id;
        private Integer produtoId;
        private Double valorDesejado;
        private Integer prazo;
        private Double resultadoSac;
        private Double resultadoPrice;
        private LocalDateTime dataSimulacao;
        private String envelopeJson;

        public Builder id(Long id) {
            this.id = id;
            return this;
        }

        public Builder produtoId(Integer produtoId) {
            this.produtoId = produtoId;
            return this;
        }

        public Builder valorDesejado(Double valorDesejado) {
            this.valorDesejado = valorDesejado;
            return this;
        }

        public Builder prazo(Integer prazo) {
            this.prazo = prazo;
            return this;
        }

        public Builder resultadoSac(Double resultadoSac) {
            this.resultadoSac = resultadoSac;
            return this;
        }

        public Builder resultadoPrice(Double resultadoPrice) {
            this.resultadoPrice = resultadoPrice;
            return this;
        }

        public Builder dataSimulacao(LocalDateTime dataSimulacao) {
            this.dataSimulacao = dataSimulacao;
            return this;
        }

        public Builder envelopeJson(String envelopeJson) {
            this.envelopeJson = envelopeJson;
            return this;
        }

        public Simulacao build() {
            return new Simulacao(id, produtoId, valorDesejado, prazo, resultadoSac, resultadoPrice, dataSimulacao, envelopeJson);
        }
    }
}
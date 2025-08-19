package com.github.rodriguesla.api_simulador.model;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
public class Simulacao {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "produto_id")
    private Produto produto;

    private Double valorDesejado;
    private Integer prazo;
    private Double resultadoSac;
    private Double resultadoPrice;
    private LocalDateTime dataSimulacao;
    private String envelopeJson;

    public Simulacao() {}

    public Simulacao(Long id, Produto produto, Double valorDesejado, Integer prazo, Double resultadoSac, Double resultadoPrice, LocalDateTime dataSimulacao, String envelopeJson) {
        this.id = id;
        this.produto = produto;
        this.valorDesejado = valorDesejado;
        this.prazo = prazo;
        this.resultadoSac = resultadoSac;
        this.resultadoPrice = resultadoPrice;
        this.dataSimulacao = dataSimulacao;
        this.envelopeJson = envelopeJson;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Produto getProduto() { return produto; }
    public void setProduto(Produto produto) { this.produto = produto; }
    public Double getValorDesejado() { return valorDesejado; }
    public void setValorDesejado(Double valorDesejado) { this.valorDesejado = valorDesejado; }
    public Integer getPrazo() { return prazo; }
    public void setPrazo(Integer prazo) { this.prazo = prazo; }
    public Double getResultadoSac() { return resultadoSac; }
    public void setResultadoSac(Double resultadoSac) { this.resultadoSac = resultadoSac; }
    public Double getResultadoPrice() { return resultadoPrice; }
    public void setResultadoPrice(Double resultadoPrice) { this.resultadoPrice = resultadoPrice; }
    public LocalDateTime getDataSimulacao() { return dataSimulacao; }
    public void setDataSimulacao(LocalDateTime dataSimulacao) { this.dataSimulacao = dataSimulacao; }
    public String getEnvelopeJson() { return envelopeJson; }
    public void setEnvelopeJson(String envelopeJson) { this.envelopeJson = envelopeJson; }

    public static Builder builder() { return new Builder(); }
    public static class Builder {
        private Long id;
        private Produto produto;
        private Double valorDesejado;
        private Integer prazo;
        private Double resultadoSac;
        private Double resultadoPrice;
        private LocalDateTime dataSimulacao;
        private String envelopeJson;

        public Builder id(Long id) { this.id = id; return this; }
        public Builder produto(Produto produto) { this.produto = produto; return this; }
        public Builder valorDesejado(Double valorDesejado) { this.valorDesejado = valorDesejado; return this; }
        public Builder prazo(Integer prazo) { this.prazo = prazo; return this; }
        public Builder resultadoSac(Double resultadoSac) { this.resultadoSac = resultadoSac; return this; }
        public Builder resultadoPrice(Double resultadoPrice) { this.resultadoPrice = resultadoPrice; return this; }
        public Builder dataSimulacao(LocalDateTime dataSimulacao) { this.dataSimulacao = dataSimulacao; return this; }
        public Builder envelopeJson(String envelopeJson) { this.envelopeJson = envelopeJson; return this; }
        public Simulacao build() {
            return new Simulacao(id, produto, valorDesejado, prazo, resultadoSac, resultadoPrice, dataSimulacao, envelopeJson);
        }
    }
}
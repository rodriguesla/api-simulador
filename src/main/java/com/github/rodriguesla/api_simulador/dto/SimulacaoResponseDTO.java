package com.github.rodriguesla.api_simulador.dto;

public class SimulacaoResponseDTO {
    private String nomeProduto;
    private Double valorSolicitado;
    private Integer prazoMeses;
    private Double resultadoSac;
    private Double resultadoPrice;

    public SimulacaoResponseDTO() {}
    public SimulacaoResponseDTO(String nomeProduto, Double valorSolicitado, Integer prazoMeses, Double resultadoSac, Double resultadoPrice) {
        this.nomeProduto = nomeProduto;
        this.valorSolicitado = valorSolicitado;
        this.prazoMeses = prazoMeses;
        this.resultadoSac = resultadoSac;
        this.resultadoPrice = resultadoPrice;
    }
    public String getNomeProduto() { return nomeProduto; }
    public void setNomeProduto(String nomeProduto) { this.nomeProduto = nomeProduto; }
    public Double getValorSolicitado() { return valorSolicitado; }
    public void setValorSolicitado(Double valorSolicitado) { this.valorSolicitado = valorSolicitado; }
    public Integer getPrazoMeses() { return prazoMeses; }
    public void setPrazoMeses(Integer prazoMeses) { this.prazoMeses = prazoMeses; }
    public Double getResultadoSac() { return resultadoSac; }
    public void setResultadoSac(Double resultadoSac) { this.resultadoSac = resultadoSac; }
    public Double getResultadoPrice() { return resultadoPrice; }
    public void setResultadoPrice(Double resultadoPrice) { this.resultadoPrice = resultadoPrice; }

    public static Builder builder() { return new Builder(); }
    public static class Builder {
        private String nomeProduto;
        private Double valorSolicitado;
        private Integer prazoMeses;
        private Double resultadoSac;
        private Double resultadoPrice;
        public Builder nomeProduto(String nomeProduto) { this.nomeProduto = nomeProduto; return this; }
        public Builder valorSolicitado(Double valorSolicitado) { this.valorSolicitado = valorSolicitado; return this; }
        public Builder prazoMeses(Integer prazoMeses) { this.prazoMeses = prazoMeses; return this; }
        public Builder resultadoSac(Double resultadoSac) { this.resultadoSac = resultadoSac; return this; }
        public Builder resultadoPrice(Double resultadoPrice) { this.resultadoPrice = resultadoPrice; return this; }
        public SimulacaoResponseDTO build() { return new SimulacaoResponseDTO(nomeProduto, valorSolicitado, prazoMeses, resultadoSac, resultadoPrice); }
    }
}

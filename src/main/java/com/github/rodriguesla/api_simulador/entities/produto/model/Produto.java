package com.github.rodriguesla.api_simulador.entities.produto.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "PRODUTO", schema = "dbo")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Produto {
    @Id
    @Column(name = "CO_PRODUTO")
    private Integer id;

    @Column(name = "NO_PRODUTO", nullable = false)
    private String nome;

    @Column(name = "PC_TAXA_JUROS", nullable = false, precision = 10, scale = 9)
    private java.math.BigDecimal taxaJuros;

    @Column(name = "NU_MINIMO_MESES", nullable = false)
    private Short prazoMinimo;

    @Column(name = "NU_MAXIMO_MESES")
    private Short prazoMaximo;

    @Column(name = "VR_MINIMO", nullable = false, precision = 18, scale = 2)
    private java.math.BigDecimal valorMinimo;

    @Column(name = "VR_MAXIMO", precision = 18, scale = 2)
    private java.math.BigDecimal valorMaximo;
}

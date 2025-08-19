package com.github.rodriguesla.api_simulador.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "PRODUTO")
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

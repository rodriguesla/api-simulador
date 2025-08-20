package com.github.rodriguesla.api_simulador.produto.repository;

import com.github.rodriguesla.api_simulador.produto.model.Produto;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProdutoRepository extends JpaRepository<Produto, Integer> {
}

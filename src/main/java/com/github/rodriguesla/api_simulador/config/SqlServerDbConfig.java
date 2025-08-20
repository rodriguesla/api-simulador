package com.github.rodriguesla.api_simulador.config;

import jakarta.persistence.EntityManagerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.boot.orm.jpa.EntityManagerFactoryBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.transaction.PlatformTransactionManager;

import javax.sql.DataSource;

@Configuration
@EnableJpaRepositories(
        basePackages = "com.github.rodriguesla.api_simulador.produto.repository",
        entityManagerFactoryRef = "produtoEntityManager",
        transactionManagerRef = "produtoTransactionManager"
)
public class SqlServerDbConfig {
    @Bean
    @Primary
    @ConfigurationProperties("spring.datasource")
    public DataSource produtoDataSource() {
        return DataSourceBuilder.create().build();
    }

    @Bean
    @Primary
    public LocalContainerEntityManagerFactoryBean produtoEntityManager(
            EntityManagerFactoryBuilder builder) {
        return builder
                .dataSource(produtoDataSource())
                .packages("com.github.rodriguesla.api_simulador.produto.model")
                .persistenceUnit("produto")
                .build();
    }

    @Bean
    @Primary
    public PlatformTransactionManager produtoTransactionManager(
            @Qualifier("produtoEntityManager") EntityManagerFactory emf) {
        return new JpaTransactionManager(emf);
    }
}

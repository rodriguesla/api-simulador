package com.github.rodriguesla.api_simulador.config;

import jakarta.persistence.EntityManagerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.boot.orm.jpa.EntityManagerFactoryBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.transaction.PlatformTransactionManager;

import javax.sql.DataSource;

@Configuration
@EnableJpaRepositories(
        basePackages = {
                "com.github.rodriguesla.api_simulador.entities.simulacao.repository",
                "com.github.rodriguesla.api_simulador.entities.auditoria.repository"
        },
        entityManagerFactoryRef = "simulacaoEntityManager",
        transactionManagerRef = "simulacaoTransactionManager"
)
public class H2DbConfig {
    @Bean
    @ConfigurationProperties("spring.h2.datasource")
    public DataSource simulacaoDataSource() {
        return DataSourceBuilder.create().build();
    }

    @Bean
    public LocalContainerEntityManagerFactoryBean simulacaoEntityManager(
            EntityManagerFactoryBuilder builder) {
        return builder
                .dataSource(simulacaoDataSource())
                .packages(
                    "com.github.rodriguesla.api_simulador.entities.simulacao.model",
                    "com.github.rodriguesla.api_simulador.entities.auditoria.model"
                )
                .persistenceUnit("simulacao")
                .build();
    }

    @Bean
    public PlatformTransactionManager simulacaoTransactionManager(
            @Qualifier("simulacaoEntityManager") EntityManagerFactory emf) {
        return new JpaTransactionManager(emf);
    }
}
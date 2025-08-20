package com.github.rodriguesla.api_simulador.config;

import jakarta.persistence.EntityManagerFactory;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.transaction.PlatformTransactionManager;

import javax.sql.DataSource;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class H2DbConfigTest {

    @Autowired
    private ApplicationContext context;

    @Test
    void simulacaoDataSourceBeanExists() {
        DataSource ds = context.getBean("simulacaoDataSource", DataSource.class);
        assertThat(ds).isNotNull();
    }

    @Test
    void simulacaoEntityManagerBeanExists() {
        EntityManagerFactory emf =
                context.getBean("simulacaoEntityManager", EntityManagerFactory.class);
        assertThat(emf).isNotNull();
    }

    @Test
    void simulacaoTransactionManagerBeanExists() {
        PlatformTransactionManager tm =
                context.getBean("simulacaoTransactionManager", PlatformTransactionManager.class);
        assertThat(tm).isNotNull();
    }
}
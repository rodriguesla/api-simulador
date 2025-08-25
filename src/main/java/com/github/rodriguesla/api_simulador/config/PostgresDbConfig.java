//package com.github.rodriguesla.api_simulador.config;
//
//import jakarta.persistence.EntityManagerFactory;
//import org.springframework.beans.factory.annotation.Qualifier;
//import org.springframework.boot.context.properties.ConfigurationProperties;
//import org.springframework.boot.jdbc.DataSourceBuilder;
//import org.springframework.boot.orm.jpa.EntityManagerFactoryBuilder;
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//import org.springframework.context.annotation.Profile;
//import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
//import org.springframework.orm.jpa.JpaTransactionManager;
//import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
//import org.springframework.transaction.PlatformTransactionManager;
//
//import javax.sql.DataSource;
//
//@Configuration
//@Profile("prod")
//@EnableJpaRepositories(
//    basePackages = "com.github.rodriguesla.api_simulador.entities.simulacao.repository",
//    entityManagerFactoryRef = "simulacaoEntityManager",
//    transactionManagerRef = "simulacaoTransactionManager"
//)
//public class PostgresDbConfig {
//    @Bean(name = "simulacaoDataSource")
//    @ConfigurationProperties("spring.simulacao.datasource")
//    public DataSource simulacaoDataSource() {
//        return DataSourceBuilder.create().build();
//    }
//
//    @Bean(name = "simulacaoEntityManager")
//    public LocalContainerEntityManagerFactoryBean simulacaoEntityManager(
//            EntityManagerFactoryBuilder builder) {
//        return builder
//                .dataSource(simulacaoDataSource())
//                .packages("com.github.rodriguesla.api_simulador.entities.simulacao.model")
//                .persistenceUnit("simulacao")
//                .properties(java.util.Map.of(
//                    "hibernate.dialect", "org.hibernate.dialect.PostgreSQLDialect",
//                    "hibernate.hbm2ddl.auto", "update"
//                ))
//                .build();
//    }
//
//    @Bean(name = "simulacaoTransactionManager")
//    public PlatformTransactionManager simulacaoTransactionManager(
//            @Qualifier("simulacaoEntityManager") EntityManagerFactory emf) {
//        return new JpaTransactionManager(emf);
//    }
//}

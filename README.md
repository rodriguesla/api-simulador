# API Simulador de Crédito

## Descrição
Este projeto é uma API desenvolvida em Spring Boot para simulação de crédito bancário, conforme desafio técnico. Permite que qualquer pessoa ou sistema realize simulações de empréstimo, retornando condições de negociação e cálculos dos sistemas de amortização SAC e PRICE.

## Funcionalidades
- Receber solicitações de simulação via JSON
- Consultar produtos parametrizados em banco SQL Server
- Validar dados de entrada conforme parâmetros dos produtos
- Filtrar produtos adequados à solicitação
- Calcular parcelas nos sistemas SAC e PRICE
- Persistir simulações realizadas em banco local
- Enviar eventos de simulação para o EventHub
- Endpoints para consulta de simulações, valores por produto/dia e telemetria

## Arquitetura
```
[Usuário/Postman] --> [API Simulador] --> [Banco SQL Server]
                                 |--> [EventHub]
                                 |--> [Banco Local]
```
- API RESTful documentada via Swagger
- Integração com SQL Server para consulta de produtos
- Persistência de simulações em banco local
- Envio de eventos para EventHub
- Métricas expostas para Prometheus

## Como rodar o projeto
1. **Pré-requisitos:**
   - Docker e Docker Compose instalados
   - JDK 21+
   - Maven
2. **Configuração:**
   - Variáveis de ambiente no já estipuladas no `application.properties` para acesso ao SQL Server e EventHub
3. **Execução via Docker Compose:**
   ```bash
   docker-compose up --build
   ```
   - A API estará disponível em `http://localhost:8080`
4. **Execução local:**
   ```bash
   mvn spring-boot:run
   ```
5. **Endpoint Público:**
   A API está disponível publicamente em:

- http://18.230.17.128/:8080

   Qualquer pessoa pode acessar este endpoint, desde que as regras de segurança estejam configuradas corretamente na instância EC2.

## Endpoints da API

- **POST /simulacoes**: Realiza uma simulação de crédito
- **GET /simulacoes**: Lista todas as simulações realizadas
- **GET /simulacoes/por-dia?data=**: Retorna valores simulados por produto em cada dia
- **GET /simulacoes/telemetria**: Retorna dados de telemetria

## Testes
- **Testes unitários:**
  ```bash
  mvn test
  ```
- **Testes de integração:**
  Incluídos no projeto, simulando banco de dados e integrações
- **Cobertura de testes:**
  ```bash
  mvn clean test jacoco:report
  ```
  Relatório gerado em `/target/site/jacoco/index.html`

## Documentação da API
- Swagger disponível em: `http://localhost:8080/swagger-ui.html`
- Exemplos de requisições e respostas documentados
- Erros tratados: 400, 401, 403, 404, 500

## Coleção Postman
- Coleção pronta disponível em `/docs/Hackaton 2025.postman_collection.json`

## Métricas e Telemetria
- Métricas expostas para Prometheus em `/actuator/prometheus`
- Endpoint de telemetria: `/simulacoes/telemetria`

## Pipeline CI/CD
- Pipeline configurado no GitHub Actions para build e testes automáticos
- Link: [Pipeline no GitHub](https://github.com/rodriguesla/api-simulador/actions)

## Diagrama de Arquitetura
![Diagrama de Arquitetura](docs/diagrama-arquitetura.png)

## Observações
- Estratégias de cache, circuit break e rate limit podem ser implementadas conforme necessidade
- Auditoria de operações disponível
- Resultados de teste de carga com K6/JMeter disponíveis em `/docs/tests`

## Contato
- Desenvolvedor: [Luciano Aymone Rodrigues]
- Email: luciano.a.rodrigues@caixa.gov.br

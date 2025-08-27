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
                                 |--> [Banco Local H2]
```
- API RESTful documentada via Swagger
- Integração com SQL Server para consulta de produtos
- Persistência de simulações em banco local
- Envio de eventos para EventHub
- Métricas expostas para Prometheus

## Auditoria de Requisições
O sistema possui funcionalidade de auditoria automática para todas requisições relevantes feitas à API. As informações registradas incluem:
- Usuário (não foi implementado sistema de autenticação, somente uma coluna para demonstração)
- Endpoint acessado
- Dados da requisição (para POST/PUT)
- Status da resposta
- Endereço IP
- Mensagem de erro (caso ocorra)
- Data/hora

A auditoria pode ser visualizada acessando a página `/auditoria-view`.

**Observações:**
- Requisições a recursos estáticos (CSS, JS, imagens) e à própria página de auditoria não são registradas.
- Caso não existam registros de auditoria, será exibida a página de erro.
- O log de auditoria pode impactar a performance em cenários de alto volume. Para otimizações, consulte a documentação ou desabilite conforme necessário.

## Como rodar o projeto
1. **Pré-requisitos:**
    - Docker e Docker Compose instalados
    - JDK 21+
    - Maven
2. **Configuração:**
    - Variáveis de ambiente já estipuladas no `application.properties` para acesso ao SQL Server e EventHub
3. **Execução via Docker Compose (recomendado):**
   O Dockerfile está configurado para construir o JAR automaticamente usando multi-stage build. Não é necessário rodar scripts ou criar o package manualmente.
   ```bash
   docker compose up --build
   ```
   - A API estará disponível em `http://localhost:8080`
4. **Execução local:**
   ```bash
   mvn spring-boot:run
   ```
5. **Execução manual do build (opcional):**
   Caso queira criar o JAR manualmente:
   ```bash
   mvn clean package
   docker build -t api-simulador .
   docker run -p 8080:8080 api-simulador
   ```
6. **Endpoint Público:**
   A API está disponível publicamente em uma instância EC2 da AWS, configurada para acesso externo via IP público. O ambiente foi provisionado e configurado para garantir segurança e disponibilidade.

- http://18.229.125.164:8080

  > **Importante:** O endereço IP público da API está configurado como variável no arquivo de ambiente do Postman (`/docs/HACK.postman_environment.json`), sob o nome `{{ec2-user}}`. Ao utilizar a coleção no Postman, todas as requisições usam `{{ec2-user}}` como base do endpoint. Para alterar o IP, basta modificar o valor da variável no ambiente, facilitando testes automatizados e integração sem necessidade de editar cada requisição manualmente.

  Qualquer pessoa pode acessar este endpoint, desde que as regras de segurança estejam configuradas corretamente na instância AWS/EC2. O endpoint público está incluído na coleção Postman disponível no projeto (`/docs/Hackaton2025.postman_collection.json`), facilitando testes automatizados e integração.

## Segurança e Autenticação JWT
A partir desta versão, todos os endpoints protegidos exigem autenticação via token JWT. Para acessar os recursos, siga o fluxo abaixo:

- **Gerar Token de Acesso:**
  - Endpoint: `POST /auth/token`
  - Corpo da requisição (JSON):
    ```json
    {
      "usuario": "hackathon",
      "password": "2025"
    }
    ```
  - Exemplo de resposta (sucesso):
    ```json
    {
      "token": "<seu_token_jwt>",
      "tipo": "Bearer",
      "usuario": "hackathon",
      "expiraEm": 3600
    }
    ```
  - Se as credenciais estiverem incorretas, será retornado HTTP 401.
  

- **Utilizar o Token nos Endpoints Protegidos:**
  - Adicione o header `Authorization` em todas as requisições:
    ```
    Authorization: Bearer <seu_token_jwt>
    ```
  - Sem o token ou com token inválido, a API retorna HTTP 403 e mensagem de erro.


- **Endpoints públicos:**
  - O endpoint `/auth/token` não exige autenticação.
  - Endpoints de documentação Swagger e métricas também são públicos.

## Endpoints da API

- **POST /auth/token**: Gera token JWT para autenticação
- **POST /simulacoes**: Realiza uma simulação de crédito
- **GET  /simulacoes**: Lista todas as simulações realizadas
- **GET  /simulacoes/por-dia?data=**: Retorna valores simulados por produto em cada dia
- **GET  /simulacoes/telemetria**: Retorna dados de telemetria

## Visualização dos Dados via Navegador (preferivelmente o Brave ou Chrome)
Além do consumo dos endpoints via API, é possível visualizar os dados diretamente no navegador utilizando templates Thymeleaf, proporcionando uma experiência visual mais clara e detalhada das respostas.

A navegação principal agora começa pela página `/home`, que apresenta um menu com botões para acessar as principais visualizações do sistema. Recomenda-se iniciar por `/home` para uma experiência mais intuitiva, podendo acessar facilmente as demais páginas de visualização.

Para uma melhor experiência visual, recomenda-se instalar uma extensão ou servidor de linguagem CSS no seu editor, garantindo que os estilos dos templates sejam renderizados corretamente durante o desenvolvimento.

Os principais endpoints de visualização são:
- `/home`: Menu principal de navegação para as visualizações
- `/simulacoes-view`: Lista de simulações realizadas
- `/simulacoes-por-dia-view`: Simulações agrupadas por dia
- `/telemetria-view`: Dados de telemetria e métricas da API

Esses endpoints utilizam templates HTML para exibir as informações de forma organizada e intuitiva, facilitando o entendimento dos dados retornados pela API. Todos os endereços estão definidos no controller de visualização (`SimulacaoViewController`).

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

- **Testes automatizados com Postman:**
  - A coleção de testes automatizados está disponível em `/docs/TestesAutomatizados.postman_collection.json`.
  - Para executar corretamente os testes, é necessário importar também o arquivo de ambiente `/docs/HACK.postman_environment.json` no Postman.
  - Recomenda-se utilizar o Runner do Postman para rodar a coleção `TestesAutomatizados` antes de realizar qualquer requisição manual por meio da coleção principal.
  - Alternativamente, é possível executar todos os testes automatizados via terminal utilizando o script `run-newman-tests.bat`.
  - Caso o Newman não esteja instalado, utilize o comando abaixo para instalar globalmente:
    ```bash
    npm install -g newman
    ```
  - Para visualizar o relatório dos testes automatizados padrão, instale:
    ```bash
    npm install -g newman-reporter-html
    ```
  - A visualização dos testes pode ser feita através do acesso na pasta `/docs/newman-report.html` após a execução do script.

### Testes de carga (K6)

- Os testes de carga foram realizados utilizando a ferramenta K6, simulando 20 usuários virtuais (VUs) durante 60 segundos, acessando todos os principais endpoints da API.
- O script de teste está disponível em `/docs/tests/k6/simulacao-test.js` e o resultado completo em `/docs/tests/simulacao-test-summary.json`.

**Resumo dos resultados:**

| Métrica                       | Valor         |
|-------------------------------|--------------|
| Usuários Virtuais (VUs)       | 20           |
| Duração do teste              | 60 segundos  |
| Total de requisições          | 3608         |
| Endpoints testados            | /simulacoes (POST, GET), /simulacoes/por-dia, /simulacoes/telemetria |
| Sucesso nas requisições       | 100%         |
| Falhas nas requisições        | 0            |
| Tempo médio de resposta       | 86 ms        |

- Todos os endpoints responderam corretamente (HTTP 200) durante o teste de carga, sem falhas ou erros.
- O resultado detalhado pode ser consultado no arquivo `/docs/tests/simulacao-test-summary.json`.

### Visualização dos resultados dos testes de carga (K6)

Após rodar o teste de carga com o K6, o resumo gerado em `/docs/tests/simulacao-test-summary.json` pode ser visualizado de forma amigável em HTML:

- Basta abrir o arquivo `/docs/tests/simulacao-test-summary.html` no navegador.
- A página carrega automaticamente o JSON mais recente e exibe as métricas e checks em tabelas organizadas.
- Sempre que o teste for executado novamente e o JSON for atualizado, a visualização HTML refletirá os novos resultados ao ser recarregada.

Essa visualização facilita a análise dos resultados dos testes de carga, tornando-os mais acessíveis e compreensíveis para todos os envolvidos no projeto.

## Documentação da API
- Swagger disponível em: `http://localhost:8080/swagger-ui.html`
- Exemplos de requisições e respostas documentados
- Erros tratados: 400, 401, 403, 404, 500

## Coleção e Variável Postman
- Coleção pronta disponível em `/docs/Hackaton2025.postman_collection.json`
- Variável de ambiente disponível em `/docs/HACK.postman_environment.json`

## Métricas e Telemetria
- Métricas expostas para Prometheus em `/actuator/prometheus`
- Endpoint de telemetria: `/simulacoes/telemetria`

## Pipeline CI/CD
- Pipeline configurado no GitHub Actions para build e testes automáticos
- Link: [Pipeline no GitHub](https://github.com/rodriguesla/api-simulador/actions)

## Diagrama de Arquitetura
![Diagrama de Arquitetura](docs/diagrama-arquitetura.png)

## Demonstração em Vídeo

Veja uma demonstração em vídeo do funcionamento completo do projeto, incluindo principais funcionalidades, navegação pelas páginas, exemplos de uso da API e visualização dos resultados.

[Assista à demonstração aqui](https://youtu.be/IzIGoH7P_WI)

## Contato
- Desenvolvedor: [Luciano Aymone Rodrigues]
- Email: luciano.a.rodrigues@caixa.gov.br



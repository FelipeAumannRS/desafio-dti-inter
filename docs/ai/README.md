# Uso de IA

Ferramenta: Claude Code (modelo Claude Opus 5.5), rodando no terminal da máquina do candidato.
Cada entrada abaixo registra o pedido feito à IA e o que ela produziu. Todo código gerado foi revisado antes do commit.

## 1. Preparação do ambiente

**Pedido:** conferir os pré-requisitos do documento da dinâmica e rodar o `mn create-app`.

**Resultado:** instalação de mise, Java 25, Maven 3.9 e Micronaut CLI 5.1; teste com Testcontainers (PostgreSQL 16 e Kafka) para validar o Docker. O comando do documento falhava em três features (`datajpa`, `junitparams`, `problemjson`); os nomes corretos são `data-jpa`, `junit-params` e `problem-json`.

## 2. README e repositório

**Pedido:** clonar o repositório, criar uma branch e escrever um README.

**Resultado:** `README.md` com stack, pré-requisitos e o comando de geração do serviço; branch `feature/piggybank`.

## 3. Contratos

**Pedido:** regras da dinâmica (contract-first, serviço de borda assíncrono, testes unitários e de integração) e o enunciado do SLP.

**Resultado:** pasta `contracts/` com o OpenAPI do Buyer e do Seller, os JSON Schemas dos três eventos Kafka e um resumo do fluxo e dos tópicos. Decisões:

- Borda assíncrona: o `POST` responde `202 Accepted` com `Location` e a oferta `OPEN`; o match é feito depois, via Kafka, e o cliente consulta pelo `GET`.
- Um único tópico para as ofertas de compra e de venda, com chave constante, para o Coordinator processar uma oferta de cada vez, na ordem.
- Preço de execução: média exata `(compra + venda) / 2`.

## 4. Divisão do time

**Pedido:** esboço técnico, em imagem, dividindo o trabalho entre as 4 pessoas da call.

**Resultado:** `docs/arquitetura/slp-divisao-time.png` (fonte: `slp-divisao-time.html`, renderizado com Edge headless). Dev 1 Buyer, Dev 2 Seller, Dev 3 matching engine do Coordinator, Dev 4 orquestração e liquidação do Coordinator, mais o combinado do time e o plano de 90 minutos.

## 5. Passo a passo e estrutura dos serviços

**Pedido:** imagem no README, um passo a passo por dev e a estrutura dos três serviços na branch base, para o time todo usar.

**Resultado:**

- `README.md` com o diagrama, a tabela de serviços e portas e os links.
- `docs/passo-a-passo/`: passos comuns, integração ponta a ponta e um guia por dev (entidade, evento, endpoints ou listener, testes unitários e de integração).
- `buyer-service/`, `seller-service/` e `transaction-coordinator/` gerados com o `mn create-app` corrigido; `application.yml` com as portas 8090, 8091 e 8092 e o Postgres e o Kafka do `docker-compose.yml`; a classe `IntegrationTest` da raiz copiada para cada serviço e o teste gerado estendendo ela. Os três compilam (`mvn test-compile`).

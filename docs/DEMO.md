# Demo do SLP (17:55)

O que roda: infraestrutura (Postgres, Kafka, Kafka UI) e o **PiggiesTransactionCoordinator** completo, com o matching engine do Fernando. As ofertas entram direto no tópico `piggies.offers.created`, no formato do contrato, fazendo o papel do Buyer e do Seller.

## Rodar (Git Bash, na pasta `Desktop/slp-demo`)

1. Ambiente limpo (sobe o compose, recria os tópicos vazios, apaga as tabelas do Coordinator):

   ```bash
   cd ~/Desktop/slp-demo
   ./demo/prepare.sh
   ```

2. Em **outro terminal**, o Coordinator. Espere aparecer `Startup completed`:

   ```bash
   cd ~/Desktop/slp-demo/transaction-coordinator
   mvn mn:run
   ```

3. De volta ao primeiro terminal, a demo:

   ```bash
   ./demo/run.sh
   ```

4. Mostrar as mensagens na Kafka UI: http://localhost:8081 → Topics.

A ordem importa: o `prepare.sh` apaga as tabelas, e o Hibernate só as recria quando o Coordinator sobe.

## O que a demo mostra

| Passo | Oferta | Resultado |
|---|---|---|
| 1 | Vendedor A, $20 | aberta |
| 2 | Comprador B, $10 | sem match: B paga menos do que A exige |
| 3 | Comprador C, $30 | **match A × C a $25**: A −1 Piggy, C +1, taxa $25 publicada |
| 4–5 | Vendedores D ($22) e E ($18) | abertas |
| 6 | Comprador F, $25 | **casa com E, a venda mais barata**, a $21.5 |

No fim o script imprime os eventos `TransactionExecuted` e `ExchangeRatePublished` e o estado no Postgres (ofertas, transações, carteiras).

## Pontos para falar

- **Contract-first:** `contracts/` tem o OpenAPI do Buyer e do Seller e os JSON Schemas dos três eventos, commitados antes de qualquer implementação.
- **Borda assíncrona:** o `POST` do Buyer e do Seller responde `202` com `Location`; o match acontece depois, via Kafka, e o cliente acompanha pelo `GET`.
- **Ordem e concorrência:** todas as ofertas usam a chave `PIGGY-USD` num tópico de 1 partição, então o Coordinator processa uma por vez e duas compras não casam com a mesma venda.
- **Liquidação numa transação:** marcar as ofertas, debitar e creditar as carteiras e gravar a transação acontecem juntos; oferta repetida pelo Kafka é ignorada.
- **Matching engine (Fernando):** melhor preço primeiro, empate pela mais antiga, feito em TDD.
- **Testes:** 18 no Coordinator, unitários e de integração com Postgres e Kafka reais (Testcontainers): `cd transaction-coordinator && mvn test`.
- **Persistência:** tabelas criadas pelo Hibernate a partir das entidades; `infra/reset-db.sh` recria depois de uma mudança de entidade e `docs/db/schema.sql` registra o schema.
- **Uso de IA:** registrado em `docs/ai/`.

## Próximos passos (o que não deu tempo)

- Buyer e Seller publicando de verdade no Kafka (estão em andamento nas branches).
- Outbox para publicar os eventos só depois do commit.
- Flyway com `hbm2ddl: validate` no lugar do `update`.
- Validação de saldo nas carteiras.

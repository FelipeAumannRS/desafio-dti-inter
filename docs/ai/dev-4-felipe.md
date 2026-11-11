# Uso de IA — Dev 4 (Felipe, orquestração do Coordinator)

Ferramenta: Claude Code (Claude Opus 5.5). Cada entrada registra o pedido e o que foi produzido; o código foi revisado antes do commit.

## 1. Kafka no Coordinator (entrada e saída)

**Pedido:** próximo passo que não dependa dos outros devs: ligar o Coordinator ao Kafka.

**Resultado:**

- `event/`: records dos três eventos, iguais aos JSON Schemas; `Topics` com os nomes e a chave `PIGGY-USD`; `CoordinatorProducer` (`@KafkaClient`) publicando `TransactionExecuted` e `ExchangeRatePublished`.
- `book/`: `BookOfferEntity` (tabela `book_offers`, preço com 4 casas porque a média pode ter 3), `BookOfferRepository` com `findBySideAndStatus`, `OrderBookService.register` (ignora oferta repetida: o Kafka entrega pelo menos uma vez) e `OfferCreatedListener` no tópico `piggies.offers.created`.
- Testes: `BookOfferEntityTest` (unitário) e `CoordinatorKafkaTest` (integração com Testcontainers): oferta publicada vai para o livro; a mesma oferta duas vezes vira um registro só; os eventos de saída saem em JSON no formato do contrato (datas ISO, `pair` PIGGY/USD). 5 testes verdes.

## 2. Liquidação

**Pedido:** terminar a parte do Coordinator que não depende de ninguém: liquidação e ligação com o engine.

**Resultado:**

- `matching/`: `Side`, `BookOffer`, `Match` e um `MatchingEngine` **provisório** (casa com a primeira contraparte compatível, preço `(compra + venda) / 2`), com a assinatura combinada no guia do Dev 3. O Fernando troca o corpo de `match()`.
- `wallet/Wallet` (débito e crédito de 1 Piggy, sem validar saldo, como na versão simplificada) e `trade/Trade`.
- `auction/AuctionService.process`: numa transação, registra a oferta (ignorando repetida), busca as ofertas abertas do outro lado, chama o engine e, havendo match, marca as duas como MATCHED, debita o vendedor, credita o comprador, grava o `Trade` e publica `TransactionExecuted` e `ExchangeRatePublished`.
- Testes: `WalletTest` (unitário) e `AuctionFlowTest` (integração): o exemplo do enunciado (A vende a 20, B compra a 10, C compra a 30) gera a transação A × C a 25, a taxa 25, carteiras A = -1 e C = +1, e B continua OPEN. 7 testes verdes.
- Ponto de atenção: os eventos saem antes do commit. Se o commit falhar depois do envio, o evento já foi publicado; a solução completa seria um outbox.

## 3. Postgres

**Pedido:** preparar o Postgres e deixar a camada de persistência pronta a partir das entidades (ORM).

**Resultado:**

- Postgres do docker-compose saudável (16.15, aceitando conexões).
- As tabelas foram criadas pelo Hibernate (`hbm2ddl: update`) a partir das `@Entity`, subindo o Coordinator e o app da raiz uma vez contra o compose: `book_offers`, `trades`, `wallets` e `sell_offers`. Os apps foram parados em seguida.
- `docs/db/schema.sql`: o schema exportado com `pg_dump --schema-only`, como registro do modelo.
- Achado: `sell_offers.price` e `executed_price` ficaram `numeric(38,2)` (padrão do Hibernate). A média pode ter 3 casas (10.015) e seria arredondada no Seller; o Coordinator usa `numeric(19,4)`. Correção sugerida ao Mike: `@Column(precision = 19, scale = 4)`.

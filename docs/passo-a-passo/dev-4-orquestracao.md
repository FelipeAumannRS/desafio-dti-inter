# Dev 4 — Orquestração do Coordinator (Kafka + liquidação)

Pasta `transaction-coordinator/` · pacote `co.inter.piggies.coordinator` · porta 8092 · branch `dev4-coordinator`
Consome `contracts/events/offer-created.schema.json` · publica `contracts/events/transaction-executed.schema.json` e `contracts/events/exchange-rate-published.schema.json`

Faça antes os passos de "Todos" em [README.md](README.md). O app `transaction-coordinator/` já existe na `main`. O Dev 3 trabalha nele também, só no pacote `matching`; você fica com o resto (entidades, Kafka, serviço).

## 1. Entidades e repositórios (10 min)

- `BookOfferEntity` (`book_offers`): `offerId` (PK), `side` (BUY/SELL), `userId`, `price`, `createdAt`, `status` (OPEN/MATCHED).
- `Wallet` (`wallets`): `userId` (PK), `piggies` (long). Métodos `debit()` e `credit()` de 1 Piggy.
- `Trade` (`trades`): `transactionId` (PK), `buyOfferId`, `sellOfferId`, `buyerId`, `sellerId`, `price`, `executedAt`.

```java
@Repository
public interface BookOfferRepository extends CrudRepository<BookOfferEntity, UUID> {
    List<BookOfferEntity> findBySideAndStatus(Side side, OfferStatus status);
}
```

Versão simplificada: não validamos saldo. Uma carteira que não existe começa em 0 e pode ficar negativa.

## 2. Eventos e producer (5 min)

Records `OfferCreated`, `TransactionExecuted` e `ExchangeRatePublished` com `@Serdeable`, iguais aos JSON Schemas.

```java
@KafkaClient
public interface CoordinatorProducer {
    @Topic("piggies.transactions.executed")
    void transactionExecuted(@KafkaKey String transactionId, TransactionExecuted event);

    @Topic("piggies.exchange-rate.published")
    void exchangeRate(@KafkaKey String key, ExchangeRatePublished event);   // key "PIGGY-USD"
}
```

## 3. Listener e liquidação (20 min)

```java
@KafkaListener(groupId = "transaction-coordinator", offsetReset = OffsetReset.EARLIEST)
public class OfferCreatedListener {
    @Topic("piggies.offers.created")
    void on(OfferCreated event) { auctionService.process(event); }
}
```

`AuctionService.process`, numa única transação (`@Transactional`):

1. Se a oferta já existe (`existsById`), ignora: o Kafka pode entregar duas vezes.
2. Salva a oferta como `OPEN`.
3. Busca as ofertas `OPEN` do lado oposto e chama `matchingEngine.match(...)` (Dev 3).
4. Se houver match: marca as duas como `MATCHED`, debita 1 Piggy do vendedor, credita 1 ao comprador e grava o `Trade`.
5. Publica `TransactionExecuted` e `ExchangeRatePublished` com o preço do match.

Todas as ofertas chegam com a chave `PIGGY-USD`, então ficam na mesma partição e o listener processa uma por vez. É isso que impede duas compras de casarem com a mesma venda.

Até o PR do Dev 3 entrar, use um engine provisório (só testa compra ≥ venda) para não ficar parado.

## 4. Testes (15 min)

- **Unitário** `WalletTest`: `debit` e `credit` mudam o saldo em 1.
- **Integração** `AuctionFlowTest extends IntegrationTest`:
  1. publique `OfferCreated` SELL 20 (vendedor A) e BUY 30 (comprador C) com um producer de teste;
  2. com Awaitility, espere um `TransactionExecuted` com `price` 25 e um `ExchangeRatePublished` com `rate` 25 (listeners de teste, como o `PingListener`);
  3. confira as carteiras: A com -1, C com +1;
  4. uma compra a 10 sozinha não gera transação.

## Pronto quando

`mvn test` verde, push na `dev4-coordinator` e PR para `main`. Depois, puxe a integração ponta a ponta do [README.md](README.md).

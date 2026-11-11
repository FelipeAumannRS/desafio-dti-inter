# Dev 2 — PiggiesSellerService (borda assíncrona) · Mike

Pasta `seller-service/` · pacote `co.inter.piggies.seller` · porta 8091 · branch `dev2-seller`
Contrato: `contracts/piggies-seller-service.openapi.yaml` · publica `contracts/events/offer-created.schema.json` · consome `contracts/events/transaction-executed.schema.json`

Faça antes os passos de "Todos" em [README.md](README.md). Este serviço é o espelho do Buyer ([dev-1-buyer.md](dev-1-buyer.md)); combine com o Dev 1 os nomes das classes para os dois ficarem iguais.

## 1. Entidade e repositório (10 min)

```java
@Entity
@Table(name = "sell_offers")
@Getter @Setter @NoArgsConstructor
public class SellOffer {
    @Id private UUID id;
    private String sellerId;
    private BigDecimal price;
    @Enumerated(EnumType.STRING) private OfferStatus status;   // OPEN, EXECUTED
    private UUID transactionId;
    private BigDecimal executedPrice;
    private Instant createdAt;
    private Instant executedAt;

    /** Devolve false se já estava executada: o Kafka pode entregar o mesmo evento duas vezes. */
    public boolean execute(UUID transactionId, BigDecimal price, Instant at) { ... }
}

@Repository
public interface SellOfferRepository extends CrudRepository<SellOffer, UUID> {}
```

## 2. Evento e producer (5 min)

```java
@Serdeable
public record OfferCreated(UUID eventId, UUID offerId, String side, String userId, BigDecimal price, Instant createdAt) {}

@KafkaClient
public interface OfferProducer {
    @Topic("piggies.offers.created")
    void send(@KafkaKey String key, OfferCreated event);   // key sempre "PIGGY-USD"
}
```

`side` é sempre `"SELL"` e `userId` é o `sellerId`.

## 3. Endpoints assíncronos (10 min)

```java
@Controller("/sell-offers")
@ExecuteOn(TaskExecutors.BLOCKING)
public class SellOfferController {

    @Post
    HttpResponse<SellOfferResponse> create(@Body @Valid CreateSellOfferRequest request) {
        SellOffer offer = service.create(request.sellerId(), request.price());   // grava OPEN e publica
        return HttpResponse.accepted(URI.create("/sell-offers/" + offer.getId()))
                .body(SellOfferResponse.from(offer));
    }

    @Get("/{offerId}")
    Optional<SellOfferResponse> get(UUID offerId) { ... }   // Optional vazio vira 404
}

@Serdeable @Introspected
public record CreateSellOfferRequest(@NotBlank String sellerId, @NotNull @Positive BigDecimal price) {}
```

## 4. Consumir o resultado (10 min)

```java
@KafkaListener(groupId = "seller-service", offsetReset = OffsetReset.EARLIEST)
public class TransactionExecutedListener {
    @Topic("piggies.transactions.executed")
    void on(TransactionExecuted event) {
        // busca event.sellOfferId(); se existir, offer.execute(...) e salva
    }
}
```

## 5. Testes (15 min)

- **Unitário** `SellOfferTest`: `execute` muda para `EXECUTED`; uma segunda chamada devolve `false`.
- **Integração** `SellOfferFlowTest extends IntegrationTest`:
  1. `POST /sell-offers` devolve `202`, `Location` e `OPEN`;
  2. um listener de teste recebe o `OfferCreated` com `side=SELL`;
  3. `price` 0 ou sem `sellerId` devolve `400`;
  4. publique um `TransactionExecuted` e espere (Awaitility) o `GET` devolver `EXECUTED`.

## Pronto quando

`mvn test` verde, push na `dev2-seller` e PR para `main`.

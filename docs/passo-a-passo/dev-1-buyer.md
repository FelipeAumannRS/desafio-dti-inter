# Dev 1 — PiggiesBuyerService (borda assíncrona)

Pasta `buyer-service/` · pacote `co.inter.piggies.buyer` · porta 8090 · branch `dev1-buyer`
Contrato: `contracts/piggies-buyer-service.openapi.yaml` · publica `contracts/events/offer-created.schema.json` · consome `contracts/events/transaction-executed.schema.json`

Faça antes os passos de "Todos" em [README.md](README.md).

## 1. Entidade e repositório (10 min)

```java
@Entity
@Table(name = "buy_offers")
@Getter @Setter @NoArgsConstructor
public class BuyOffer {
    @Id private UUID id;
    private String buyerId;
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
public interface BuyOfferRepository extends CrudRepository<BuyOffer, UUID> {}
```

## 2. Evento e producer (5 min)

Records iguais ao JSON Schema, com `@Serdeable`:

```java
@Serdeable
public record OfferCreated(UUID eventId, UUID offerId, String side, String userId, BigDecimal price, Instant createdAt) {}

@KafkaClient
public interface OfferProducer {
    @Topic("piggies.offers.created")
    void send(@KafkaKey String key, OfferCreated event);   // key sempre "PIGGY-USD"
}
```

`side` é sempre `"BUY"` neste serviço.

## 3. Endpoints assíncronos (10 min)

```java
@Controller("/buy-offers")
@ExecuteOn(TaskExecutors.BLOCKING)   // JPA é bloqueante: tira o trabalho do event loop do Netty
public class BuyOfferController {

    @Post
    HttpResponse<BuyOfferResponse> create(@Body @Valid CreateBuyOfferRequest request) {
        BuyOffer offer = service.create(request.buyerId(), request.price());   // grava OPEN e publica
        return HttpResponse.accepted(URI.create("/buy-offers/" + offer.getId()))
                .body(BuyOfferResponse.from(offer));
    }

    @Get("/{offerId}")
    Optional<BuyOfferResponse> get(UUID offerId) { ... }   // Optional vazio vira 404
}

@Serdeable @Introspected
public record CreateBuyOfferRequest(@NotBlank String buyerId, @NotNull @Positive BigDecimal price) {}
```

O `service.create` é `@Transactional`: salva a oferta e chama `producer.send("PIGGY-USD", event)`.

Por que é assíncrono: o `POST` não espera o match. Responde `202` assim que a oferta está salva e publicada; o match acontece no Coordinator e o cliente acompanha pelo `GET`.

## 4. Consumir o resultado (10 min)

```java
@KafkaListener(groupId = "buyer-service", offsetReset = OffsetReset.EARLIEST)
public class TransactionExecutedListener {
    @Topic("piggies.transactions.executed")
    void on(TransactionExecuted event) {
        // busca event.buyOfferId(); se existir, offer.execute(...) e salva
    }
}
```

Ofertas que não são deste serviço (ou já executadas) são ignoradas.

## 5. Testes (15 min)

- **Unitário** `BuyOfferTest`: `execute` muda para `EXECUTED` e preenche preço e transação; uma segunda chamada devolve `false` e não altera nada.
- **Integração** `BuyOfferFlowTest extends IntegrationTest`:
  1. `POST /buy-offers` devolve `202`, header `Location` e status `OPEN`;
  2. um listener de teste (como o `PingListener`) recebe o `OfferCreated` com `side=BUY`;
  3. `POST` com `price` 0 ou sem `buyerId` devolve `400`;
  4. publique um `TransactionExecuted` com um producer de teste e, com Awaitility, espere o `GET` devolver `EXECUTED` e `executedPrice` correto.

## Pronto quando

`mvn test` verde, push na `dev1-buyer` e PR para `main`.

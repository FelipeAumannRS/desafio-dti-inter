# Uso de IA — Dev 4 (Felipe, orquestração do Coordinator)

Ferramenta: Claude Code (Claude Opus 5.5). Cada entrada registra o pedido e o que foi produzido; o código foi revisado antes do commit.

## 1. Kafka no Coordinator (entrada e saída)

**Pedido:** próximo passo que não dependa dos outros devs: ligar o Coordinator ao Kafka.

**Resultado:**

- `event/`: records dos três eventos, iguais aos JSON Schemas; `Topics` com os nomes e a chave `PIGGY-USD`; `CoordinatorProducer` (`@KafkaClient`) publicando `TransactionExecuted` e `ExchangeRatePublished`.
- `book/`: `BookOfferEntity` (tabela `book_offers`, preço com 4 casas porque a média pode ter 3), `BookOfferRepository` com `findBySideAndStatus`, `OrderBookService.register` (ignora oferta repetida: o Kafka entrega pelo menos uma vez) e `OfferCreatedListener` no tópico `piggies.offers.created`.
- Testes: `BookOfferEntityTest` (unitário) e `CoordinatorKafkaTest` (integração com Testcontainers): oferta publicada vai para o livro; a mesma oferta duas vezes vira um registro só; os eventos de saída saem em JSON no formato do contrato (datas ISO, `pair` PIGGY/USD). 5 testes verdes.

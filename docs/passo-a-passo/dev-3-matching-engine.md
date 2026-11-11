# Dev 3 — Matching engine do Coordinator (Java puro)

Pasta `transaction-coordinator/` (app gerado pelo Dev 4) · pacote `co.inter.piggies.coordinator.matching` · branch `dev3-matching`

A regra de negócio central, sem banco, sem Kafka, sem Micronaut. Só classes Java e testes unitários. O Dev 4 chama o engine de dentro do listener.

Faça antes os passos de "Todos" em [README.md](README.md). O app `transaction-coordinator/` já existe na `main`: crie o pacote `co.inter.piggies.coordinator.matching` dentro dele (código em `src/main/java`, testes em `src/test/java`). O Dev 4 trabalha no mesmo app, em outros pacotes, então os dois não mexem nos mesmos arquivos.

Os testes do engine são unitários puros (sem `@MicronautTest`), então rodam mesmo antes do Dev 4 criar as entidades: `mvn test -Dtest=MatchingEngineTest`.

## 1. Modelo (5 min)

```java
public enum Side { BUY, SELL }

public record BookOffer(UUID offerId, Side side, String userId, BigDecimal price, Instant createdAt) {}

public record Match(BookOffer buy, BookOffer sell, BigDecimal price) {}
```

## 2. Engine (15 min)

```java
public class MatchingEngine {

    /**
     * @param incoming     a oferta que acabou de chegar
     * @param counterOffers ofertas OPEN do lado oposto
     */
    public Optional<Match> match(BookOffer incoming, List<BookOffer> counterOffers) { ... }
}
```

Regras (as mesmas de `contracts/README.md`):

- Há match quando **compra ≥ venda**.
- Chegou uma **compra**: entre as vendas com preço `<=` ao da compra, pega a **mais barata**.
- Chegou uma **venda**: entre as compras com preço `>=` ao da venda, pega a **mais cara**.
- Empate de preço: a **mais antiga** (`createdAt` menor).
- Preço da transação: `buy.price().add(sell.price()).divide(BigDecimal.TWO)`. Dividir por 2 sempre dá um decimal exato, então não precisa de arredondamento.

Dica: um `Comparator` por lado resolve preço e desempate juntos, por exemplo
`Comparator.comparing(BookOffer::price).thenComparing(BookOffer::createdAt)` para as vendas.

## 3. Testes unitários (15 min)

`MatchingEngineTest` (JUnit 5 + AssertJ):

- Exemplo do enunciado: venda A a 20, compra B a 10 → sem match; compra C a 30 → match A × C a 25.
- Compra igual à venda (20 e 20) → match a 20.
- Venda que chega e encontra duas compras (25 e 30) → casa com a de 30.
- Duas vendas no mesmo preço → casa com a mais antiga.
- Lista vazia ou nenhum preço compatível → `Optional.empty()`.
- `@ParameterizedTest` (junit-params) para a média: 20/30 → 25; 10.01/10.02 → 10.015.

## Pronto quando

`mvn test -Dtest=MatchingEngineTest` verde, push na `dev3-matching` e PR para `main`. Avise o Dev 4 quando entrar, para ele trocar o engine provisório pelo seu.

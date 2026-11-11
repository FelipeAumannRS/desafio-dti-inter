package co.inter.piggies.coordinator;

import co.inter.piggies.coordinator.book.BookOfferRepository;
import co.inter.piggies.coordinator.book.BookOfferStatus;
import co.inter.piggies.coordinator.event.OfferCreated;
import co.inter.piggies.coordinator.event.Topics;
import co.inter.piggies.coordinator.trade.TradeRepository;
import co.inter.piggies.coordinator.wallet.WalletRepository;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

/** O exemplo do enunciado, de ponta a ponta no Coordinator: ofertas entram pelo Kafka, transação e taxa saem pelo Kafka. */
@MicronautTest(transactional = false)
class AuctionFlowTest extends IntegrationTest {

    @Inject
    TestOfferProducer offers;

    @Inject
    BookOfferRepository book;

    @Inject
    WalletRepository wallets;

    @Inject
    TradeRepository trades;

    @Inject
    PublishedEvents published;

    @Test
    void sellerAAndBuyerCMatchAt25AndBuyerBStaysOpen() {
        // Ids únicos por execução: os containers são compartilhados entre as classes de teste.
        String run = UUID.randomUUID().toString().substring(0, 8);
        String sellerA = "seller-A-" + run;
        String buyerB = "buyer-B-" + run;
        String buyerC = "buyer-C-" + run;
        UUID sellA = UUID.randomUUID();
        UUID buyB = UUID.randomUUID();
        UUID buyC = UUID.randomUUID();

        offers.send(Topics.MARKET_KEY, offer(sellA, "SELL", sellerA, "20"));
        offers.send(Topics.MARKET_KEY, offer(buyB, "BUY", buyerB, "10"));   // 10 < 20: sem match
        offers.send(Topics.MARKET_KEY, offer(buyC, "BUY", buyerC, "30"));   // 30 >= 20: match a (30 + 20) / 2 = 25

        await().atMost(Duration.ofSeconds(30)).untilAsserted(() ->
                assertThat(published.transactions).anySatisfy(message -> assertThat(message.json())
                        .contains("\"sellOfferId\":\"" + sellA + "\"", "\"buyOfferId\":\"" + buyC + "\"", "\"price\":25")));
        String transactionId = published.transactions.stream()
                .filter(message -> message.json().contains(sellA.toString()))
                .findFirst().orElseThrow().key();

        // O evento sai antes do commit; por isso as verificações do banco também esperam.
        await().atMost(Duration.ofSeconds(30)).untilAsserted(() -> {
            assertThat(published.rates).anySatisfy(message ->
                    assertThat(message.json()).contains(transactionId, "\"rate\":25"));
            assertThat(trades.findById(UUID.fromString(transactionId)))
                    .hasValueSatisfying(trade -> assertThat(trade.getPrice()).isEqualByComparingTo("25"));
            assertThat(book.findById(sellA)).hasValueSatisfying(o -> assertThat(o.getStatus()).isEqualTo(BookOfferStatus.MATCHED));
            assertThat(book.findById(buyC)).hasValueSatisfying(o -> assertThat(o.getStatus()).isEqualTo(BookOfferStatus.MATCHED));
            assertThat(book.findById(buyB)).hasValueSatisfying(o -> assertThat(o.getStatus()).isEqualTo(BookOfferStatus.OPEN));
            assertThat(wallets.findById(sellerA)).hasValueSatisfying(w -> assertThat(w.getPiggies()).isEqualTo(-1));
            assertThat(wallets.findById(buyerC)).hasValueSatisfying(w -> assertThat(w.getPiggies()).isEqualTo(1));
            assertThat(wallets.findById(buyerB)).isEmpty();
        });
    }

    private static OfferCreated offer(UUID offerId, String side, String userId, String price) {
        return new OfferCreated(UUID.randomUUID(), offerId, side, userId, new BigDecimal(price), Instant.now());
    }
}

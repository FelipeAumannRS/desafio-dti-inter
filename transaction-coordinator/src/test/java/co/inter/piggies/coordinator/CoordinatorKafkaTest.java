package co.inter.piggies.coordinator;

import co.inter.piggies.coordinator.book.BookOfferRepository;
import co.inter.piggies.coordinator.book.BookOfferStatus;
import co.inter.piggies.coordinator.event.CoordinatorProducer;
import co.inter.piggies.coordinator.event.ExchangeRatePublished;
import co.inter.piggies.coordinator.event.OfferCreated;
import co.inter.piggies.coordinator.event.Topics;
import co.inter.piggies.coordinator.event.TransactionExecuted;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

/**
 * Integração com Kafka e Postgres de verdade (Testcontainers, via IntegrationTest).
 * transactional = false: cada consulta ao repositório roda na sua própria transação e enxerga
 * o que o listener gravou em outra thread.
 */
@MicronautTest(transactional = false)
class CoordinatorKafkaTest extends IntegrationTest {

    @Inject
    TestOfferProducer offers;

    @Inject
    BookOfferRepository book;

    @Inject
    CoordinatorProducer producer;

    @Inject
    PublishedEvents published;

    @Test
    void offerPublishedOnKafkaIsStoredInTheBook() {
        UUID offerId = UUID.randomUUID();

        offers.send(Topics.MARKET_KEY, new OfferCreated(UUID.randomUUID(), offerId, "SELL", "seller-A",
                new BigDecimal("20.00"), Instant.now()));

        // O consumo é assíncrono: espera o listener gravar, sem Thread.sleep.
        await().atMost(Duration.ofSeconds(30)).untilAsserted(() ->
                assertThat(book.findById(offerId)).hasValueSatisfying(offer -> {
                    assertThat(offer.getSide()).isEqualTo("SELL");
                    assertThat(offer.getUserId()).isEqualTo("seller-A");
                    assertThat(offer.getPrice()).isEqualByComparingTo("20");
                    assertThat(offer.getStatus()).isEqualTo(BookOfferStatus.OPEN);
                }));
    }

    @Test
    void sameOfferDeliveredTwiceIsStoredOnce() {
        OfferCreated event = new OfferCreated(UUID.randomUUID(), UUID.randomUUID(), "BUY", "buyer-B",
                new BigDecimal("10.00"), Instant.now());
        UUID marker = UUID.randomUUID();

        offers.send(Topics.MARKET_KEY, event);
        offers.send(Topics.MARKET_KEY, event);
        // Mesma partição, mesma ordem: quando a oferta "marcador" aparece, as duas anteriores já foram processadas.
        offers.send(Topics.MARKET_KEY, new OfferCreated(UUID.randomUUID(), marker, "BUY", "buyer-B",
                new BigDecimal("11.00"), Instant.now()));

        await().atMost(Duration.ofSeconds(30)).until(() -> book.existsById(marker));
        assertThat(book.findById(event.offerId())).isPresent();
    }

    @Test
    void outputEventsAreJsonInTheContractFormat() {
        UUID transactionId = UUID.randomUUID();

        producer.transactionExecuted(transactionId.toString(), new TransactionExecuted(UUID.randomUUID(), transactionId,
                UUID.randomUUID(), UUID.randomUUID(), "buyer-C", "seller-A", new BigDecimal("25"), 1, Instant.now()));
        producer.exchangeRatePublished(Topics.MARKET_KEY, new ExchangeRatePublished(UUID.randomUUID(), transactionId,
                ExchangeRatePublished.PAIR, new BigDecimal("25"), Instant.now()));

        await().atMost(Duration.ofSeconds(30)).untilAsserted(() -> {
            assertThat(published.transactions).anySatisfy(message -> {
                assertThat(message.key()).isEqualTo(transactionId.toString());
                assertThat(message.json()).contains("\"price\":25", "\"quantity\":1", "\"buyerId\":\"buyer-C\"");
            });
            assertThat(published.rates).anySatisfy(message -> {
                assertThat(message.key()).isEqualTo(Topics.MARKET_KEY);
                assertThat(message.json()).contains(transactionId.toString(), "\"pair\":\"PIGGY/USD\"", "\"rate\":25");
            });
        });
        published.rates.stream().filter(m -> m.json().contains(transactionId.toString()))
                .forEach(m -> System.out.println("SMOKE exchange-rate json: " + m.json()));
    }
}

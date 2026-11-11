package co.inter.piggies.coordinator.book;

import co.inter.piggies.coordinator.event.OfferCreated;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/** Teste unitário puro: sem Micronaut, sem banco, sem Kafka. */
class BookOfferEntityTest {

    @Test
    void offerFromEventStartsOpenWithTheEventData() {
        OfferCreated event = new OfferCreated(UUID.randomUUID(), UUID.randomUUID(), "BUY", "buyer-C",
                new BigDecimal("30.00"), Instant.parse("2026-11-11T17:00:00Z"));

        BookOfferEntity offer = BookOfferEntity.open(event);

        assertThat(offer.getOfferId()).isEqualTo(event.offerId());
        assertThat(offer.getSide()).isEqualTo("BUY");
        assertThat(offer.getUserId()).isEqualTo("buyer-C");
        assertThat(offer.getPrice()).isEqualByComparingTo("30");
        assertThat(offer.getCreatedAt()).isEqualTo(event.createdAt());
        assertThat(offer.getStatus()).isEqualTo(BookOfferStatus.OPEN);
    }
}

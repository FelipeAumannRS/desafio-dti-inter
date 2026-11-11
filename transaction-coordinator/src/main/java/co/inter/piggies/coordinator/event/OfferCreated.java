package co.inter.piggies.coordinator.event;

import io.micronaut.serde.annotation.Serdeable;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

/** contracts/events/offer-created.schema.json. Publicado pelo Buyer (side BUY) e pelo Seller (side SELL). */
@Serdeable
public record OfferCreated(UUID eventId, UUID offerId, String side, String userId, BigDecimal price, Instant createdAt) {
}

package co.inter.piggies.coordinator.matching;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

/** Uma oferta aberta, do jeito que o engine enxerga: sem banco, sem Kafka. */
public record BookOffer(UUID offerId, Side side, String userId, BigDecimal price, Instant createdAt) {
}

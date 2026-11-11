package co.inter.piggies.domain;

import io.micronaut.serde.annotation.Serdeable;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

/** Schema SellOffer de contracts/piggies-seller-service.openapi.yaml */
@Serdeable
public record SellOfferResponse(UUID offerId, String sellerId, BigDecimal price, OfferStatus status, UUID transactionId,
                                BigDecimal executedPrice, Instant createdAt, Instant executedAt) {
}

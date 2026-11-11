package co.inter.piggies.domain;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record SellOfferResponse(
        UUID id,
        String sellerId,
        BigDecimal price,
        OfferStatus status,
        UUID transactionId,
        BigDecimal executedPrice,
        Instant createdAt,
        Instant executedAt
) {
    public static SellOfferResponse from(SellOffer offer) {
        return new SellOfferResponse(
                offer.getId(),
                offer.getSellerId(),
                offer.getPrice(),
                offer.getStatus(),
                offer.getTransactionId(),
                offer.getExecutedPrice(),
                offer.getCreatedAt(),
                offer.getExecutedAt()
        );
    }
}
@Serdeable
public record OfferCreated(UUID eventId, UUID offerId, String side, String userId, BigDecimal price, Instant createdAt) {}
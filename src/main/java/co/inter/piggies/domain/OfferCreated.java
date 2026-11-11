package co.inter.piggies.domain;

import io.micronaut.serde.annotation.Serdeable;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Serdeable
public record OfferCreated(UUID eventId, UUID offerId, String side, String userId, BigDecimal price, Instant createdAt) {}
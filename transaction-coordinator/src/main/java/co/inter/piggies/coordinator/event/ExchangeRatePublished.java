package co.inter.piggies.coordinator.event;

import io.micronaut.serde.annotation.Serdeable;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

/** contracts/events/exchange-rate-published.schema.json. A nova taxa, para outros sistemas. */
@Serdeable
public record ExchangeRatePublished(UUID eventId, UUID transactionId, String pair, BigDecimal rate, Instant publishedAt) {

    public static final String PAIR = "PIGGY/USD";
}

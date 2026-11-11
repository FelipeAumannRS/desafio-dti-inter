package co.inter.piggies.domain;

import io.micronaut.serde.annotation.Serdeable;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

/** contracts/events/transaction-executed.schema.json */
@Serdeable
public record TransactionExecuted(UUID eventId, UUID transactionId, UUID buyOfferId, UUID sellOfferId,
                                  String buyerId, String sellerId, BigDecimal price, int quantity, Instant executedAt) {
}

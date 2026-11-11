package co.inter.piggies.coordinator.event;

import io.micronaut.serde.annotation.Serdeable;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

/** contracts/events/transaction-executed.schema.json. Buyer e Seller consomem para marcar as ofertas como EXECUTED. */
@Serdeable
public record TransactionExecuted(UUID eventId,
                                  UUID transactionId,
                                  UUID buyOfferId,
                                  UUID sellOfferId,
                                  String buyerId,
                                  String sellerId,
                                  BigDecimal price,
                                  int quantity,
                                  Instant executedAt) {
}

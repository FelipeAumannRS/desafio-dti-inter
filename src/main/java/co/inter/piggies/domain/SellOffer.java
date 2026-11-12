package co.inter.piggies.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "sell_offers")
@Getter
@Setter
@NoArgsConstructor
public class SellOffer {
    @Id
    private UUID id;
    private String sellerId;
    private BigDecimal price;
    @Enumerated(EnumType.STRING)
    private OfferStatus status;   // OPEN, EXECUTED
    private UUID transactionId;
    private BigDecimal executedPrice;
    private Instant createdAt;
    private Instant executedAt;

    /** Devolve false se já estava executada: o Kafka pode entregar o mesmo evento duas vezes. */
    public boolean execute(UUID transactionId, BigDecimal price, Instant at) {
        if (status == OfferStatus.EXECUTED) {
            return false;
        }

        this.status = OfferStatus.EXECUTED;
        this.transactionId = transactionId;
        this.executedPrice = price;
        this.executedAt = at;
        return true;
    }
}

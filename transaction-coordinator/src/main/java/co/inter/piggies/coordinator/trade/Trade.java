package co.inter.piggies.coordinator.trade;

import co.inter.piggies.coordinator.matching.Match;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

/** Uma transação executada: 1 Piggy do vendedor para o comprador, pelo preço médio. */
@Entity
@Table(name = "trades")
@Getter
@NoArgsConstructor
public class Trade {

    @Id
    private UUID transactionId;

    private UUID buyOfferId;
    private UUID sellOfferId;
    private String buyerId;
    private String sellerId;

    @Column(precision = 19, scale = 4)
    private BigDecimal price;

    private Instant executedAt;

    public static Trade of(Match match, Instant executedAt) {
        Trade trade = new Trade();
        trade.transactionId = UUID.randomUUID();
        trade.buyOfferId = match.buy().offerId();
        trade.sellOfferId = match.sell().offerId();
        trade.buyerId = match.buy().userId();
        trade.sellerId = match.sell().userId();
        trade.price = match.price();
        trade.executedAt = executedAt;
        return trade;
    }
}

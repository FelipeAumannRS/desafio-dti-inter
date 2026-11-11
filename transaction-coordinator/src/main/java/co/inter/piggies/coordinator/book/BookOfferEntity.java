package co.inter.piggies.coordinator.book;

import co.inter.piggies.coordinator.event.OfferCreated;
import jakarta.persistence.Column;
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

/** Uma oferta no livro do Coordinator: a cópia local do que chegou pelo Kafka. */
@Entity
@Table(name = "book_offers")
@Getter
@Setter
@NoArgsConstructor
public class BookOfferEntity {

    @Id
    private UUID offerId;

    /** BUY ou SELL, como no contrato. */
    private String side;

    private String userId;

    /** O padrão do Hibernate para BigDecimal é 2 casas; a média de dois preços pode ter 3. */
    @Column(precision = 19, scale = 4)
    private BigDecimal price;

    private Instant createdAt;

    @Enumerated(EnumType.STRING)
    private BookOfferStatus status;

    public static BookOfferEntity open(OfferCreated event) {
        BookOfferEntity offer = new BookOfferEntity();
        offer.offerId = event.offerId();
        offer.side = event.side();
        offer.userId = event.userId();
        offer.price = event.price();
        offer.createdAt = event.createdAt();
        offer.status = BookOfferStatus.OPEN;
        return offer;
    }
}

package co.inter.piggies.coordinator.book;

import co.inter.piggies.coordinator.event.OfferCreated;
import jakarta.inject.Singleton;
import jakarta.transaction.Transactional;

@Singleton
public class OrderBookService {

    private final BookOfferRepository offers;

    public OrderBookService(BookOfferRepository offers) {
        this.offers = offers;
    }

    /**
     * Guarda a oferta no livro como OPEN.
     * Devolve false se ela já estava lá: o Kafka entrega "pelo menos uma vez", então a mesma mensagem pode chegar de novo.
     */
    @Transactional
    public boolean register(OfferCreated event) {
        if (offers.existsById(event.offerId())) {
            return false;
        }
        offers.save(BookOfferEntity.open(event));
        return true;
    }
}

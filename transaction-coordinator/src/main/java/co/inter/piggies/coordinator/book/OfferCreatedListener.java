package co.inter.piggies.coordinator.book;

import co.inter.piggies.coordinator.event.OfferCreated;
import co.inter.piggies.coordinator.event.Topics;
import io.micronaut.configuration.kafka.annotation.KafkaListener;
import io.micronaut.configuration.kafka.annotation.OffsetReset;
import io.micronaut.configuration.kafka.annotation.Topic;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Entrada do Coordinator: toda oferta criada no Buyer ou no Seller chega aqui.
 * EARLIEST: um consumer group novo começa do início do tópico, sem perder ofertas publicadas antes de ele subir.
 */
@KafkaListener(groupId = "transaction-coordinator", offsetReset = OffsetReset.EARLIEST)
public class OfferCreatedListener {

    private static final Logger LOG = LoggerFactory.getLogger(OfferCreatedListener.class);

    private final OrderBookService orderBook;

    public OfferCreatedListener(OrderBookService orderBook) {
        this.orderBook = orderBook;
    }

    @Topic(Topics.OFFERS_CREATED)
    void onOfferCreated(OfferCreated event) {
        boolean registered = orderBook.register(event);
        LOG.info("{} offer {} at {} {}", event.side(), event.offerId(), event.price(),
                registered ? "registered in the book" : "already in the book, ignored");
    }
}

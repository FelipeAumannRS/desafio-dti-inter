package co.inter.piggies.coordinator.book;

import co.inter.piggies.coordinator.auction.AuctionService;
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
 * O tópico tem uma partição só, então as ofertas são processadas uma de cada vez, na ordem de chegada.
 */
@KafkaListener(groupId = "transaction-coordinator", offsetReset = OffsetReset.EARLIEST)
public class OfferCreatedListener {

    private static final Logger LOG = LoggerFactory.getLogger(OfferCreatedListener.class);

    private final AuctionService auction;

    public OfferCreatedListener(AuctionService auction) {
        this.auction = auction;
    }

    @Topic(Topics.OFFERS_CREATED)
    void onOfferCreated(OfferCreated event) {
        auction.process(event).ifPresentOrElse(
                trade -> LOG.info("{} offer {} matched: transaction {} at {}", event.side(), event.offerId(),
                        trade.getTransactionId(), trade.getPrice()),
                () -> LOG.info("{} offer {} at {} processed, no match", event.side(), event.offerId(), event.price()));
    }
}

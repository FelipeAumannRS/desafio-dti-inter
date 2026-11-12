package co.inter.piggies.messaging;

import co.inter.piggies.domain.TransactionExecuted;
import co.inter.piggies.domain.SellOffer;
import co.inter.piggies.repository.SellOfferRepository;
import io.micronaut.configuration.kafka.annotation.KafkaListener;
import io.micronaut.configuration.kafka.annotation.OffsetReset;
import io.micronaut.configuration.kafka.annotation.Topic;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;

@KafkaListener(groupId = "seller-service", offsetReset = OffsetReset.EARLIEST)
public class TransactionExecutedListener {

    private static final Logger LOG = LoggerFactory.getLogger(TransactionExecutedListener.class);

    private final SellOfferRepository repository;

    public TransactionExecutedListener(SellOfferRepository repository) {
        this.repository = repository;
    }

    @Topic("piggies.transactions.executed")
    @Transactional
    void on(TransactionExecuted event) {
        Optional<SellOffer> offerResult = repository.findById(event.sellOfferId());
        if (offerResult.isEmpty()) {
            LOG.warn("Transaction {} references unknown sell offer {}", event.transactionId(), event.sellOfferId());
            return;
        }

        SellOffer offer = offerResult.get();
        if (!offer.execute(event.transactionId(), event.price(), event.executedAt())) {
            LOG.info("Sell offer {} was already executed; ignoring duplicate transaction event {}",
                    offer.getId(), event.transactionId());
            return;
        }

        repository.update(offer);
        LOG.info("Sell offer {} executed in transaction {} at {}", offer.getId(),
                event.transactionId(), event.price());
    }
}

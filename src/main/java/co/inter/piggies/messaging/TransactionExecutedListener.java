package co.inter.piggies.messaging;

import io.micronaut.configuration.kafka.annotation.KafkaListener;
import io.micronaut.configuration.kafka.annotation.OffsetReset;
import io.micronaut.configuration.kafka.annotation.Topic;

@KafkaListener(groupId = "seller-service", offsetReset = OffsetReset.EARLIEST)
public class TransactionExecutedListener {
    @Topic("piggies.transactions.executed")
    void on(TransactionExecuted event) {
        // busca event.sellOfferId(); se existir, offer.execute(...) e salva
    }
}
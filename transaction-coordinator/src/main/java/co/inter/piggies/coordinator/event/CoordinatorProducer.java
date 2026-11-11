package co.inter.piggies.coordinator.event;

import io.micronaut.configuration.kafka.annotation.KafkaClient;
import io.micronaut.configuration.kafka.annotation.KafkaKey;
import io.micronaut.configuration.kafka.annotation.Topic;

/** O Micronaut gera a implementação desta interface: cada método publica no tópico indicado. */
@KafkaClient
public interface CoordinatorProducer {

    @Topic(Topics.TRANSACTIONS_EXECUTED)
    void transactionExecuted(@KafkaKey String transactionId, TransactionExecuted event);

    @Topic(Topics.EXCHANGE_RATE_PUBLISHED)
    void exchangeRatePublished(@KafkaKey String key, ExchangeRatePublished event);
}

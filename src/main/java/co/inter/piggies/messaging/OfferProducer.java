package co.inter.piggies.messaging;

import co.inter.piggies.domain.OfferCreated;
import io.micronaut.configuration.kafka.annotation.KafkaClient;
import io.micronaut.configuration.kafka.annotation.KafkaKey;
import io.micronaut.configuration.kafka.annotation.Topic;

@KafkaClient
public interface OfferProducer {
    @Topic("piggies.offers.created")
    void send(@KafkaKey String key, OfferCreated event);   // key sempre "PIGGY-USD"
}
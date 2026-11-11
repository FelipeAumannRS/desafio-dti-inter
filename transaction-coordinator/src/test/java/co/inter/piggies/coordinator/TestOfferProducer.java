package co.inter.piggies.coordinator;

import co.inter.piggies.coordinator.event.OfferCreated;
import co.inter.piggies.coordinator.event.Topics;
import io.micronaut.configuration.kafka.annotation.KafkaClient;
import io.micronaut.configuration.kafka.annotation.KafkaKey;
import io.micronaut.configuration.kafka.annotation.Topic;

/** Faz o papel do Buyer e do Seller nos testes: publica ofertas no tópico de entrada do Coordinator. */
@KafkaClient
interface TestOfferProducer {

    @Topic(Topics.OFFERS_CREATED)
    void send(@KafkaKey String key, OfferCreated event);
}

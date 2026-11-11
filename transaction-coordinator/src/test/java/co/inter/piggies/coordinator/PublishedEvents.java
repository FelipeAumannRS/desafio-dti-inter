package co.inter.piggies.coordinator;

import co.inter.piggies.coordinator.event.Topics;
import io.micronaut.configuration.kafka.annotation.KafkaKey;
import io.micronaut.configuration.kafka.annotation.KafkaListener;
import io.micronaut.configuration.kafka.annotation.OffsetReset;
import io.micronaut.configuration.kafka.annotation.Topic;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Faz o papel de quem consome as saídas do Coordinator (Buyer, Seller, outros sistemas).
 * Guarda o JSON cru, para o teste conferir o formato que vai para o tópico, e não só o objeto Java.
 */
@KafkaListener(groupId = "coordinator-test", offsetReset = OffsetReset.EARLIEST)
class PublishedEvents {

    record Message(String key, String json) {
    }

    final List<Message> transactions = new CopyOnWriteArrayList<>();
    final List<Message> rates = new CopyOnWriteArrayList<>();

    @Topic(Topics.TRANSACTIONS_EXECUTED)
    void onTransaction(@KafkaKey String key, String json) {
        transactions.add(new Message(key, json));
    }

    @Topic(Topics.EXCHANGE_RATE_PUBLISHED)
    void onRate(@KafkaKey String key, String json) {
        rates.add(new Message(key, json));
    }
}

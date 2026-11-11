@KafkaClient
public interface OfferProducer {
    @Topic("piggies.offers.created")
    void send(@KafkaKey String key, OfferCreated event);   // key sempre "PIGGY-USD"
}
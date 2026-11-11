package co.inter.piggies.service;

import co.inter.piggies.domain.OfferCreated;
import co.inter.piggies.domain.OfferStatus;
import co.inter.piggies.domain.SellOffer;
import co.inter.piggies.messaging.OfferProducer;
import co.inter.piggies.repository.SellOfferRepository;
import jakarta.inject.Singleton;
import jakarta.transaction.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

@Singleton
public class SellOfferService {

    private static final String OFFER_KEY = "PIGGY-USD";

    private final SellOfferRepository repository;
    private final OfferProducer producer;

    public SellOfferService(SellOfferRepository repository, OfferProducer producer) {
        this.repository = repository;
        this.producer = producer;
    }

    @Transactional
    public SellOffer create(String sellerId, BigDecimal price) {
        Instant createdAt = Instant.now();

        SellOffer offer = new SellOffer();
        offer.setId(UUID.randomUUID());
        offer.setSellerId(sellerId);
        offer.setPrice(price);
        offer.setStatus(OfferStatus.OPEN);
        offer.setCreatedAt(createdAt);

        SellOffer savedOffer = repository.save(offer);

        OfferCreated event = new OfferCreated(
                UUID.randomUUID(),
                savedOffer.getId(),
                "OPEN",
                sellerId,
                price,
                createdAt
        );
        producer.send(OFFER_KEY, event);

        return savedOffer;
    }

    @Transactional
    public Optional<SellOffer> get(UUID offerId) {
        return repository.findById(offerId);
    }
}

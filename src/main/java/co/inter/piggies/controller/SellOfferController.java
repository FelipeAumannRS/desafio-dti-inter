package co.inter.piggies.controller;

import co.inter.piggies.domain.CreateSellOfferRequest;
import co.inter.piggies.domain.SellOffer;
import co.inter.piggies.domain.SellOfferResponse;
import co.inter.piggies.service.SellOfferService;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.annotation.Body;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Get;
import io.micronaut.http.annotation.Post;
import io.micronaut.scheduling.TaskExecutors;
import io.micronaut.scheduling.annotation.ExecuteOn;
import jakarta.validation.Valid;

import java.net.URI;
import java.util.Optional;
import java.util.UUID;

@Controller("/sell-offers")
@ExecuteOn(TaskExecutors.BLOCKING)
public class SellOfferController {

    private final SellOfferService service;

    public SellOfferController(SellOfferService service) {
        this.service = service;
    }

    @Post
    HttpResponse<SellOfferResponse> create(@Body @Valid CreateSellOfferRequest request) {
        SellOffer offer = service.create(request.sellerId(), request.price());
        return HttpResponse.accepted(URI.create("/sell-offers/" + offer.getId()))
                .body(toResponse(offer));
    }

    @Get("/{offerId}")
    Optional<SellOfferResponse> get(UUID offerId) {
        return service.get(offerId).map(SellOfferController::toResponse);
    }

    private static SellOfferResponse toResponse(SellOffer offer) {
        return new SellOfferResponse(
                offer.getId(),
                offer.getSellerId(),
                offer.getPrice(),
                offer.getStatus(),
                offer.getTransactionId(),
                offer.getExecutedPrice(),
                offer.getCreatedAt(),
                offer.getExecutedAt()
        );
    }
}

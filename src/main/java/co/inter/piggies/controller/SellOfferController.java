package co.inter.piggies.controller;

import co.inter.piggies.domain.CreateSellOfferRequest;
import co.inter.piggies.domain.SellOffer;
import co.inter.piggies.domain.SellOfferResponse;
import io.micronaut.http.annotation.Body;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Get;
import io.micronaut.http.annotation.Post;
import io.micronaut.scheduling.TaskExecutors;
import io.micronaut.scheduling.annotation.ExecuteOn;
import jakarta.validation.Valid;

import java.net.URI;
import io.micronaut.http.HttpResponse;
import java.util.Optional;
import java.util.UUID;

@Controller("/sell-offers")
@ExecuteOn(TaskExecutors.BLOCKING)
public class SellOfferController {

    @Post
    HttpResponse<SellOfferResponse> create(@Body @Valid CreateSellOfferRequest request) {
        // TODO Mike: depende do SellOfferService, que ainda não existe
        // SellOffer offer = service.create(request.sellerId(), request.price());   // grava OPEN e publica
        // return HttpResponse.accepted(URI.create("/sell-offers/" + offer.getId()))
        //         .body(SellOfferResponse.from(offer));
        throw new UnsupportedOperationException("TODO");
    }

    @Get("/{offerId}")
    Optional<SellOfferResponse> get(UUID offerId) { throw new UnsupportedOperationException("TODO"); }   // Optional vazio vira 404
}
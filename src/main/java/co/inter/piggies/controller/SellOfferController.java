@Controller("/sell-offers")
@ExecuteOn(TaskExecutors.BLOCKING)
public class SellOfferController {

    @Post
    HttpResponse<SellOfferResponse> create(@Body @Valid CreateSellOfferRequest request) {
        SellOffer offer = service.create(request.sellerId(), request.price());   // grava OPEN e publica
        return HttpResponse.accepted(URI.create("/sell-offers/" + offer.getId()))
                .body(SellOfferResponse.from(offer));
    }

    @Get("/{offerId}")
    Optional<SellOfferResponse> get(UUID offerId) { ... }   // Optional vazio vira 404
}
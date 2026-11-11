package co.inter.piggies.buyer.kafka.events;

import io.micronaut.serde.annotation.Serdeable;

/*
 *-------------------------------------------------------------------------*
 * @Serdeable: o Micronaut Serialization gera, em tempo de compilação, o   *
 * código que converte JSON em objeto (e vice-versa). Sem ele, o Micronaut *
 * não consegue ler o body da requisição nem devolver este DTO como JSON.  *
 *-------------------------------------------------------------------------*
 */

@Serdeable
public record OfferCreatedEventDTO(
        String eventId,
        String offerId,
        String side,
        String price,
        String createdAt
) {
}

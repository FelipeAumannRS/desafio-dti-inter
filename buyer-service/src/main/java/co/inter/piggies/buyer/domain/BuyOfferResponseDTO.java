package co.inter.piggies.buyer.domain;

import io.micronaut.serde.annotation.Serdeable;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

/*
 *-------------------------------------------------------------------------*
 * @Serdeable: o Micronaut Serialization gera, em tempo de compilação, o   *
 * código que converte JSON em objeto (e vice-versa). Sem ele, o Micronaut *
 * não consegue ler o body da requisição nem devolver este DTO como JSON.  *
 *-------------------------------------------------------------------------*
 */
@Serdeable
public record BuyOfferResponseDTO(
        UUID id,
        String status,
        BigDecimal price,
        BigDecimal priceExecuted,
        OffsetDateTime createdAt,
        OffsetDateTime executedAt

) {
    public static BuyOfferResponseDTO from(BuyOffer buyOffer) {
        return new BuyOfferResponseDTO(
                buyOffer.getId(),
                buyOffer.getStatus(),
                buyOffer.getPrice(),
                buyOffer.getExecutedPrice(),
                buyOffer.getCreatedAt(),
                buyOffer.getExecutedAt()
        );
    }
}
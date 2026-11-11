package co.inter.piggies.buyer.domain;

import io.micronaut.serde.annotation.Serdeable;
import jakarta.annotation.Nullable;

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
        String id,
        String price,
        String status,
        @Nullable String executedPrice,
        String createdAt,
        @Nullable String executedAt
) {

    public static BuyOfferResponseDTO from(BuyOffer offer) {
        return new BuyOfferResponseDTO(
                offer.getId().toString(),
                offer.getPrice().toPlainString(),
                offer.getStatus(),
                offer.getExecutedPrice() == null ? null : offer.getExecutedPrice().toPlainString(),
                offer.getCreatedAt().toString(),
                offer.getExecutedAt() == null ? null : offer.getExecutedAt().toString()
        );
    }
}
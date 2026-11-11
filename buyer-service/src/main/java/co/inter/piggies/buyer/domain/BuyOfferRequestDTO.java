package co.inter.piggies.buyer.domain;

import io.micronaut.core.annotation.Introspected;
import io.micronaut.serde.annotation.Serdeable;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

import java.math.BigDecimal;
import java.math.RoundingMode;

/*
 *-------------------------------------------------------------------------*
 * @Serdeable: o Micronaut Serialization gera, em tempo de compilação, o   *
 * código que converte JSON em objeto (e vice-versa). Sem ele, o Micronaut *
 * não consegue ler o body da requisição nem devolver este DTO como JSON.  *
 *-------------------------------------------------------------------------*
 */

@Serdeable
@Introspected
public record BuyOfferRequestDTO(

        @NotBlank(message = "price é obrigatório")
        @Pattern(regexp = "^(?!0+(\\.0{1,2})?$)[0-9]+(\\.[0-9]{1,2})?$",
                message = "price deve ser um decimal positivo, ex.: 20.00")
        String price
) {

    public BigDecimal priceAsDecimal() {
        return new BigDecimal(price).setScale(2, RoundingMode.HALF_UP);
    }
}
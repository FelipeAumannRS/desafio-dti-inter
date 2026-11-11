package co.inter.piggies.domain;

import io.micronaut.core.annotation.Introspected;
import io.micronaut.serde.annotation.Serdeable;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

@Serdeable
@Introspected
public record CreateSellOfferRequest(@NotBlank String sellerId, @NotNull @Positive BigDecimal price) {}
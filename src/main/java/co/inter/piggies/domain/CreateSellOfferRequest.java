@Serdeable
@Introspected
public record CreateSellOfferRequest(@NotBlank String sellerId, @NotNull @Positive BigDecimal price) {}
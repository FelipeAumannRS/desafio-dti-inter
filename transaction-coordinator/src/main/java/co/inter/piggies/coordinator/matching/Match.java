package co.inter.piggies.coordinator.matching;

import java.math.BigDecimal;

public record Match(BookOffer buy, BookOffer sell, BigDecimal price) {
}

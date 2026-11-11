package co.inter.piggies.coordinator.matching;

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

/**
 * Regra do leilão. Dono: Fernando (Dev 3), ver docs/passo-a-passo/dev-3-matching-engine.md.
 */
public class MatchingEngine {

    private static final Comparator<BookOffer> SELL_PRIORITY =
            Comparator.comparing(BookOffer::price)
                    .thenComparing(BookOffer::createdAt);

    private static final Comparator<BookOffer> BUY_PRIORITY =
            Comparator.comparing(BookOffer::price, Comparator.reverseOrder())
                    .thenComparing(BookOffer::createdAt);

    /**
     * @param incoming      a oferta que acabou de chegar
     * @param counterOffers ofertas OPEN do lado oposto
     */
    public Optional<Match> match(BookOffer incoming, List<BookOffer> counterOffers) {
        return counterOffers.stream()
                .filter(counterOffer -> isCompatible(incoming, counterOffer))
                .min(priorityFor(incoming.side()))
                .map(counterOffer -> createMatch(incoming, counterOffer));
    }

    /** Dividir por 2 sempre dá um decimal exato, então não há arredondamento. */
    static BigDecimal average(BigDecimal buyPrice, BigDecimal sellPrice) {
        return buyPrice.add(sellPrice).divide(BigDecimal.TWO);
    }

    private static boolean isCompatible(BookOffer incoming, BookOffer counterOffer) {
        BookOffer buy = incoming.side() == Side.BUY ? incoming : counterOffer;
        BookOffer sell = incoming.side() == Side.SELL ? incoming : counterOffer;
        return buy.price().compareTo(sell.price()) >= 0;
    }

    private static Comparator<BookOffer> priorityFor(Side incomingSide) {
        return incomingSide == Side.BUY ? SELL_PRIORITY : BUY_PRIORITY;
    }

    private static Match createMatch(BookOffer incoming, BookOffer counterOffer) {
        BookOffer buy = incoming.side() == Side.BUY ? incoming : counterOffer;
        BookOffer sell = incoming.side() == Side.SELL ? incoming : counterOffer;
        return new Match(buy, sell, average(buy.price(), sell.price()));
    }
}

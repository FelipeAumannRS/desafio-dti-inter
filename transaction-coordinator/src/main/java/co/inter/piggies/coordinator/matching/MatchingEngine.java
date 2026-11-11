package co.inter.piggies.coordinator.matching;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

/**
 * Regra do leilão. Dono: Fernando (Dev 3), ver docs/passo-a-passo/dev-3-matching-engine.md.
 *
 * PROVISÓRIO: casa com a primeira contraparte compatível, sem prioridade de preço nem desempate.
 * Existe para a orquestração já funcionar de ponta a ponta. O Fernando troca o corpo de match()
 * mantendo a assinatura, que é o contrato entre o engine e a orquestração.
 */
public class MatchingEngine {

    /**
     * @param incoming      a oferta que acabou de chegar
     * @param counterOffers ofertas OPEN do lado oposto
     */
    public Optional<Match> match(BookOffer incoming, List<BookOffer> counterOffers) {
        return counterOffers.stream()
                .map(other -> incoming.side() == Side.BUY ? pair(incoming, other) : pair(other, incoming))
                .filter(pair -> pair.buy().price().compareTo(pair.sell().price()) >= 0)
                .findFirst()
                .map(pair -> new Match(pair.buy(), pair.sell(), average(pair.buy().price(), pair.sell().price())));
    }

    /** Dividir por 2 sempre dá um decimal exato, então não há arredondamento. */
    static BigDecimal average(BigDecimal buyPrice, BigDecimal sellPrice) {
        return buyPrice.add(sellPrice).divide(BigDecimal.TWO);
    }

    private static Pair pair(BookOffer buy, BookOffer sell) {
        return new Pair(buy, sell);
    }

    private record Pair(BookOffer buy, BookOffer sell) {
    }
}

package co.inter.piggies.coordinator.matching;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class MatchingEngineTest {

    private static final Instant BASE_TIME = Instant.parse("2026-01-01T10:00:00Z");

    private final MatchingEngine matchingEngine = new MatchingEngine();

    @Test
    void shouldReproduceTheAuctionExample() {
        BookOffer sellerA = offer("sell-A", Side.SELL, "A", "20", 0);
        BookOffer buyerB = offer("buy-B", Side.BUY, "B", "10", 1);
        BookOffer buyerC = offer("buy-C", Side.BUY, "C", "30", 2);

        Optional<Match> firstAttempt = matchingEngine.match(buyerB, List.of(sellerA));
        Optional<Match> secondAttempt = matchingEngine.match(buyerC, List.of(sellerA));

        assertThat(firstAttempt).isEmpty();
        assertMatch(secondAttempt, buyerC, sellerA, "25");
    }

    @Test
    void shouldMatchWhenBuyAndSellPricesAreEqual() {
        BookOffer sell = offer("sell-A", Side.SELL, "A", "20", 0);
        BookOffer buy = offer("buy-B", Side.BUY, "B", "20", 1);

        Optional<Match> result = matchingEngine.match(buy, List.of(sell));

        assertMatch(result, buy, sell, "20");
    }

    @Test
    void shouldSelectTheCheapestSellForAnIncomingBuy() {
        BookOffer expensiveSell = offer("sell-20", Side.SELL, "seller-20", "20", 0);
        BookOffer cheapestSell = offer("sell-18", Side.SELL, "seller-18", "18", 1);
        BookOffer buy = offer("buy-30", Side.BUY, "buyer-30", "30", 2);

        Optional<Match> result = matchingEngine.match(
                buy,
                List.of(expensiveSell, cheapestSell)
        );

        assertMatch(result, buy, cheapestSell, "24");
    }

    @Test
    void shouldSelectTheHighestBuyForAnIncomingSell() {
        BookOffer lowerBuy = offer("buy-25", Side.BUY, "buyer-25", "25", 0);
        BookOffer highestBuy = offer("buy-30", Side.BUY, "buyer-30", "30", 1);
        BookOffer sell = offer("sell-20", Side.SELL, "seller-20", "20", 2);

        Optional<Match> result = matchingEngine.match(
                sell,
                List.of(lowerBuy, highestBuy)
        );

        assertMatch(result, highestBuy, sell, "25");
    }

    @Test
    void shouldSelectTheOldestSellWhenPricesAreEqual() {
        BookOffer oldestSell = offer("sell-oldest", Side.SELL, "seller-oldest", "20", 0);
        BookOffer newestSell = offer("sell-newest", Side.SELL, "seller-newest", "20", 1);
        BookOffer buy = offer("buy-30", Side.BUY, "buyer-30", "30", 2);

        Optional<Match> result = matchingEngine.match(
                buy,
                List.of(newestSell, oldestSell)
        );

        assertMatch(result, buy, oldestSell, "25");
    }

    @Test
    void shouldSelectTheOldestBuyWhenPricesAreEqual() {
        BookOffer oldestBuy = offer("buy-oldest", Side.BUY, "buyer-oldest", "30", 0);
        BookOffer newestBuy = offer("buy-newest", Side.BUY, "buyer-newest", "30", 1);
        BookOffer sell = offer("sell-20", Side.SELL, "seller-20", "20", 2);

        Optional<Match> result = matchingEngine.match(
                sell,
                List.of(newestBuy, oldestBuy)
        );

        assertMatch(result, oldestBuy, sell, "25");
    }

    @Test
    void shouldNotMatchWhenThereAreNoCounterOffers() {
        BookOffer buy = offer("buy-30", Side.BUY, "buyer-30", "30", 0);

        Optional<Match> result = matchingEngine.match(buy, List.of());

        assertThat(result).isEmpty();
    }

    @Test
    void shouldNotMatchWhenNoCounterOfferHasACompatiblePrice() {
        BookOffer sell = offer("sell-20", Side.SELL, "seller-20", "20", 0);
        BookOffer buy = offer("buy-10", Side.BUY, "buyer-10", "10", 1);

        Optional<Match> result = matchingEngine.match(buy, List.of(sell));

        assertThat(result).isEmpty();
    }

    @ParameterizedTest(name = "buy {0} and sell {1} should execute at {2}")
    @CsvSource({
            "30, 20, 25",
            "10.02, 10.01, 10.015"
    })
    void shouldCalculateTheExactAveragePrice(
            String buyPrice,
            String sellPrice,
            String expectedPrice
    ) {
        BookOffer sell = offer("sell", Side.SELL, "seller", sellPrice, 0);
        BookOffer buy = offer("buy", Side.BUY, "buyer", buyPrice, 1);

        Optional<Match> result = matchingEngine.match(buy, List.of(sell));

        assertMatch(result, buy, sell, expectedPrice);
    }

    private static BookOffer offer(
            String offerKey,
            Side side,
            String userId,
            String price,
            long secondsAfterBase
    ) {
        return new BookOffer(
                UUID.nameUUIDFromBytes(offerKey.getBytes(StandardCharsets.UTF_8)),
                side,
                userId,
                new BigDecimal(price),
                BASE_TIME.plusSeconds(secondsAfterBase)
        );
    }

    private static void assertMatch(
            Optional<Match> result,
            BookOffer expectedBuy,
            BookOffer expectedSell,
            String expectedPrice
    ) {
        assertThat(result).isPresent();

        Match match = result.orElseThrow();
        assertThat(match.buy()).isEqualTo(expectedBuy);
        assertThat(match.sell()).isEqualTo(expectedSell);
        assertThat(match.price()).isEqualByComparingTo(expectedPrice);
    }
}

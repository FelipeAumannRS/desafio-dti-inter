package co.inter.piggies.coordinator.auction;

import co.inter.piggies.coordinator.book.BookOfferEntity;
import co.inter.piggies.coordinator.book.BookOfferRepository;
import co.inter.piggies.coordinator.book.BookOfferStatus;
import co.inter.piggies.coordinator.event.CoordinatorProducer;
import co.inter.piggies.coordinator.event.ExchangeRatePublished;
import co.inter.piggies.coordinator.event.OfferCreated;
import co.inter.piggies.coordinator.event.Topics;
import co.inter.piggies.coordinator.event.TransactionExecuted;
import co.inter.piggies.coordinator.matching.BookOffer;
import co.inter.piggies.coordinator.matching.Match;
import co.inter.piggies.coordinator.matching.MatchingEngine;
import co.inter.piggies.coordinator.matching.Side;
import co.inter.piggies.coordinator.trade.Trade;
import co.inter.piggies.coordinator.trade.TradeRepository;
import co.inter.piggies.coordinator.wallet.Wallet;
import co.inter.piggies.coordinator.wallet.WalletRepository;
import jakarta.inject.Singleton;
import jakarta.transaction.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/** Orquestração do leilão: registra a oferta, procura o match e liquida a transação. */
@Singleton
public class AuctionService {

    private final BookOfferRepository offers;
    private final WalletRepository wallets;
    private final TradeRepository trades;
    private final CoordinatorProducer producer;
    private final MatchingEngine engine = new MatchingEngine();

    public AuctionService(BookOfferRepository offers, WalletRepository wallets, TradeRepository trades,
                          CoordinatorProducer producer) {
        this.offers = offers;
        this.wallets = wallets;
        this.trades = trades;
        this.producer = producer;
    }

    /**
     * Tudo numa transação: a oferta entra no livro, casa e as carteiras mudam juntas, ou nada muda.
     * Devolve a transação executada, se houve match.
     */
    @Transactional
    public Optional<Trade> process(OfferCreated event) {
        if (offers.existsById(event.offerId())) {
            return Optional.empty();   // o Kafka entrega pelo menos uma vez: a mesma oferta pode chegar de novo
        }
        BookOfferEntity incoming = offers.save(BookOfferEntity.open(event));

        Side counterSide = incoming.toBookOffer().side() == Side.BUY ? Side.SELL : Side.BUY;
        List<BookOffer> counterOffers = offers.findBySideAndStatus(counterSide.name(), BookOfferStatus.OPEN).stream()
                .map(BookOfferEntity::toBookOffer)
                .toList();

        return engine.match(incoming.toBookOffer(), counterOffers).map(this::settle);
    }

    private Trade settle(Match match) {
        markMatched(match.buy().offerId());
        markMatched(match.sell().offerId());
        wallet(match.sell().userId()).debit();
        wallet(match.buy().userId()).credit();

        Instant now = Instant.now();
        Trade trade = trades.save(Trade.of(match, now));
        BigDecimal price = plain(trade.getPrice());

        producer.transactionExecuted(trade.getTransactionId().toString(), new TransactionExecuted(UUID.randomUUID(),
                trade.getTransactionId(), trade.getBuyOfferId(), trade.getSellOfferId(), trade.getBuyerId(),
                trade.getSellerId(), price, 1, now));
        producer.exchangeRatePublished(Topics.MARKET_KEY, new ExchangeRatePublished(UUID.randomUUID(),
                trade.getTransactionId(), ExchangeRatePublished.PAIR, price, now));
        return trade;
    }

    private void markMatched(UUID offerId) {
        BookOfferEntity offer = offers.findById(offerId).orElseThrow();
        offer.setStatus(BookOfferStatus.MATCHED);
        offers.update(offer);
    }

    /** A carteira fica gerenciada pelo JPA nesta transação: o débito e o crédito são gravados no commit. */
    private Wallet wallet(String userId) {
        return wallets.findById(userId).orElseGet(() -> wallets.save(new Wallet(userId)));
    }

    /** O banco guarda 4 casas (25.0000); no evento vai o valor sem zeros à direita (25). */
    private static BigDecimal plain(BigDecimal value) {
        BigDecimal stripped = value.stripTrailingZeros();
        return stripped.scale() < 0 ? stripped.setScale(0) : stripped;
    }
}

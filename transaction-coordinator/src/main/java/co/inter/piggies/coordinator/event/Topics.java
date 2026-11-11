package co.inter.piggies.coordinator.event;

/** Tópicos e chaves do contrato (contracts/README.md). */
public final class Topics {

    public static final String OFFERS_CREATED = "piggies.offers.created";
    public static final String TRANSACTIONS_EXECUTED = "piggies.transactions.executed";
    public static final String EXCHANGE_RATE_PUBLISHED = "piggies.exchange-rate.published";

    /** Chave constante das ofertas e da taxa: tudo na mesma partição, processado em ordem. */
    public static final String MARKET_KEY = "PIGGY-USD";

    private Topics() {
    }
}

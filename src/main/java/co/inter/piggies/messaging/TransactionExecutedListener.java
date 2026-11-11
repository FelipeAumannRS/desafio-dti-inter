@KafkaListener(groupId = "seller-service", offsetReset = OffsetReset.EARLIEST)
public class TransactionExecutedListener {
    @Topic("piggies.transactions.executed")
    void on(TransactionExecuted event) {
        // busca event.sellOfferId(); se existir, offer.execute(...) e salva
    }
}
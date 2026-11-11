package co.inter.piggies.coordinator.wallet;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class WalletTest {

    @Test
    void newWalletStartsAtZero() {
        assertThat(new Wallet("seller-A").getPiggies()).isZero();
    }

    @Test
    void debitAndCreditMoveOnePiggy() {
        Wallet seller = new Wallet("seller-A");
        Wallet buyer = new Wallet("buyer-C");

        seller.debit();
        buyer.credit();

        assertThat(seller.getPiggies()).isEqualTo(-1);
        assertThat(buyer.getPiggies()).isEqualTo(1);
    }
}

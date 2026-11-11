package co.inter.piggies.coordinator.wallet;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * Saldo de Piggies de um usuário.
 * Versão simplificada do desafio: não validamos saldo, então uma carteira nova começa em 0 e pode ficar negativa.
 */
@Entity
@Table(name = "wallets")
@Getter
@NoArgsConstructor
public class Wallet {

    @Id
    private String userId;

    private long piggies;

    public Wallet(String userId) {
        this.userId = userId;
    }

    /** Toda oferta é de 1 Piggy. */
    public void debit() {
        piggies -= 1;
    }

    public void credit() {
        piggies += 1;
    }
}

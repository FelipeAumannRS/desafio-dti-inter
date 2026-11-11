package co.inter.piggies.coordinator.wallet;

import io.micronaut.data.annotation.Repository;
import io.micronaut.data.repository.CrudRepository;

@Repository
public interface WalletRepository extends CrudRepository<Wallet, String> {
}

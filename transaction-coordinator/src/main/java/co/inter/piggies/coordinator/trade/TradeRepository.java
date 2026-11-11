package co.inter.piggies.coordinator.trade;

import io.micronaut.data.annotation.Repository;
import io.micronaut.data.repository.CrudRepository;

import java.util.UUID;

@Repository
public interface TradeRepository extends CrudRepository<Trade, UUID> {
}

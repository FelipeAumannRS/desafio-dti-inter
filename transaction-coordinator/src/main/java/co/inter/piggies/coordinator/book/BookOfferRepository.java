package co.inter.piggies.coordinator.book;

import io.micronaut.data.annotation.Repository;
import io.micronaut.data.repository.CrudRepository;

import java.util.List;
import java.util.UUID;

@Repository
public interface BookOfferRepository extends CrudRepository<BookOfferEntity, UUID> {

    /** As ofertas abertas de um lado: é nelas que o matching engine procura a contraparte. */
    List<BookOfferEntity> findBySideAndStatus(String side, BookOfferStatus status);
}

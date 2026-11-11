package co.inter.piggies.buyer.service;

import co.inter.piggies.buyer.domain.BuyOffer;
import co.inter.piggies.buyer.domain.BuyOfferRequestDTO;
import co.inter.piggies.buyer.domain.BuyOfferResponseDTO;
import co.inter.piggies.buyer.kafka.events.OfferCreatedEventDTO;
import co.inter.piggies.buyer.kafka.producer.OfferCreatedProducer;
import co.inter.piggies.buyer.repository.BuyOfferRepository;
import io.micronaut.http.HttpStatus;
import io.micronaut.http.annotation.Body;
import io.micronaut.problem.HttpStatusType;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import org.zalando.problem.Problem;
import org.zalando.problem.ThrowableProblem;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.util.UUID;

/*
 *-----------------------------------------------------------------------*
 * @Singleton registra a classe como um bean do Micronaut com uma única  *
 * instância compartilhada por toda a aplicação. É o que permite injetar *
 * este service no controller (via @Inject ou construtor). Por ser       *
 * compartilhado, não guarde estado de requisição em campos da classe.   *
 *-----------------------------------------------------------------------*
 */
@Singleton
public class BuyOfferService {

    /*
     *-------------------------------------------------------------------------*
     * @Inject: o Micronaut cria o repository e o injeta neste campo.          *
     * O campo não pode ser private nem final (o Micronaut evita reflection).  *
     * Alternativa: receber o repository pelo construtor (permite usar final). *
     *-------------------------------------------------------------------------*
     */
    @Inject
    BuyOfferRepository buyOfferRepository;

    @Inject
    OfferCreatedProducer offerCreatedProducer;

    /*
     *--------------------------------------------------------------------*
     * Metodo: getBuyOfferById                                            *
     *--------------------------------------------------------------------*
     * Descrição: Função para buscar uma BuyOffer pelo seu id se ele      *
     *            existir.                                                *
     *--------------------------------------------------------------------*
     * Params:  UUID id                                               *
     * Returns: BuyOfferResponseDTO                                       *
     *--------------------------------------------------------------------*
     */

    public BuyOfferResponseDTO getBuyOfferById(UUID id) {

        // Busca a entidade no banco de dados via id e caso a mesma não seja encontrada
        // lança um Problem 404 com a mensagem de não existente para aquele id
        BuyOffer buyOffer = buyOfferRepository.findById(id)
                .orElseThrow(() -> notFound(id));

        // Retorna a conversão da Entity em um ResponseDTO
        return BuyOfferResponseDTO.from(buyOffer);
    }


    /*
     *--------------------------------------------------------------------*
     * Metodo: createBuyOffer                                             *
     *--------------------------------------------------------------------*
     * Descrição: Função para criar uma BuyOffer com os dados passados    *
     *            via body da requisição                                  *
     *--------------------------------------------------------------------*
     * Params:  BuyOfferRequestDTO body                                   *
     * Returns: BuyOfferResponseDTO                                       *
     *--------------------------------------------------------------------*
     */

    @Transactional
    public BuyOfferResponseDTO createBuyOffer(@Valid @Body BuyOfferRequestDTO body) {

        // Converte o Body que é um RequestDTO em uma Entity
        BuyOffer buyOffer = new BuyOffer();
        buyOffer.setId(UUID.randomUUID());
        buyOffer.setPrice(new BigDecimal(body.price()).setScale(2, RoundingMode.HALF_UP));
        buyOffer.setStatus("OPEN");
        buyOffer.setCreatedAt(OffsetDateTime.now());

        // Salvando Entity no banco e recebendo os dados que foram salvos
        BuyOffer buyOfferSaved = buyOfferRepository.save(buyOffer);

        // Publica o evento OfferCreated no Kafka (chave PIGGY-USD)
        OfferCreatedEventDTO event = new OfferCreatedEventDTO(
                UUID.randomUUID().toString(),
                buyOfferSaved.getId().toString(),
                "BUY",
                buyOfferSaved.getPrice().toPlainString(),
                buyOfferSaved.getCreatedAt().toString()
        );

        offerCreatedProducer.send("PIGGY-USD", event);

        // Retorna a conversão da Entidade que foi salva em um ResponseDTO
        return BuyOfferResponseDTO.from(buyOfferSaved);
    }

    /*
     *--------------------------------------------------------------------*
     * Metodo auxiliar: notFound                                          *
     *--------------------------------------------------------------------*
     * Descrição: Monta o Problem 404 lançado quando a BuyOffer não       *
     *            existe. Centraliza a mensagem para os 3 métodos.        *
     *            O detail aparece no body da resposta (Postman).         *
     *--------------------------------------------------------------------*
     * Params:  UUID id                                               *
     * Returns: ThrowableProblem                                          *
     *--------------------------------------------------------------------*
     */

    private ThrowableProblem notFound(UUID id) {
        return Problem.builder()
                .withStatus(new HttpStatusType(HttpStatus.NOT_FOUND))
                .withTitle("Não encontrado")
                .withDetail("BuyOffer não existente para o id: " + id)
                .build();
    }
}

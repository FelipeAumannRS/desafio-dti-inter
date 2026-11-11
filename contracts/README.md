# Contratos do SLP

Contract-first: estes arquivos são a fonte da verdade. Cada serviço implementa o seu contrato; mudança de contrato se combina com o time antes de mudar código.

| Serviço | Contrato |
|---|---|
| PiggiesBuyerService | `piggies-buyer-service.openapi.yaml` + publica `events/offer-created.schema.json` (side=BUY) |
| PiggiesSellerService | `piggies-seller-service.openapi.yaml` + publica `events/offer-created.schema.json` (side=SELL) |
| PiggiesTransactionCoordinator | publica `events/transaction-executed.schema.json` e `events/exchange-rate-published.schema.json` |

## Fluxo

1. Cliente faz `POST /buy-offers` ou `POST /sell-offers`. O serviço grava a oferta como `OPEN`, publica `OfferCreated` e responde `202 Accepted` na hora, com `Location` para consulta.
2. O Coordinator consome `OfferCreated`, guarda a oferta no livro e procura match:
   - oferta de compra casa com a venda aberta de **menor** preço que seja `<=` ao preço de compra;
   - oferta de venda casa com a compra aberta de **maior** preço que seja `>=` ao preço de venda;
   - empate de preço: a oferta mais antiga primeiro.
3. Com match, o Coordinator executa pelo valor médio `(compra + venda) / 2`, debita 1 Piggy do vendedor, credita 1 Piggy ao comprador e publica `TransactionExecuted` e `ExchangeRatePublished`.
4. Buyer e Seller consomem `TransactionExecuted` e marcam a sua oferta como `EXECUTED`. O cliente vê isso no `GET /buy-offers/{id}` ou `GET /sell-offers/{id}`.

## Tópicos Kafka

| Tópico | Evento | Produtor | Consumidores | Chave |
|---|---|---|---|---|
| `piggies.offers.created` | OfferCreated | Buyer, Seller | Coordinator | `PIGGY-USD` |
| `piggies.transactions.executed` | TransactionExecuted | Coordinator | Buyer, Seller | `transactionId` |
| `piggies.exchange-rate.published` | ExchangeRatePublished | Coordinator | outros sistemas | `PIGGY-USD` |

Todas as ofertas usam a mesma chave, então caem na mesma partição e o Coordinator as processa uma de cada vez, na ordem de chegada. Isso evita que duas ofertas casem com a mesma contraparte.

## Regras simplificadas

- Toda oferta é de 1 Piggy; não existe match parcial.
- Não há cancelamento nem expiração.
- Preços em USD por 1 Piggy. A média de dois valores com duas casas cabe exatamente em três casas, então não há arredondamento.

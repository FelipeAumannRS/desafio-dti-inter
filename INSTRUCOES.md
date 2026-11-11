## Instructions
 
No Inter trabalhamos com `contract-first`. O primeiro passo de qualquer entrega é
definir os contratos entre os microserviços usando OpenAPI ou JSON Schema — um contrato
por serviço, definido antes da implementação. Eventos publicados (ex.: Kafka) também
devem ter schema.
 
Os clientes do Inter não gostam de ficar olhando para uma ampulheta na tela, por
isso todo serviço de borda precisa trabalhar de forma assíncrona.
 
Testes unitários e de integração são obrigatórios.
 
Planejem as entregas. Tempo é um recurso limitado e a gestão do tempo é parte do desafio.
 
Trabalhem em equipe. Vocês não estão competindo entre si. Vocês vencem juntos ou perdem juntos.
 
Uma entrega parcial que faz alguma coisa vale mais do que um monte de código que não
realiza nada.
 
Vocês podem usar IA, consultar a internet e usar qualquer outra ferramenta à sua
disposição, desde que tudo seja anexado ao projeto e comitado (pesquisas, prompts,
códigos de referência, etc.).
 
Contudo, usem com responsabilidade:
- Vocês precisarão resolver bugs e responder questões sobre a implementação. Responder
  “Não sei, a IA que fez assim” é critério de eliminação.
- Fiquem atentos quanto a propriedade intelectual e nunca utilizem códigos ou documentos
  que vocês não estão autorizados a publicar.




## SLP — Sistema de Leilão de Porquinhos
 
Piggie é a nova stablecoin do Inter. O desafio é implementar o Sistema de Leilão de Porquinhos (SLP).
 
Um leilão envolve algumas etapas:
 
1. Compradores criam ofertas informando quantos dólares USD estão dispostos a pagar por 1 Piggy
2. Vendedores criam ofertas informando por quantos dólares USD estão dispostos a vender 1 Piggy
3. Sempre que uma oferta de compra e uma oferta de venda dão match, a operação é executada pelo valor médio das duas ofertas e esse valor é publicado para outros sistemas.
 
Há match quando o preço de compra é maior ou igual ao preço de venda.
 
Exemplo:
- Vendedor A cria oferta de $20 por 1 Piggy
- Comprador B cria oferta de $10 por 1 Piggy
- Não há match. Comprador dispõe pagar menos do que o vendedor exige.
- Comprador C cria oferta de $30 por 1 Piggy
- As ofertas do vendedor A e do comprador C dão match no valor de $25 por 1 Piggy. Piggy é debitado do vendedor, creditado ao comprador e taxa de câmbio de $25/Piggy é publicada.
 
**IMPORTANTE** nessa dinâmica vamos implementar uma versão simplificada do sistema:
- Ofertas são sempre para 1 Piggy
- Nunca há match parcial
- Não há cancelamento ou expiração de ofertas
 
Vocês devem implementar 3 serviços:
 
- **PiggiesBuyerService** — ponto de entrada para ofertas de compra; cria ofertas e atualiza o status quando houver uma transação.
- **PiggiesSellerService** — ponto de entrada para ofertas de venda; cria ofertas e atualiza o status quando houver uma transação.
- **PiggiesTransactionCoordinator** — identifica matches, executa débito/crédito de Piggies e publica a nova taxa de câmbio (ex.: via Kafka).
 
Fluxo sugerido: Buyer/Seller registram a oferta → Coordinator detecta match → Coordinator orquestra a transação e notifica Buyer/Seller para atualizar o status.
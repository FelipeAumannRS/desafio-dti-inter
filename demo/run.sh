#!/usr/bin/env bash
# Demo do SLP com o exemplo do enunciado. Rode da raiz com o Coordinator no ar.
# As ofertas entram direto no tópico piggies.offers.created, no lugar do Buyer e do Seller,
# no formato do contrato (contracts/events/offer-created.schema.json).
set -euo pipefail

NET=$(docker inspect piggies-kafka --format '{{range $k, $v := .NetworkSettings.Networks}}{{$k}}{{end}}')
kafka() { MSYS_NO_PATHCONV=1 docker run --rm -i --network "$NET" apache/kafka:4.3.1 /opt/kafka/bin/"$@"; }
psql() { docker exec piggies-postgres psql -U piggies -d piggies "$@"; }
uuid() { python -c 'import uuid; print(uuid.uuid4())'; }

offer() {   # offer SIDE USER PRICE
  printf 'PIGGY-USD|{"eventId":"%s","offerId":"%s","side":"%s","userId":"%s","price":%s,"createdAt":"%s"}\n' \
    "$(uuid)" "$(uuid)" "$1" "$2" "$3" "$(date -u +%Y-%m-%dT%H:%M:%SZ)" \
    | kafka kafka-console-producer.sh --bootstrap-server kafka:19092 --topic piggies.offers.created \
        --property parse.key=true --property key.separator='|'
}

echo "1) Vendedor A cria oferta de \$20 por 1 Piggy";   offer SELL vendedor-A 20;   sleep 2
echo "2) Comprador B cria oferta de \$10 por 1 Piggy";  offer BUY comprador-B 10;   sleep 2
echo "   -> não há match: B paga menos do que A exige"
echo "3) Comprador C cria oferta de \$30 por 1 Piggy";  offer BUY comprador-C 30;   sleep 3
echo "   -> match A x C a (30 + 20) / 2 = \$25"
echo

echo "== Evento publicado: piggies.transactions.executed"
kafka kafka-console-consumer.sh --bootstrap-server kafka:19092 --topic piggies.transactions.executed \
  --from-beginning --timeout-ms 5000 --property print.key=true 2>/dev/null || true
echo "== Evento publicado: piggies.exchange-rate.published"
kafka kafka-console-consumer.sh --bootstrap-server kafka:19092 --topic piggies.exchange-rate.published \
  --from-beginning --timeout-ms 5000 --property print.key=true 2>/dev/null || true
echo

echo "== Postgres do Coordinator"
psql -c "select user_id as usuario, side as lado, price as preco, status from book_offers order by created_at"
psql -c "select seller_id as vendedor, buyer_id as comprador, price as preco from trades"
psql -c "select user_id as usuario, piggies from wallets order by user_id"
echo "Kafka UI: http://localhost:8081"

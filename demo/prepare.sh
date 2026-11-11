#!/usr/bin/env bash
# Deixa o ambiente limpo para a demo. Rode da raiz ANTES de subir o Coordinator.
#   1. Postgres e Kafka (e a Kafka UI) do docker-compose;
#   2. tópicos do contrato recriados vazios;
#   3. tabelas do Coordinator apagadas (o Hibernate recria quando ele subir).
set -euo pipefail

docker compose -p desafio-dti-inter --profile ui up -d

NET=$(docker inspect piggies-kafka --format '{{range $k, $v := .NetworkSettings.Networks}}{{$k}}{{end}}')
kafka() { MSYS_NO_PATHCONV=1 docker run --rm --network "$NET" apache/kafka:4.3.1 /opt/kafka/bin/"$@"; }

for topic in piggies.offers.created piggies.transactions.executed piggies.exchange-rate.published; do
  kafka kafka-topics.sh --bootstrap-server kafka:19092 --delete --if-exists --topic "$topic"
done
sleep 3   # o Kafka apaga os tópicos de forma assíncrona
./infra/create-kafka-topics.sh

./infra/reset-db.sh book_offers trades wallets

echo
echo "Pronto. Agora suba o Coordinator:  cd transaction-coordinator && mvn mn:run"
echo "Quando aparecer 'Startup completed', rode:  ./demo/run.sh"

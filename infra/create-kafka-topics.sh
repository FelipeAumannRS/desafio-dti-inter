#!/usr/bin/env bash
# Cria os 3 tópicos do contrato no Kafka do docker-compose. Rode da raiz, depois de `docker compose up -d`:
#   ./infra/create-kafka-topics.sh
# A imagem apache/kafka-native não traz as ferramentas de linha de comando,
# então a apache/kafka (mesma versão) entra só como cliente, na rede do compose.
set -euo pipefail

NET=$(docker inspect piggies-kafka --format '{{range $k, $v := .NetworkSettings.Networks}}{{$k}}{{end}}')
kafka() { MSYS_NO_PATHCONV=1 docker run --rm --network "$NET" apache/kafka:4.3.1 /opt/kafka/bin/"$@"; }
create() { kafka kafka-topics.sh --bootstrap-server kafka:19092 --create --if-not-exists --topic "$1" --partitions "$2" --replication-factor 1; }

# 1 partição: o Coordinator lê as ofertas uma de cada vez, na ordem de chegada.
create piggies.offers.created 1
create piggies.transactions.executed 3
create piggies.exchange-rate.published 1

kafka kafka-topics.sh --bootstrap-server kafka:19092 --describe --topic 'piggies\..*' | grep -E '^Topic:'

#!/usr/bin/env bash
# Exporta o schema atual do Postgres do docker-compose para docs/db/schema.sql.
# Rode da raiz depois de subir os apps: o git diff do arquivo mostra o que mudou nas tabelas.
set -euo pipefail

{
  echo "-- Schema gerado pelo Hibernate (hbm2ddl: update) a partir das @Entity, exportado do Postgres do docker-compose."
  echo "-- Atualize com ./infra/dump-schema.sh; para recriar as tabelas depois de mudar uma entidade, ./infra/reset-db.sh."
  echo
  docker exec piggies-postgres pg_dump -U piggies -d piggies --schema-only --no-owner --no-privileges \
    | tr -d '\r' \
    | grep -v -E '^--|^SET |^SELECT pg_catalog|^$' \
    | grep -v -F -e '\restrict' -e '\unrestrict'
} > docs/db/schema.sql

grep -E '^CREATE TABLE' docs/db/schema.sql

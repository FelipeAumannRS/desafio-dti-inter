#!/usr/bin/env bash
# Faz o Postgres do docker-compose acompanhar as @Entity.
#
# O hbm2ddl "update" cria tabelas e colunas novas sozinho, mas nunca renomeia, nunca muda tipo
# ou precisão e nunca apaga. Quando uma entidade muda desse jeito, apague as tabelas e suba o app
# de novo: o Hibernate recria tudo exatamente como a entidade está agora.
#
#   ./infra/reset-db.sh                      apaga todas as tabelas (schema public)
#   ./infra/reset-db.sh sell_offers trades   apaga só as tabelas listadas
#
# Depois: suba o(s) app(s) e rode ./infra/dump-schema.sh para atualizar docs/db/schema.sql.
set -euo pipefail

psql() { docker exec piggies-postgres psql -U piggies -d piggies -v ON_ERROR_STOP=1 -q "$@"; }

if [ $# -eq 0 ]; then
  psql -c 'drop schema public cascade' -c 'create schema public'
  echo "Todas as tabelas apagadas. Suba os apps para o Hibernate recriar."
else
  for table in "$@"; do
    psql -c "drop table if exists \"$table\" cascade"
  done
  echo "Tabelas apagadas: $*. Suba o app dono delas para o Hibernate recriar."
fi

-- Schema gerado pelo Hibernate (hbm2ddl: update) a partir das @Entity, exportado do Postgres do docker-compose.
-- Atualize com ./infra/dump-schema.sh; para recriar as tabelas depois de mudar uma entidade, ./infra/reset-db.sh.

COMMENT ON SCHEMA public IS '';
CREATE TABLE public.book_offers (
    offer_id uuid NOT NULL,
    created_at timestamp(6) with time zone,
    price numeric(19,4),
    side character varying(255),
    status character varying(255),
    user_id character varying(255),
    CONSTRAINT book_offers_status_check CHECK (((status)::text = ANY ((ARRAY['OPEN'::character varying, 'MATCHED'::character varying])::text[])))
);
CREATE TABLE public.sell_offers (
    id uuid NOT NULL,
    created_at timestamp(6) with time zone,
    executed_at timestamp(6) with time zone,
    executed_price numeric(38,2),
    price numeric(38,2),
    seller_id character varying(255),
    status character varying(255),
    transaction_id uuid,
    CONSTRAINT sell_offers_status_check CHECK (((status)::text = ANY ((ARRAY['OPEN'::character varying, 'EXECUTED'::character varying])::text[])))
);
CREATE TABLE public.trades (
    transaction_id uuid NOT NULL,
    buy_offer_id uuid,
    buyer_id character varying(255),
    executed_at timestamp(6) with time zone,
    price numeric(19,4),
    sell_offer_id uuid,
    seller_id character varying(255)
);
CREATE TABLE public.wallets (
    user_id character varying(255) NOT NULL,
    piggies bigint NOT NULL
);
ALTER TABLE ONLY public.book_offers
    ADD CONSTRAINT book_offers_pkey PRIMARY KEY (offer_id);
ALTER TABLE ONLY public.sell_offers
    ADD CONSTRAINT sell_offers_pkey PRIMARY KEY (id);
ALTER TABLE ONLY public.trades
    ADD CONSTRAINT trades_pkey PRIMARY KEY (transaction_id);
ALTER TABLE ONLY public.wallets
    ADD CONSTRAINT wallets_pkey PRIMARY KEY (user_id);

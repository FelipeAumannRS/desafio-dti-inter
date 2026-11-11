# Desafio DTI Inter

Repositório da dinâmica de live code do Inter. O serviço é um microsserviço Micronaut no pacote `co.inter.piggies`.

## Stack

- Java 25
- Micronaut 5.1 (Netty, YAML, validação, Problem JSON, Management)
- Maven 3.9
- Micronaut Data JPA (Hibernate) e Jakarta Data, com PostgreSQL 16
- Kafka
- Testes com JUnit 5, AssertJ, Awaitility e Testcontainers
- Lombok

## Pré-requisitos

- Conta no GitHub
- [mise](https://mise.jdx.dev/installing-mise.html)
- [Docker](https://docs.docker.com/engine/install), com o engine rodando (os testes sobem containers)
- Java, Maven e Micronaut CLI, instalados pelo mise:

```bash
mise use java@25
mise use maven@3.9
mise use micronaut@5.1
```

- Imagens usadas pelos Testcontainers:

```bash
docker pull postgres:16-alpine
docker pull apache/kafka-native:4.3.1
```

## Geração do serviço

```bash
mn create-app --inplace --build=maven --jdk=25 --lang=java --test=junit \
  --features=yaml,data-jpa,hibernate-jpa,hibernate-jpamodelgen,jakarta-data,postgres,testcontainers,assertj,awaitility,junit-params,lombok,logback,management,kafka,netty-server,jspecify,micronaut-http-validation,problem-json,validation \
  co.inter.piggies.piggybank
```

## Testes

```bash
mvn test
```

PostgreSQL e Kafka sobem em containers pelo Testcontainers, então o Docker precisa estar no ar.

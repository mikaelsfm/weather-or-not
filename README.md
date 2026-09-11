# Weather or Not

Backend inicial em Java 21 e Quarkus 3.33 LTS. É um monólito modular preparado
para extrações futuras, sem introduzir a complexidade operacional de microserviços no MVP.

## O que já existe

- CRUD REST de compromissos em `/appointments`;
- compromissos únicos e recorrentes;
- PostgreSQL via Docker Compose;
- motor de recomendação puro, com testes para condições normais e chuva forte.

## Executar localmente

É necessário Java 21+ e Maven 3.9+.

```bash
cd backend
docker compose up -d postgres
mvn quarkus:dev
```

Exemplo para criar um compromisso recorrente:

```bash
curl -X POST http://localhost:8080/appointments \
  -H 'Content-Type: application/json' \
  -d '{
    "name":"Faculdade", "type":"Aula", "startTime":"19:00",
    "locationName":"Universidade", "preparationMinutes":30,
    "travelMinutes":30, "safetyMarginMinutes":10,
    "recurring":true, "recurringDays":["MONDAY","WEDNESDAY"]
  }'
```

Para executar os testes:

```bash
cd backend
mvn test
```

## Frontend

O cliente web vive em [`frontend/`](frontend/README.md), no mesmo repositório, para
manter o MVP simples e o contrato da API próximo de quem o consome. Ele usa React e
consome o CRUD disponível em `/appointments`.

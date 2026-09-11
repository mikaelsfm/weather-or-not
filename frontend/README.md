# Frontend — Weather or Not

Cliente React/TypeScript do backend Quarkus. No desenvolvimento, o Vite encaminha
as chamadas para `/appointments` ao backend em `http://localhost:8080`.

## Executar

Em um terminal, inicie o backend e o PostgreSQL:

```bash
cd backend
docker compose up -d postgres
mvn quarkus:dev
```

Em outro terminal:

```bash
cd frontend
npm install
npm run dev
```

Abra o endereço exibido pelo Vite (normalmente `http://localhost:5173`).

Para apontar o cliente a uma API já publicada, defina `VITE_API_URL` com a URL
base da API antes do build. Nesse cenário, o Quarkus deve liberar essa origem em CORS.

## Escopo atual

O backend atual disponibiliza somente o CRUD de compromissos. Por isso, esta versão
inclui lista, cadastro, edição e exclusão de compromissos, além de mostrar horários
normais de preparação e saída calculados no cliente. Clima, impacto e recomendações
dinâmicas dependem de endpoints que ainda não existem no backend.

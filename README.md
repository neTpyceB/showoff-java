# showoff-java

### Start all services

```bash
docker compose up -d
```

Services:
- `app` (Spring API): `http://localhost:8080`
- `postgres`: `localhost:5432`
- `redis` (future-ready): `localhost:6379`
- `rabbitmq` (future-ready): `localhost:5672`, management UI `http://localhost:15672`

### Verify app and DB connectivity

```bash
docker compose ps
docker compose logs -f app
```

The app logs should show Flyway migration execution at startup.

### Verify migrations and table creation

```bash
docker compose exec postgres psql -U incidentops -d incidentops -c "\dt"
docker compose exec postgres psql -U incidentops -d incidentops -c "select * from flyway_schema_history order by installed_rank;"
```

Expected table:
- `incident_tickets`

### Verify CRUD endpoint behavior

Create:

```bash
curl -sS -X POST http://localhost:8080/api/v4/tickets \
  -H "Content-Type: application/json" \
  -d '{"serviceId":"payments-api","severity":4,"summary":"queue delay"}'
```

Get by id:

```bash
curl -sS http://localhost:8080/api/v4/tickets/TKT-5001
```

Paginated listing:

```bash
curl -sS "http://localhost:8080/api/v4/tickets?status=OPEN&page=0&size=20"
```

Search with query + pagination:

```bash
curl -sS "http://localhost:8080/api/v4/tickets/search?serviceId=payments-api&minSeverity=3&page=0&size=20"
```

### Stop environment

```bash
docker compose down
```

Also remove volumes (if you want a clean DB):

```bash
docker compose down -v
```

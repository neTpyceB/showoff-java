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

### Profile files

- `application.properties`: shared/base settings.
- `application-dev.properties`: developer defaults.
- `application-docker.properties`: Docker Compose profile.
- `application-prod.properties`: production profile (env-driven, no defaults for secrets).

### Activate profile

```bash
SPRING_PROFILES_ACTIVE=dev ./gradlew runIncidentApi
SPRING_PROFILES_ACTIVE=docker ./gradlew runIncidentApi
SPRING_PROFILES_ACTIVE=prod ./gradlew runIncidentApi
```

Docker Compose sets:
- `SPRING_PROFILES_ACTIVE=docker`

### Secrets via environment variables

Required in non-dev environments:
- `POSTGRES_HOST`
- `POSTGRES_PORT`
- `POSTGRES_DB`
- `POSTGRES_USER`
- `POSTGRES_PASSWORD`
- `REDIS_HOST`
- `RABBITMQ_HOST`
- `INCIDENTOPS_API_KEY`
- `INCIDENTOPS_SIGNING_SECRET`

Example (prod-like local run):

```bash
POSTGRES_HOST=localhost \
POSTGRES_PORT=5432 \
POSTGRES_DB=incidentops \
POSTGRES_USER=incidentops \
POSTGRES_PASSWORD=incidentops \
REDIS_HOST=localhost \
RABBITMQ_HOST=localhost \
INCIDENTOPS_API_KEY=replace-me \
INCIDENTOPS_SIGNING_SECRET=replace-me-too \
SPRING_PROFILES_ACTIVE=prod \
./gradlew runIncidentApi
```

Repository integration tests run against a real PostgreSQL container with no manual DB setup.

Run:

```bash
./gradlew test --tests '*IncidentTicketRepositoryTestcontainersTest'
```

What happens:
- Testcontainers starts PostgreSQL automatically.
- Spring datasource and Flyway properties are injected dynamically.
- Flyway migrations run inside that test database.
- Repository tests execute against real Postgres behavior.

Caching is enabled through Spring Cache abstraction.

- In `docker` and `prod` profiles: `spring.cache.type=redis`
- In tests/dev: `spring.cache.type=simple`

Cached operations:
- `IncidentTicketService#getByTicketId` uses cache `ticketsById`
- paginated status reads use cache `ticketPagesByStatus`
- service/severity search uses cache `ticketPagesByServiceSeverity`

Eviction and refresh:
- `create` clears page/search caches and puts created ticket into `ticketsById`
- `updateStatus` clears page/search caches and refreshes `ticketsById`

Quick check:

```bash
docker compose up -d
curl -sS -X POST http://localhost:8080/api/v4/tickets \
  -H "Content-Type: application/json" \
  -d '{"serviceId":"payments-api","severity":4,"summary":"queue delay"}'
curl -sS http://localhost:8080/api/v4/tickets/TKT-5001
curl -sS http://localhost:8080/api/v4/tickets/TKT-5001
```

Kafka messaging basics:

- Producer endpoint:
`POST /api/v5/events/incidents/created`
- Consumer:
`IncidentEventConsumer` with `@KafkaListener`

Example publish call:

```bash
curl -sS -X POST http://localhost:8080/api/v5/events/incidents/created \
  -H "Content-Type: application/json" \
  -d '{"incidentId":"inc-7001","serviceId":"payments-api","severity":4}'
```

Docker profile already wires Kafka via:
- `KAFKA_BOOTSTRAP_SERVERS=kafka:9092`
- `INCIDENTOPS_KAFKA_TOPIC=incident-events`
- `INCIDENTOPS_KAFKA_GROUP_ID=incidentops-docker`

Async processing patterns:

- Custom executor bean: `incidentOpsAsyncExecutor`
- Config prefix: `incidentops.async.executor.*`
- Async controller: `/api/v6/async/incidents`

Queue background audit task:

```bash
curl -sS -X POST http://localhost:8080/api/v6/async/incidents/INC-8801/audits \
  -H "Content-Type: application/json" \
  -d '{"requestedBy":"sre.oncall","reason":"trigger post-incident audit"}'
```

Async impact score calculation:

```bash
curl -sS "http://localhost:8080/api/v6/async/incidents/INC-8801/impact-score?severity=4"
```

Environment variables (optional overrides):
- `INCIDENTOPS_ASYNC_CORE_POOL_SIZE`
- `INCIDENTOPS_ASYNC_MAX_POOL_SIZE`
- `INCIDENTOPS_ASYNC_QUEUE_CAPACITY`
- `INCIDENTOPS_ASYNC_THREAD_NAME_PREFIX`

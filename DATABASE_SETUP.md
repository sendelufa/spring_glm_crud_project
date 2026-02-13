# Database Setup Guide

## Prerequisites

- Docker and Docker Compose installed
- PostgreSQL client tools (optional)

## Starting PostgreSQL Database

### Option 1: Using Docker Compose (Recommended)

```bash
# Start the database
docker-compose up -d

# View logs
docker-compose logs -f postgres

# Stop the database
docker-compose down

# Stop and remove volumes (wipe database)
docker-compose down -v
```

### Option 2: Using Docker directly

```bash
docker run -d \
  --name alcoholshop-postgres \
  -p 5432:5432 \
  -e POSTGRES_DB=alcoholshop \
  -e POSTGRES_USER=postgres \
  -e POSTGRES_PASSWORD=postgres \
  postgres:16
```

## Environment Variables

The application uses the following defaults (can be overridden):

```bash
DB_URL=jdbc:postgresql://localhost:5432/alcoholshop
DB_USERNAME=postgres
DB_PASSWORD=postgres
```

## Flyway Migrations

Migrations are located in:
```
src/main/resources/db/migration/
```

### Manual Migration Commands

```bash
# Check migration status
mvn flyway:info

# Apply pending migrations
mvn flyway:migrate

# Validate migrations
mvn flyway:validate

# Baseline an existing database
mvn flyway:baseline
```

## Database Schema

### alcohol_shops Table

| Column        | Type             | Constraints                  |
|---------------|------------------|------------------------------|
| id            | UUID             | PRIMARY KEY                  |
| name          | VARCHAR(100)     | NOT NULL                     |
| address       | VARCHAR(500)     | NOT NULL                     |
| latitude      | DOUBLE PRECISION | NOT NULL                     |
| longitude     | DOUBLE PRECISION | NOT NULL                     |
| phone_number  | VARCHAR(20)      | NULLABLE                     |
| working_hours | VARCHAR(50)      | NULLABLE                     |
| shop_type     | VARCHAR(20)      | NOT NULL                     |
| created_at    | TIMESTAMP        | NOT NULL                     |

### Indexes

- `idx_alcohol_shops_name` - for name searches
- `idx_alcohol_shops_type` - for shop_type filtering
- `idx_alcohol_shops_location` - for location queries

## Connecting to Database

### Using psql

```bash
# From within the container
docker exec -it alcoholshop-postgres psql -U postgres -d alcoholshop

# From host (if psql installed)
psql -h localhost -p 5432 -U postgres -d alcoholshop
```

### Using DBeaver or other tools

- Host: localhost
- Port: 5432
- Database: alcoholshop
- Username: postgres
- Password: postgres

## Troubleshooting

### Port already in use

```bash
# Check what's using port 5432
lsof -i :5432

# Stop existing PostgreSQL container
docker stop alcoholshop-postgres
docker rm alcoholshop-postgres
```

### Connection refused

1. Verify container is running: `docker ps`
2. Check container logs: `docker logs alcoholshop-postgres`
3. Verify network connectivity

### Migration failures

```bash
# Repair Flyway metadata
mvn flyway:repair

# Check migration history
SELECT * FROM flyway_schema_history;
```

## Production Considerations

For production deployment:

1. Change default passwords
2. Use environment-specific configuration
3. Enable SSL connections
4. Set up regular backups
5. Configure connection pooling
6. Monitor database performance
7. Use managed PostgreSQL service (AWS RDS, Cloud SQL, etc.)

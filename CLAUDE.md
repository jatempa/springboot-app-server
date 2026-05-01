# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Commands

```bash
# Build
./mvnw clean package

# Run application
./mvnw spring-boot:run

# Run all tests
./mvnw test

# Run a single test class
./mvnw test -Dtest=AppApplicationTests

# Build without tests
./mvnw clean package -DskipTests
```

## Environment Setup

Copy `.env.example` to `.env` and fill in the PostgreSQL credentials before running. The app reads `DB_URL`, `DB_USER`, and `DB_PASSWORD` at startup via the `dotenv` library.

```
DB_URL=jdbc:postgresql://localhost:5432/mystore
DB_USER=postgres
DB_PASSWORD=yourpassword
```

## Architecture

Spring Boot 4.0.6 REST API for store management. Java 21, PostgreSQL, Hibernate (ddl-auto: update), Lombok, MapStruct.

**Layered pattern**: `Controller → Service → Repository → Entity`

All REST endpoints are rooted at `/api/{resource}`. Base package: `com.mystore.app`.

**Domain entities**: Client, Product, Order, OrderItem (composite key), Payment, Category, Employee, Region, Review. Enums: PaymentStatus, PaymentMethod, Channel, OrderStatus, Role, Gender, ClientSegment.

**DTOs live in** `dto/` with `Request`/`Response` suffix naming. MapStruct mappers in `dto/mapper/` handle all entity↔DTO conversions — never map manually in controllers or services. Paginated endpoints return a `Paged{Entity}ResponseDTO`.

## Pagination

Cursor-based (keyset) pagination is implemented for **Product**, **Order**, and **Payment**. The cursor is a Base64-encoded `timestamp:id` pair managed by the shared `CursorUtils` utility class in `util/`:

- `CursorUtils.encodeCursor(Instant, Integer)` — encodes the cursor
- `CursorUtils.decodeCursor(String)` — decodes to a `CursorData(timestamp, id)` record

Use `CursorUtils` for all new pagination — never re-implement encoding inline and never use offset-based pagination.

`@EnableSpringDataWebSupport` is set on the main application class with `CAMEL_CASE_STRATEGY` DTO serialization mode for `Page` responses.

## N+1 Query Optimization

Use `JOIN FETCH` in repository queries to eagerly load required relationships and avoid N+1 problems. For entities with multiple collections (e.g., Order has both `items` and `payments`), Hibernate's cartesian product restriction requires splitting into two queries:

1. A `JOIN FETCH` query for the root entity and its scalar/single-value associations (Order → Client, Client.Region, Employee, Employee.Region).
2. Separate `IN`-clause queries for each collection (Order → OrderItems, Order → Payments).

See `OrderRepository` and `PaymentRepository` for reference implementations. Always use `FetchType.LAZY` on entity relationships and resolve loading eagerly in the repository query layer.

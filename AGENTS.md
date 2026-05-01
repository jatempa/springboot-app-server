# AGENTS.md

## Essential Commands

```bash
# Build and package
./mvnw clean package

# Run application locally
./mvnw spring-boot:run

# Run all tests
./mvnw test

# Run specific test class
./mvnw test -Dtest=AppApplicationTests

# Build without tests
./mvnw clean package -DskipTests
```

## Environment Setup

Copy `.env.example` to `.env` and configure PostgreSQL credentials before first run:

```
DB_URL=jdbc:postgresql://localhost:5432/mystore
DB_USER=postgres
DB_PASSWORD=yourpassword
```

The app loads environment variables via `dotenv` library at startup. **Do not commit `.env` file** - it's gitignored by default.

## Architecture

**Stack**: Spring Boot 4.0.6, Java 21, PostgreSQL, Hibernate, Lombok, MapStruct

**Package structure**: `com.mystore.app` with layered pattern: `Controller → Service → Repository → Entity`

**REST endpoints**: All endpoints are prefixed with `/api/{resource}` (e.g., `/api/payments`, `/api/products`)

**Domain entities**: Client, Product, Order, OrderItem (composite key), Payment, Category, Employee, Region, Review

**DTO Mapping**: 
- DTOs live in `dto/` with `Request`/`Response` suffix naming
- MapStruct mappers in `dto/mapper/` handle ALL entity↔DTO conversions
- **Never map manually in controllers or services**
- Mapper implementation classes are generated during compilation

**Pagination**: Use cursor-based (keyset) pagination, NOT offset-based. The cursor is a Base64-encoded timestamp (`createdAt`). See `ProductRepository` for example implementation using `WHERE createdAt > :cursor` style queries.

**Entity relationships**: Use `FetchType.LAZY` by default. Be aware of N+1 query problems when repositories return entities that are later mapped to DTOs accessing lazy-loaded relationships.

**Hibernate**: Configured with `ddl-auto: update` - database schema updates automatically on startup.

**Important annotation processor config**: Lombok and MapStruct require specific `maven-compiler-plugin` configuration with annotation processor paths in `pom.xml`. This is already configured - do not modify unless you understand the interaction between these two annotation processors.

## Testing

The project uses Spring Boot's default testing setup. Test files should mirror the main source structure under `src/test/java/`. When writing tests for repositories or services that use MapStruct mappers, remember that mapper implementations are generated at compile-time and available in tests.

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

**Domain entities**: Client, Product, Order, OrderItem (composite key), Payment, Category, Employee, Region, Review, plus enums: PaymentStatus, PaymentMethod, Channel, OrderStatus, Role, Gender, ClientSegment

**DTO Mapping**: 
- DTOs live in `dto/` with `Request`/`Response` suffix naming
- MapStruct mappers in `dto/mapper/` handle ALL entity↔DTO conversions
- **Never map manually in controllers or services**
- Mapper implementation classes are generated during compilation

**Pagination**: Cursor-based (keyset) pagination is implemented for **Payment**, **Order**, and **Product** entities. The cursor is a Base64-encoded `timestamp:id` pair (not just timestamp). See:
- `CursorUtils` utility class for `encodeCursor()` and `decodeCursor()` methods
- `Paged{Payment,Order,Product}ResponseDTO` for paginated response format
- Each repository has `findAllOrderBy*` and `findByKeyset()` methods for efficient keyset pagination
- **Never use offset-based pagination** - always use keyset pagination pattern

**Entity relationships**: Use `FetchType.LAZY` by default. Be aware of N+1 query problems when repositories return entities that are later mapped to DTOs accessing lazy-loaded relationships.

**N+1 Query Optimization**: Repositories use `JOIN FETCH` in queries to eagerly load required relationships in a single query, avoiding N+1 problems. For complex scenarios with multiple collections (e.g., Order with items and payments), the pattern uses:
1. A dedicated `JOIN FETCH` query for the main entity with its direct relationships (Order → Client, Client.Region, Employee, Employee.Region)
2. Separate IN-clause queries for collections (Order → OrderItems, Order → Payments) to avoid Hibernate cartesian product issues

Example optimized queries can be found in `OrderRepository` and `PaymentRepository`.

**Hibernate**: Configured with `ddl-auto: update` - database schema updates automatically on startup.

**Important annotation processor config**: Lombok and MapStruct require specific `maven-compiler-plugin` configuration with annotation processor paths in `pom.xml`. This is already configured - do not modify unless you understand the interaction between these two annotation processors.

**RabbitMQ Messaging**: Asynchronous order report generation implemented with RabbitMQ for background PDF processing. The topology uses a direct exchange pattern:
- **Exchange**: `orders.exchange` (durable, non-auto-delete)
- **Queue**: `orders.report.queue` (durable)
- **Routing Key**: `orders.report`
- **Message**: Integer `orderId` with JSON serialization

**Workflow**: 
1. Client POSTs to `/api/orders/{id}/report` → returns `202 Accepted` immediately
2. `OrderReportPublisher` sends orderId to RabbitMQ
3. `OrderReportConsumer` receives message, fetches order data, generates PDF report
4. Client polls same endpoint to download PDF when ready (`200 OK`) or check status (`202 Accepted` if still processing)

**Key Components**:
- `RabbitMQConfig.java` - Declares exchange, queue, binding, and Jackson message converter
- `OrderReportPublisher.java` - Publishes orderId to configured exchange/routing key
- `OrderReportConsumer.java` - `@RabbitListener` processes messages asynchronously
- `OrderReportService.java` - Generates PDF reports using OpenPDF library
- `OrderController.java` - `/api/orders/{id}/report` endpoints for enqueue and download

**RabbitMQ Environment Variables**: Add to `.env` file:
```bash
RABBITMQ_HOST=localhost
RABBITMQ_PORT=5672
RABBITMQ_USER=guest
RABBITMQ_PASS=guest
```

## Testing

The project uses Spring Boot's default testing setup. Test files should mirror the main source structure under `src/test/java/`. When writing tests for repositories or services that use MapStruct mappers, remember that mapper implementations are generated at compile-time and available in tests.

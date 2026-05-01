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

**Domain entities**: Client, Product, Order, OrderItem (composite key), Payment, Category, Employee, Region, Review.

**DTOs live in** `dto/` with `Request`/`Response` suffix naming. MapStruct mappers in `dto/mapper/` handle all entity↔DTO conversions — never map manually in controllers or services.

## Pagination

`ProductService` implements cursor-based (keyset) pagination. The cursor is a Base64-encoded timestamp (`createdAt`) used in `ProductRepository` with a `WHERE createdAt > :cursor` style query. Use this same approach when adding pagination to other entities — do not use offset-based pagination.

`@EnableSpringDataWebSupport` is set on the main application class with `CAMEL_CASE_STRATEGY` DTO serialization mode for `Page` responses.

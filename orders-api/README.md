# orders-api

A small Spring Boot REST service that manages **products** (digital goods) and **orders** for them, persisting everything in **AWS DynamoDB**. It is the catalogue-and-order backbone of the PayLab platform: it decides *what* is being sold, for *how much*, and tracks the lifecycle of each purchase. Payment processing itself happens elsewhere; this service only records the outcome through the order status.

## Tech stack

- Java 21, Spring Boot 4.2 (`spring-boot-starter-webmvc`, `spring-boot-starter-validation`)
- AWS SDK v2 DynamoDB Enhanced Client
- Lombok
- Maven (wrapper included)

## How the system works

```
Client ──HTTP/JSON──▶ Controller ──▶ Service ──▶ Repository ──▶ DynamoDB
                          │             │
                          │             └─ business rules (pricing, active check, ID generation)
                          └─ request validation (Jakarta Bean Validation) + DTO mapping
```

| Layer        | Package      | Responsibility |
|--------------|--------------|----------------|
| Controller   | `controller` | HTTP routes, `@Valid` request validation, mapping models to response DTOs |
| Service      | `service`    | Business rules; `OrderService` depends on `ProductService` |
| Repository   | `repository` | DynamoDB access through `DynamoDbTable` (get/put/delete/scan) |
| Model        | `model`      | DynamoDB beans (`Product`, `Order`) and the `OrderStatus` enum |
| DTO          | `dto`        | Immutable request/response records |
| Exception    | `exception`  | Domain exceptions and a global `@RestControllerAdvice` error mapper |
| Config       | `config`     | `DynamoDbClient` and `DynamoDbEnhancedClient` beans |

### Typical flow

1. An admin creates a product (`POST /api/products`) with a name, price in minor units (cents), currency, `active` flag and the S3 key of the file being sold.
2. A storefront lists active products (`GET /api/products`) and shows them to the buyer.
3. The buyer checks out: the client calls `POST /api/orders` with only a `productId` and `email`.
4. The server loads the product, verifies it is active, and **copies the price and currency from the product** into a new order with status `CREATED`. The client can never choose or tamper with the amount.
5. After the payment provider reports the result, the order status is updated through `PUT /api/orders/{orderId}` to `PAID` or `FAILED`.

## Domain model

### Product (`Products` table, partition key `productId`)

| Field         | Type    | Notes |
|---------------|---------|-------|
| `productId`   | String  | Client-supplied or auto-generated UUID |
| `name`        | String  | |
| `description` | String  | Optional |
| `priceMinor`  | Long    | Price in minor units (e.g. cents) |
| `currency`    | String  | 3-letter code, stored lower-case |
| `s3Key`       | String  | Location of the purchased file in S3. **Never returned by the API** |
| `active`      | Boolean | Only active products are listed and can be ordered |

### Order (`Orders` table, partition key `orderId`)

| Field         | Type        | Notes |
|---------------|-------------|-------|
| `orderId`     | String      | Server-generated UUID |
| `productId`   | String      | |
| `email`       | String      | Buyer email |
| `amountMinor` | Long        | Snapshot of the product price at order time |
| `currency`    | String      | Snapshot of the product currency |
| `status`      | OrderStatus | `CREATED`, `PAID`, `FAILED` |
| `createdAt`   | Instant     | |
| `updatedAt`   | Instant     | Refreshed on every update |

## API endpoints

Base path: `/api`. All request and response bodies are JSON.

### Health

| Method | Path          | Description | Response |
|--------|---------------|-------------|----------|
| GET    | `/api/health` | Liveness check | `200` `{"status":"UP"}` |

### Products

| Method | Path                       | Description | Success |
|--------|----------------------------|-------------|---------|
| GET    | `/api/products`            | List active products | `200` |
| GET    | `/api/products/{productId}`| Get one product | `200` |
| POST   | `/api/products`            | Create a product | `201` |
| PUT    | `/api/products/{productId}`| Replace a product's fields | `200` |
| DELETE | `/api/products/{productId}`| Delete a product | `204` |

Request body for `POST` / `PUT`:

```json
{
  "productId": "ebook-001",
  "name": "Java Handbook",
  "description": "PDF edition",
  "priceMinor": 1999,
  "currency": "EUR",
  "s3Key": "files/java-handbook.pdf",
  "active": true
}
```

- `productId` is optional on create (a UUID is generated if omitted, max 64 chars). Creating with an ID that already exists returns `422`.
- `name` required (max 200); `description` max 2000; `priceMinor` required, `>= 0`; `currency` required, exactly 3 letters; `s3Key` required (max 1024); `active` required.

Response body (note: no `s3Key`):

```json
{
  "productId": "ebook-001",
  "name": "Java Handbook",
  "description": "PDF edition",
  "priceMinor": 1999,
  "currency": "eur",
  "active": true
}
```

### Orders

| Method | Path                     | Description | Success |
|--------|--------------------------|-------------|---------|
| POST   | `/api/orders`            | Create an order for a product | `201` |
| GET    | `/api/orders/{orderId}`  | Get an order | `200` |
| PUT    | `/api/orders/{orderId}`  | Update status (and optionally email) | `200` |
| DELETE | `/api/orders/{orderId}`  | Delete an order | `204` |

Create request:

```json
{ "productId": "ebook-001", "email": "buyer@example.com" }
```

Update request (`status` required, `email` optional):

```json
{ "status": "PAID", "email": "new@example.com" }
```

Order response:

```json
{
  "orderId": "3f1c2c0e-6a4b-4a43-9a53-0d2f3e6f0c11",
  "productId": "ebook-001",
  "email": "buyer@example.com",
  "amountMinor": 1999,
  "currency": "eur",
  "status": "CREATED",
  "createdAt": "2026-11-25T10:00:00Z",
  "updatedAt": "2026-11-25T10:00:00Z"
}
```

## Error handling

All errors are returned by `GlobalExceptionHandler` in one shape:

```json
{
  "timestamp": "2026-11-25T10:00:00Z",
  "status": 400,
  "message": "Validation failed",
  "errors": { "email": "must be a well-formed email address" }
}
```

| Status | When |
|--------|------|
| 400 | Bean validation failed (`errors` lists field → message) or the body is malformed JSON |
| 404 | Product or order not found |
| 422 | Business rule violated: ordering an inactive product, or creating a product whose ID already exists |
| 500 | Unexpected error (logged server-side; details are not leaked to the client) |

## Design notes

- **Server-side pricing:** order amount and currency always come from the product, never from the request.
- **Price snapshot:** the order stores its own copy of the amount and currency, so later product edits do not change existing orders.
- **Secret file location:** `s3Key` is write-only through the API; `ProductResponse` deliberately omits it.
- **Key-based order access:** `OrderRepository` only exposes get/put/delete by key. There is no scan or list of orders, by design.
- **Product scan:** listing active products uses a DynamoDB scan with a filter, which is acceptable because the catalogue is small.
- **No authentication yet:** the endpoints are open, so the service should sit behind a gateway or be secured before public exposure.

## Configuration

`src/main/resources/application.properties`:

| Property                       | Default | Description |
|--------------------------------|---------|-------------|
| `aws.region`                   | `${AWS_REGION:eu-central-1}` | AWS region |
| `aws.dynamodb.endpoint`        | `https://dynamodb.eu-central-1.amazonaws.com` | DynamoDB endpoint. Set to `http://localhost:8000` for DynamoDB Local; blank uses the SDK default |
| `aws.dynamodb.products-table`  | `Products` | Products table name |
| `aws.dynamodb.orders-table`    | `Orders`   | Orders table name |

AWS credentials are resolved by the SDK's default provider chain (environment variables, `~/.aws` profile, IAM role, etc.).

### Required DynamoDB tables

Both tables must exist before the service starts handling requests (the app does not create them):

- `Products`: partition key `productId` (String)
- `Orders`: partition key `orderId` (String)

## Running locally

```bash
# Optional: DynamoDB Local
docker run -p 8000:8000 amazon/dynamodb-local

# Run (override the endpoint if using DynamoDB Local)
./mvnw spring-boot:run -Dspring-boot.run.arguments="--aws.dynamodb.endpoint=http://localhost:8000"

# Tests
./mvnw test

# Build a jar
./mvnw clean package
```

The service listens on port `8080` by default.

```bash
curl localhost:8080/api/health
```

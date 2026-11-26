# Payment Provider

A plain Java library that wraps the [LiqPay](https://www.liqpay.ua/) payment gateway. It is a module of `app-api` and is used by `orders-api`. It has no Spring dependencies, no `main` class and no HTTP endpoints of its own, so LiqPay request signing and API quirks live in one place.

## What it does

- Creates **invoices** that LiqPay emails to a customer.
- Builds **checkout** payloads/forms so a frontend can redirect a customer to the LiqPay payment page.
- **Verifies webhook signatures** so callers can trust that a payment notification really came from LiqPay.
- Looks up the **status of a payment** by order id.

The library is stateless. It has no database and stores no payment data. Every call is translated into a LiqPay request (or a local signature computation) and the result is returned.

## Tech stack

- Java 21
- LiqPay Java SDK (`liqpay-sdk` 0.8-SNAPSHOT)
- Jackson 3, SLF4J
- Lombok
- Maven

## How it works

| Layer | Class | Responsibility |
|-------|-------|----------------|
| Service | `LiqPayProviderService` | Builds LiqPay parameters, calls the SDK, signs data, verifies signatures. |
| DTOs | `CreateInvoiceDTO`, `CreateCheckoutDTO`, `VerifyWebhookDTO` | Method inputs. |
| Exceptions | `LiqPayException` | Thrown when LiqPay rejects or fails a request. |

## Usage

Add the dependency (the version is managed by the `app-api` parent):

```xml
<dependency>
    <groupId>paylab</groupId>
    <artifactId>payment-provider</artifactId>
</dependency>
```

Create the service (`orders-api` does this in `LiqPayConfig`):

```java
LiqPay liqPay = new LiqPay(publicKey, privateKey);
LiqPayProviderService service =
        new LiqPayProviderService(liqPay, JsonMapper.builder().build(), publicKey, privateKey);
```

| Method | Description |
|--------|-------------|
| `createInvoice(CreateInvoiceDTO)` | Sends an invoice to the customer's email (`invoice_send`). |
| `createCheckout(CreateCheckoutDTO)` | Returns an HTML checkout form. |
| `generateCheckoutSignatureAndData(CreateCheckoutDTO)` | Returns the signed `data` / `signature` pair. |
| `verifyWebhook(VerifyWebhookDTO)` | Checks a webhook signature (constant-time). |
| `getPaymentStatus(Integer orderId)` | Looks up a payment (`status`). |

### LiqPay request model

Every LiqPay call carries an `action` (`invoice_send`, `pay`, `status`) and `version` (`3`) plus action-specific fields. For browser-based flows LiqPay expects two values:

- `data`: the parameters as JSON, Base64-encoded
- `signature`: `Base64(SHA1(private_key + data + private_key))`

The service produces and checks these itself.

## Building

From the `app-api` directory:

```bash
./mvnw test -pl payment-provider     # run the library tests
./mvnw clean install                 # build all modules
```

## Notes and current limitations

- Checkout requests are hard-coded to LiqPay **sandbox mode** (`sandbox=1`) in `LiqPayProviderService.createCheckoutParams`. Remove or make it configurable before going to production.
- `getPaymentStatus` takes the order id as an `Integer`, while the other methods use strings.
- No payment state is persisted here. Callers are responsible for tracking orders.

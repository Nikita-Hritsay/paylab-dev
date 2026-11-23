package paylab.ordersapi.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/** Amount and currency are taken from the product on the server side. */
public record CreateOrderRequest(
        @NotBlank String productId,
        @NotBlank @Email @Size(max = 254) String email) {
}

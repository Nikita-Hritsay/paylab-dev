package paylab.ordersapi.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

public record ProductRequest(
        @Size(max = 64) String productId,
        @NotBlank @Size(max = 200) String name,
        @Size(max = 2000) String description,
        @NotNull @PositiveOrZero Long priceMinor,
        @NotBlank @Pattern(regexp = "^[a-zA-Z]{3}$", message = "must be a 3-letter currency code") String currency,
        @NotBlank @Size(max = 1024) String s3Key,
        @NotNull Boolean active) {
}

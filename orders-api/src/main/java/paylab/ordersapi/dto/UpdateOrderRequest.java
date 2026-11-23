package paylab.ordersapi.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import paylab.ordersapi.model.OrderStatus;

public record UpdateOrderRequest(
        @NotNull OrderStatus status,
        @Email @Size(max = 254) String email) {
}

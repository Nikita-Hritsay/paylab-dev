package paylab.ordersapi.dto;

import java.time.Instant;

import paylab.ordersapi.model.Order;
import paylab.ordersapi.model.OrderStatus;

public record OrderResponse(
        String orderId,
        String productId,
        String email,
        Long amountMinor,
        String currency,
        OrderStatus status,
        Instant createdAt,
        Instant updatedAt,
        String checkoutUrl) {

    /** For responses without a payment link: checkoutUrl is null. */
    public static OrderResponse from(Order o) {
        return from(o, null);
    }

    public static OrderResponse from(Order o, String checkoutUrl) {
        return new OrderResponse(o.getOrderId(), o.getProductId(), o.getEmail(), o.getAmountMinor(),
                o.getCurrency(), o.getStatus(), o.getCreatedAt(), o.getUpdatedAt(), checkoutUrl);
    }
}

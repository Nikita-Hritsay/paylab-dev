package paylab.ordersapi.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import java.math.BigDecimal;
import java.util.Locale;
import paylab.ordersapi.dto.CreateOrderRequest;
import paylab.ordersapi.dto.OrderResponse;
import paylab.ordersapi.dto.UpdateOrderRequest;
import paylab.ordersapi.model.Order;
import paylab.ordersapi.model.OrderStatus;
import paylab.ordersapi.service.OrderService;
import paylab.paymentprovider.dtos.CreateCheckoutDTO;
import paylab.paymentprovider.services.LiqPayProviderService;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService service;
    private final LiqPayProviderService liqPayProviderService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public OrderResponse create(@Valid @RequestBody CreateOrderRequest request) {
        return toResponseWithCheckout(service.create(request));
    }

    @GetMapping("/{orderId}")
    public OrderResponse get(@PathVariable String orderId) {
        return toResponseWithCheckout(service.get(orderId));
    }

    @PutMapping("/{orderId}")
    public OrderResponse update(@PathVariable String orderId, @Valid @RequestBody UpdateOrderRequest request) {
        return OrderResponse.from(service.update(orderId, request));
    }

    @DeleteMapping("/{orderId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable String orderId) {
        service.delete(orderId);
    }

    /** Adds a checkout URL only while the order is still awaiting payment (CREATED). */
    private OrderResponse toResponseWithCheckout(Order order) {
        if (order.getStatus() != OrderStatus.CREATED) {
            return OrderResponse.from(order);
        }
        return OrderResponse.from(order, liqPayProviderService.createCheckoutUrl(checkoutFor(order)));
    }

    private static CreateCheckoutDTO checkoutFor(Order order) {
        CreateCheckoutDTO checkout = new CreateCheckoutDTO();
        // LiqPay expects major units ("10.50") and an upper-case currency code.
        checkout.setAmount(BigDecimal.valueOf(order.getAmountMinor(), 2).toPlainString());
        checkout.setCurrency(order.getCurrency().toUpperCase(Locale.ROOT));
        checkout.setDescription("Order " + order.getOrderId());
        checkout.setOrder_id(order.getOrderId());
        return checkout;
    }
}

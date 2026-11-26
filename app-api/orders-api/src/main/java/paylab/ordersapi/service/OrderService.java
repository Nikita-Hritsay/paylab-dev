package paylab.ordersapi.service;

import java.time.Instant;
import java.util.UUID;

import org.springframework.stereotype.Service;

import paylab.ordersapi.dto.CreateOrderRequest;
import paylab.ordersapi.dto.UpdateOrderRequest;
import paylab.ordersapi.exception.BusinessException;
import paylab.ordersapi.exception.NotFoundException;
import paylab.ordersapi.model.Order;
import paylab.ordersapi.model.OrderStatus;
import paylab.ordersapi.model.Product;
import paylab.ordersapi.repository.OrderRepository;

@Service
public class OrderService {

    private final OrderRepository orderRepository;
    private final ProductService productService;

    public OrderService(OrderRepository orderRepository, ProductService productService) {
        this.orderRepository = orderRepository;
        this.productService = productService;
    }

    public Order create(CreateOrderRequest req) {
        Product product = productService.get(req.productId());
        if (!Boolean.TRUE.equals(product.getActive())) {
            throw new BusinessException("Product is not active: " + req.productId());
        }
        Instant now = Instant.now();
        Order order = new Order();
        order.setOrderId(UUID.randomUUID().toString());
        order.setProductId(product.getProductId());
        order.setEmail(req.email());
        order.setAmountMinor(product.getPriceMinor());
        order.setCurrency(product.getCurrency());
        order.setStatus(OrderStatus.CREATED);
        order.setCreatedAt(now);
        order.setUpdatedAt(now);
        orderRepository.save(order);
        return order;
    }

    public Order get(String orderId) {
        return orderRepository.findById(orderId)
                .orElseThrow(() -> new NotFoundException("Order not found: " + orderId));
    }

    public Order update(String orderId, UpdateOrderRequest req) {
        Order order = get(orderId);
        order.setStatus(req.status());
        if (req.email() != null) {
            order.setEmail(req.email());
        }
        order.setUpdatedAt(Instant.now());
        orderRepository.save(order);
        return order;
    }

    public void delete(String orderId) {
        get(orderId);
        orderRepository.deleteById(orderId);
    }
}

package paylab.ordersapi.repository;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;

import paylab.ordersapi.model.Order;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;
import software.amazon.awssdk.enhanced.dynamodb.Key;
import software.amazon.awssdk.enhanced.dynamodb.TableSchema;

/** Key-based access only: Scan on the Orders table is intentionally not provided. */
@Repository
public class OrderRepository {

    private final DynamoDbTable<Order> table;

    public OrderRepository(DynamoDbEnhancedClient client,
                           @Value("${aws.dynamodb.orders-table}") String tableName) {
        this.table = client.table(tableName, TableSchema.fromBean(Order.class));
    }

    public void save(Order order) {
        table.putItem(order);
    }

    public Optional<Order> findById(String orderId) {
        return Optional.ofNullable(table.getItem(Key.builder().partitionValue(orderId).build()));
    }

    public void deleteById(String orderId) {
        table.deleteItem(Key.builder().partitionValue(orderId).build());
    }
}

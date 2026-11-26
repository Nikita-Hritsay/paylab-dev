package paylab.ordersapi.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;

import paylab.ordersapi.model.Product;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;
import software.amazon.awssdk.enhanced.dynamodb.Expression;
import software.amazon.awssdk.enhanced.dynamodb.Key;
import software.amazon.awssdk.enhanced.dynamodb.TableSchema;
import software.amazon.awssdk.enhanced.dynamodb.model.ScanEnhancedRequest;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;

@Repository
public class ProductRepository {

    private final DynamoDbTable<Product> table;

    public ProductRepository(DynamoDbEnhancedClient client,
                             @Value("${aws.dynamodb.products-table}") String tableName) {
        this.table = client.table(tableName, TableSchema.fromBean(Product.class));
    }

    public void save(Product product) {
        table.putItem(product);
    }

    public Optional<Product> findById(String productId) {
        return Optional.ofNullable(table.getItem(Key.builder().partitionValue(productId).build()));
    }

    /** Scan is acceptable here: the Products table is small. */
    public List<Product> findActive() {
        ScanEnhancedRequest request = ScanEnhancedRequest.builder()
                .filterExpression(Expression.builder()
                        .expression("active = :a")
                        .putExpressionValue(":a", AttributeValue.builder().bool(true).build())
                        .build())
                .build();
        return table.scan(request).items().stream().toList();
    }

    public List<Product> findAll() {
        return table.scan().items().stream().toList();
    }

    public boolean exists(String productId) {
        return findById(productId).isPresent();
    }

    public void deleteById(String productId) {
        table.deleteItem(Key.builder().partitionValue(productId).build());
    }
}

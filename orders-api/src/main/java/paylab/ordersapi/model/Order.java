package paylab.ordersapi.model;

import java.time.Instant;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbBean;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbPartitionKey;

@DynamoDbBean
@Getter
@Setter
@NoArgsConstructor
public class Order {

    @Getter(onMethod_ = @DynamoDbPartitionKey)
    private String orderId;
    private String productId;
    private String email;
    private Long amountMinor;
    private String currency;
    private OrderStatus status;
    private Instant createdAt;
    private Instant updatedAt;
}

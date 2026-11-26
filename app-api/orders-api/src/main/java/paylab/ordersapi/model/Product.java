package paylab.ordersapi.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbBean;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbPartitionKey;

@DynamoDbBean
@Getter
@Setter
@NoArgsConstructor
public class Product {

    @Getter(onMethod_ = @DynamoDbPartitionKey)
    private String productId;
    private String name;
    private String description;
    /** Price in minor units (cents). */
    private Long priceMinor;
    private String currency;
    /** Key of the purchased file in S3. Never exposed through the API responses. */
    private String s3Key;
    private Boolean active;
}

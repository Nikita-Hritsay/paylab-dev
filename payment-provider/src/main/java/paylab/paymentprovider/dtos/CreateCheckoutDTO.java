package paylab.paymentprovider.dtos;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CreateCheckoutDTO {
    String amount;
    String currency;
    String description;
    String order_id;
}

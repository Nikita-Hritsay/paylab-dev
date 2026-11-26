package paylab.paymentprovider.dtos;

import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.Map;

@Getter
@Setter
public class CreateInvoiceDTO {
    String email;
    String amount;
    String currency;
    String orderId;
    List<Map<String, String>> goods;
}

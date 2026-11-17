package paylab.paymentprovider.dtos;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class VerifyWebhookDTO {
    String data;
    String signature;
}

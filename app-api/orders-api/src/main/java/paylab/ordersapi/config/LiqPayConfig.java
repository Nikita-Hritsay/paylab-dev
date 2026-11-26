package paylab.ordersapi.config;

import com.liqpay.LiqPay;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import paylab.paymentprovider.services.LiqPayProviderService;
import tools.jackson.databind.json.JsonMapper;

@Configuration
public class LiqPayConfig {

    @Bean
    public LiqPay liqPay(@Value("${public_key}") String publicKey,
                         @Value("${private_key}") String privateKey) {
        return new LiqPay(publicKey, privateKey);
    }

    @Bean
    public LiqPayProviderService liqPayProviderService(LiqPay liqPay,
                                                       JsonMapper jsonMapper,
                                                       @Value("${public_key}") String publicKey,
                                                       @Value("${private_key}") String privateKey) {
        return new LiqPayProviderService(liqPay, jsonMapper, publicKey, privateKey);
    }
}

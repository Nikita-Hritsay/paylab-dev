package paylab.paymentprovider.config;

import com.liqpay.LiqPay;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class LiqPayConfig {

    @Bean
    public LiqPay liqPay(@Value("${public_key}") String publicKey,
                         @Value("${private_key}") String privateKey) {
        return new LiqPay(publicKey, privateKey);
    }
}

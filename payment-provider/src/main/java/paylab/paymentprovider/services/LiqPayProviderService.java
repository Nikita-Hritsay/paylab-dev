package paylab.paymentprovider.services;

import com.liqpay.LiqPay;
import com.liqpay.LiqPayUtil;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.json.simple.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import paylab.paymentprovider.dtos.CreateCheckoutDTO;
import paylab.paymentprovider.dtos.CreateInvoiceDTO;
import paylab.paymentprovider.exceptions.LiqPayException;
import tools.jackson.databind.json.JsonMapper;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class LiqPayProviderService {

    private final LiqPay liqPay;
    private final JsonMapper jsonMapper;
    @Value("${public_key}")
    private String publicKey;
    @Value("${private_key}")
    private String privateKey;

    public Map<String, Object> createInvoice(CreateInvoiceDTO dto) {
        Map<String, String> params = new HashMap<>();
        params.put("action", "invoice_send");
        params.put("version", "3");
        params.put("email", dto.getEmail());
        params.put("amount", dto.getAmount());
        params.put("currency", dto.getCurrency());
        params.put("order_id", dto.getOrderId());
        params.put("goods", jsonMapper.writeValueAsString(dto.getGoods()));

        Map<String, Object> res;
        try {
            res = liqPay.api("request", params);
        } catch (Exception e) {
            throw new LiqPayException("LiqPay request failed", e);
        }

        if ("error".equals(res.get("result"))) {
            log.warn("LiqPay rejected invoice for order {}: {} ({})",
                    dto.getOrderId(), res.get("err_description"), res.get("err_code"));
            throw new LiqPayException("LiqPay error: " + res.get("err_description"));
        }

        log.info("LiqPay invoice created for order {}", dto.getOrderId());
        return res;
    }

    public String createCheckout(CreateCheckoutDTO dto) {
        Map<String, String> params = createDefaultCheckoutParams(dto);
        return liqPay.cnb_form(params);
    }

    public Map<String, String> generateCheckoutSignatureAndData(CreateCheckoutDTO dto) {
        Map<String, String> ret = new HashMap<>();
        Map<String, String> params = createDefaultCheckoutParams(dto);
        params.put("public_key", this.publicKey);
        String data = LiqPayUtil.base64_encode(JSONObject.toJSONString(params).getBytes());
        ret.put("data", data);
        ret.put("signature", LiqPayUtil.base64_encode(LiqPayUtil.sha1(this.privateKey + data + this.privateKey)));
        return ret;
    }

    private Map<String, String> createDefaultCheckoutParams(CreateCheckoutDTO dto) {
        Map<String, String> params = new HashMap<>();
        params.put("action", "pay");
        params.put("amount", dto.getAmount());
        params.put("currency", dto.getCurrency());
        params.put("description", dto.getDescription());
        params.put("order_id", dto.getOrder_id());
        params.put("version", "3");
        params.put("sandbox", "1");
        return params;
    }
}

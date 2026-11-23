package paylab.paymentprovider.services;

import com.liqpay.LiqPay;
import com.liqpay.LiqPayUtil;
import lombok.extern.slf4j.Slf4j;
import org.json.simple.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import paylab.paymentprovider.dtos.CreateCheckoutDTO;
import paylab.paymentprovider.dtos.CreateInvoiceDTO;
import paylab.paymentprovider.dtos.VerifyWebhookDTO;
import paylab.paymentprovider.exceptions.LiqPayException;
import tools.jackson.databind.json.JsonMapper;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
public class LiqPayProviderService {

    private static final String API_VERSION = "3";
    private static final String REQUEST_PATH = "request";

    private final LiqPay liqPay;
    private final JsonMapper jsonMapper;
    private final String publicKey;
    private final String privateKey;

    public LiqPayProviderService(LiqPay liqPay,
                                 JsonMapper jsonMapper,
                                 @Value("${public_key}") String publicKey,
                                 @Value("${private_key}") String privateKey) {
        this.liqPay = liqPay;
        this.jsonMapper = jsonMapper;
        this.publicKey = publicKey;
        this.privateKey = privateKey;
    }

    public Map<String, Object> createInvoice(CreateInvoiceDTO dto) {
        Map<String, String> params = baseParams("invoice_send");
        params.put("email", dto.getEmail());
        params.put("amount", dto.getAmount());
        params.put("currency", dto.getCurrency());
        params.put("order_id", dto.getOrderId());
        params.put("goods", jsonMapper.writeValueAsString(dto.getGoods()));

        Map<String, Object> res = callApi(params);

        if ("error".equals(res.get("result"))) {
            log.warn("LiqPay rejected invoice for order {}: {} ({})",
                    dto.getOrderId(), res.get("err_description"), res.get("err_code"));
            throw new LiqPayException("LiqPay error: " + res.get("err_description"));
        }

        log.info("LiqPay invoice created for order {}", dto.getOrderId());
        return res;
    }

    public String createCheckout(CreateCheckoutDTO dto) {
        return liqPay.cnb_form(createCheckoutParams(dto));
    }

    public Map<String, String> generateCheckoutSignatureAndData(CreateCheckoutDTO dto) {
        Map<String, String> params = createCheckoutParams(dto);
        params.put("public_key", publicKey);

        String data = LiqPayUtil.base64_encode(JSONObject.toJSONString(params).getBytes(StandardCharsets.UTF_8));

        Map<String, String> result = new HashMap<>();
        result.put("data", data);
        result.put("signature", sign(data));
        return result;
    }

    public boolean verifyWebhook(VerifyWebhookDTO dto) {
        if (dto.getData() == null || dto.getSignature() == null) {
            return false;
        }
        return MessageDigest.isEqual(
                sign(dto.getData()).getBytes(StandardCharsets.UTF_8),
                dto.getSignature().getBytes(StandardCharsets.UTF_8));
    }

    public Map<String, Object> getPaymentStatus(Integer orderId) {
        Map<String, String> params = baseParams("status");
        params.put("order_id", orderId.toString());
        return callApi(params);
    }

    private Map<String, String> createCheckoutParams(CreateCheckoutDTO dto) {
        Map<String, String> params = baseParams("pay");
        params.put("amount", dto.getAmount());
        params.put("currency", dto.getCurrency());
        params.put("description", dto.getDescription());
        params.put("order_id", dto.getOrder_id());
        params.put("sandbox", "1");
        return params;
    }

    private Map<String, String> baseParams(String action) {
        Map<String, String> params = new HashMap<>();
        params.put("action", action);
        params.put("version", API_VERSION);
        return params;
    }

    private Map<String, Object> callApi(Map<String, String> params) {
        try {
            return liqPay.api(REQUEST_PATH, params);
        } catch (Exception e) {
            throw new LiqPayException("LiqPay request failed", e);
        }
    }

    private String sign(String data) {
        return LiqPayUtil.base64_encode(LiqPayUtil.sha1(privateKey + data + privateKey));
    }
}

package paylab.paymentprovider.services;

import com.liqpay.LiqPay;
import com.liqpay.LiqPayUtil;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import paylab.paymentprovider.dtos.CreateCheckoutDTO;
import paylab.paymentprovider.dtos.CreateInvoiceDTO;
import paylab.paymentprovider.dtos.VerifyWebhookDTO;
import paylab.paymentprovider.exceptions.LiqPayException;
import tools.jackson.databind.json.JsonMapper;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LiqPayProviderServiceTest {

    private static final String PUBLIC_KEY = "pub";
    private static final String PRIVATE_KEY = "priv";

    @Mock
    private LiqPay liqPay;

    private LiqPayProviderService service;

    @BeforeEach
    void setUp() {
        service = new LiqPayProviderService(liqPay, JsonMapper.builder().build(), PUBLIC_KEY, PRIVATE_KEY);
    }

    private static String sign(String data) {
        return LiqPayUtil.base64_encode(LiqPayUtil.sha1(PRIVATE_KEY + data + PRIVATE_KEY));
    }

    private static CreateCheckoutDTO checkoutDto() {
        CreateCheckoutDTO dto = new CreateCheckoutDTO();
        dto.setAmount("10");
        dto.setCurrency("UAH");
        dto.setDescription("desc");
        dto.setOrder_id("o-1");
        return dto;
    }

    private static CreateInvoiceDTO invoiceDto() {
        CreateInvoiceDTO dto = new CreateInvoiceDTO();
        dto.setEmail("a@b.c");
        dto.setAmount("5");
        dto.setCurrency("USD");
        dto.setOrderId("o-2");
        dto.setGoods(List.of(Map.of("name", "item")));
        return dto;
    }

    // ---- createInvoice

    @Test
    @SuppressWarnings("unchecked")
    void createInvoice_sendsExpectedParamsAndReturnsResponse() throws Exception {
        Map<String, Object> response = Map.of("result", "ok");
        when(liqPay.api(eq("request"), any())).thenReturn(response);

        Map<String, Object> result = service.createInvoice(invoiceDto());

        assertSame(response, result);
        ArgumentCaptor<Map<String, String>> captor = ArgumentCaptor.forClass(Map.class);
        verify(liqPay).api(eq("request"), captor.capture());
        Map<String, String> params = captor.getValue();
        assertEquals("invoice_send", params.get("action"));
        assertEquals("3", params.get("version"));
        assertEquals("a@b.c", params.get("email"));
        assertEquals("5", params.get("amount"));
        assertEquals("USD", params.get("currency"));
        assertEquals("o-2", params.get("order_id"));
        assertEquals("[{\"name\":\"item\"}]", params.get("goods"));
    }

    @Test
    void createInvoice_throwsWhenLiqPayReturnsError() throws Exception {
        when(liqPay.api(eq("request"), any()))
                .thenReturn(Map.of("result", "error", "err_description", "bad things", "err_code", "x"));

        LiqPayException ex = assertThrows(LiqPayException.class, () -> service.createInvoice(invoiceDto()));
        assertTrue(ex.getMessage().contains("bad things"));
    }

    @Test
    void createInvoice_wrapsApiExceptions() throws Exception {
        Exception cause = new RuntimeException("network");
        when(liqPay.api(eq("request"), any())).thenThrow(cause);

        LiqPayException ex = assertThrows(LiqPayException.class, () -> service.createInvoice(invoiceDto()));
        assertSame(cause, ex.getCause());
    }

    // ---- createCheckout

    @Test
    @SuppressWarnings("unchecked")
    void createCheckout_delegatesToCnbFormWithSandboxParams() {
        when(liqPay.cnb_form(any())).thenReturn("<form/>");

        String form = service.createCheckout(checkoutDto());

        assertEquals("<form/>", form);
        ArgumentCaptor<Map<String, String>> captor = ArgumentCaptor.forClass(Map.class);
        verify(liqPay).cnb_form(captor.capture());
        Map<String, String> params = captor.getValue();
        assertEquals("pay", params.get("action"));
        assertEquals("3", params.get("version"));
        assertEquals("1", params.get("sandbox"));
        assertEquals("10", params.get("amount"));
        assertEquals("UAH", params.get("currency"));
        assertEquals("desc", params.get("description"));
        assertEquals("o-1", params.get("order_id"));
    }

    // ---- generateCheckoutSignatureAndData

    @Test
    void generateCheckoutSignatureAndData_encodesParamsAndSignsThem() {
        Map<String, String> result = service.generateCheckoutSignatureAndData(checkoutDto());

        String data = result.get("data");
        assertEquals(sign(data), result.get("signature"));

        String json = new String(java.util.Base64.getDecoder().decode(data), StandardCharsets.UTF_8);
        JSONObject decoded = (JSONObject) JSONValue.parse(json);
        assertEquals(PUBLIC_KEY, decoded.get("public_key"));
        assertEquals("pay", decoded.get("action"));
        assertEquals("o-1", decoded.get("order_id"));
        assertEquals("10", decoded.get("amount"));
        assertEquals("UAH", decoded.get("currency"));
    }

    // ---- verifyWebhook

    private static VerifyWebhookDTO webhook(String data, String signature) {
        VerifyWebhookDTO dto = new VerifyWebhookDTO();
        dto.setData(data);
        dto.setSignature(signature);
        return dto;
    }

    @Test
    void verifyWebhook_acceptsValidSignature() {
        assertTrue(service.verifyWebhook(webhook("payload", sign("payload"))));
    }

    @Test
    void verifyWebhook_rejectsWrongSignature() {
        assertFalse(service.verifyWebhook(webhook("payload", sign("other"))));
    }

    @Test
    void verifyWebhook_rejectsMissingFields() {
        assertFalse(service.verifyWebhook(webhook(null, "sig")));
        assertFalse(service.verifyWebhook(webhook("payload", null)));
    }

    // ---- getPaymentStatus

    @Test
    @SuppressWarnings("unchecked")
    void getPaymentStatus_sendsStatusRequest() throws Exception {
        Map<String, Object> response = Map.of("status", "success");
        when(liqPay.api(eq("request"), any())).thenReturn(response);

        assertSame(response, service.getPaymentStatus(42));

        ArgumentCaptor<Map<String, String>> captor = ArgumentCaptor.forClass(Map.class);
        verify(liqPay).api(eq("request"), captor.capture());
        assertEquals(Map.of("action", "status", "version", "3", "order_id", "42"), captor.getValue());
    }

    @Test
    void getPaymentStatus_wrapsApiExceptions() throws Exception {
        when(liqPay.api(eq("request"), any())).thenThrow(new RuntimeException("boom"));

        assertThrows(LiqPayException.class, () -> service.getPaymentStatus(1));
    }
}

package paylab.ordersapi.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import paylab.paymentprovider.dtos.CreateCheckoutDTO;
import paylab.paymentprovider.dtos.CreateInvoiceDTO;
import paylab.paymentprovider.dtos.VerifyWebhookDTO;
import paylab.paymentprovider.services.LiqPayProviderService;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/liqPayProvider")
@RequiredArgsConstructor
public class LiqPayProviderController {

    private final LiqPayProviderService liqPayProviderService;

    @PostMapping("/createInvoice")
    public Map<String, Object> createInvoice(@RequestBody CreateInvoiceDTO dto) {
        return liqPayProviderService.createInvoice(dto);
    }

    @PostMapping("/createCheckoutForm")
    public String createCheckout(@RequestBody CreateCheckoutDTO dto) {
        return liqPayProviderService.createCheckout(dto);
    }

    @PostMapping("/createCheckout")
    public Map<String, String> generateCheckout(@RequestBody CreateCheckoutDTO dto) {
        return liqPayProviderService.generateCheckoutSignatureAndData(dto);
    }

    @GetMapping("/verifyWebhook")
    public boolean verifyWebhook(@RequestBody VerifyWebhookDTO dto) {
        return liqPayProviderService.verifyWebhook(dto);
    }

    @GetMapping("/paymentStatus")
    public Map<String, Object> getPaymentStatus(@RequestParam Integer orderId) {
        return liqPayProviderService.getPaymentStatus(orderId);
    }
}

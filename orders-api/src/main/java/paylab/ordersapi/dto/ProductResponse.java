package paylab.ordersapi.dto;

import paylab.ordersapi.model.Product;

/** Public view of a product: intentionally does not contain s3Key. */
public record ProductResponse(
        String productId,
        String name,
        String description,
        Long priceMinor,
        String currency,
        Boolean active) {

    public static ProductResponse from(Product p) {
        return new ProductResponse(p.getProductId(), p.getName(), p.getDescription(),
                p.getPriceMinor(), p.getCurrency(), p.getActive());
    }
}

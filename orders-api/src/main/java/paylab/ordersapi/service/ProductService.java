package paylab.ordersapi.service;

import java.util.List;
import java.util.Locale;
import java.util.UUID;

import org.springframework.stereotype.Service;

import paylab.ordersapi.dto.ProductRequest;
import paylab.ordersapi.exception.BusinessException;
import paylab.ordersapi.exception.NotFoundException;
import paylab.ordersapi.model.Product;
import paylab.ordersapi.repository.ProductRepository;

@Service
public class ProductService {

    private final ProductRepository repository;

    public ProductService(ProductRepository repository) {
        this.repository = repository;
    }

    public Product create(ProductRequest req) {
        String id = (req.productId() == null || req.productId().isBlank())
                ? UUID.randomUUID().toString() : req.productId();
        if (repository.exists(id)) {
            throw new BusinessException("Product already exists: " + id);
        }
        Product product = new Product();
        product.setProductId(id);
        apply(product, req);
        repository.save(product);
        return product;
    }

    public Product get(String productId) {
        return repository.findById(productId)
                .orElseThrow(() -> new NotFoundException("Product not found: " + productId));
    }

    public List<Product> listActive() {
        return repository.findActive();
    }

    public Product update(String productId, ProductRequest req) {
        Product product = get(productId);
        apply(product, req);
        repository.save(product);
        return product;
    }

    public void delete(String productId) {
        get(productId);
        repository.deleteById(productId);
    }

    private void apply(Product product, ProductRequest req) {
        product.setName(req.name());
        product.setDescription(req.description());
        product.setPriceMinor(req.priceMinor());
        product.setCurrency(req.currency().toLowerCase(Locale.ROOT));
        product.setS3Key(req.s3Key());
        product.setActive(req.active());
    }
}

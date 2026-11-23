package paylab.ordersapi.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import paylab.ordersapi.dto.ProductRequest;
import paylab.ordersapi.dto.ProductResponse;
import paylab.ordersapi.service.ProductService;

@RestController
@RequestMapping("/api/products")
public class ProductController {

    private final ProductService service;

    public ProductController(ProductService service) {
        this.service = service;
    }

    @GetMapping
    public List<ProductResponse> listActive() {
        return service.listActive().stream().map(ProductResponse::from).toList();
    }

    @GetMapping("/{productId}")
    public ProductResponse get(@PathVariable String productId) {
        return ProductResponse.from(service.get(productId));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ProductResponse create(@Valid @RequestBody ProductRequest request) {
        return ProductResponse.from(service.create(request));
    }

    @PutMapping("/{productId}")
    public ProductResponse update(@PathVariable String productId, @Valid @RequestBody ProductRequest request) {
        return ProductResponse.from(service.update(productId, request));
    }

    @DeleteMapping("/{productId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable String productId) {
        service.delete(productId);
    }
}

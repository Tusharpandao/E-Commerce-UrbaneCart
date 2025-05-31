package in.urbanecart.products.controllers;

import in.urbanecart.products.DTO.ProductDto;
import in.urbanecart.products.entity.Product;
import in.urbanecart.products.service.ProductService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class ProductController {


    private final ProductService productService;

    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    @PostMapping("/product")
    public ResponseEntity<ProductDto> createProduct(@RequestBody ProductDto productDto) {
        ProductDto created = productService.createProduct(productDto);
        return ResponseEntity.ok(created);
    }

    @PostMapping("/products/bulk")
    public ResponseEntity<List<ProductDto>> createProducts(@RequestBody List<ProductDto> productDtos) {
        List<ProductDto> created = productService.createProducts(productDtos);
        return ResponseEntity.ok(created);
    }

}

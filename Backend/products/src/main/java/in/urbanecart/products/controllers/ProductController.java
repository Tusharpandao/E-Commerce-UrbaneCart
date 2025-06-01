package in.urbanecart.products.controllers;

import in.urbanecart.products.DTO.BulkProductResponse;
import in.urbanecart.products.DTO.ProductDto;
import in.urbanecart.products.service.ProductService;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/products")
public class ProductController {


    private final ProductService productService;

    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    @PostMapping("/add")
    public ResponseEntity<ProductDto> createProduct(@RequestBody ProductDto productDto) {
        ProductDto created = productService.createProduct(productDto);
        return ResponseEntity.ok(created);
    }

    @PostMapping("/bulk")
    public ResponseEntity<BulkProductResponse> createProducts(@RequestBody List<ProductDto> productDtos) {
        return ResponseEntity.ok(productService.createProducts(productDtos));
    }

    @GetMapping("/info")
    public ResponseEntity<ProductDto> getProductById(@RequestParam Long id){
        return ResponseEntity.ok(productService.getProductById(id));
    }

    @GetMapping
    public ResponseEntity<Page<ProductDto>> getProducts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Page<ProductDto> result = productService.getPaginatedProducts(page, size);
        return ResponseEntity.ok(result);
    }

}

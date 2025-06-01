package in.urbanecart.products.service;

import in.urbanecart.products.DTO.BulkProductResponse;
import in.urbanecart.products.DTO.ProductDto;
import org.springframework.data.domain.Page;

import java.util.List;


public interface ProductService {


    ProductDto createProduct(ProductDto productDto);
   BulkProductResponse createProducts(List<ProductDto> productDtos);

    ProductDto getProductById(Long id);

    public Page<ProductDto> getPaginatedProducts(int page, int size);
}

package in.urbanecart.products.service;

import in.urbanecart.products.DTO.ProductDto;

import java.util.List;


public interface ProductService {


    ProductDto createProduct(ProductDto productDto);
    List<ProductDto> createProducts(List<ProductDto> productDtos);

}

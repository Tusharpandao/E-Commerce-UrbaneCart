package in.urbanecart.products.DTO;


import lombok.*;

import java.math.BigDecimal;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductDto {

    private Long id;
    private String title;
    private String description;
    private String category;
    private BigDecimal price;
    private BigDecimal discountPercentage;
    private BigDecimal rating;
    private Integer stock;

    private List<String> tags;

    private String brand;
    private String sku;
    private Integer weight;

    private DimensionsDTO dimensions;


    private String warrantyInformation;
    private String shippingInformation;
    private String availabilityStatus;

    private List<ReviewDto> reviews;

    private String returnPolicy;
    private Integer minimumOrderQuantity;

   private  MetaDTO meta;

    private List<String> images;
    private String thumbnail;
}


package in.urbanecart.products.mapper;

import in.urbanecart.products.DTO.DimensionsDTO;
import in.urbanecart.products.DTO.MetaDTO;
import in.urbanecart.products.DTO.ProductDto;
import in.urbanecart.products.DTO.ReviewDto;
import in.urbanecart.products.entity.Product;
import in.urbanecart.products.entity.ProductImage;
import in.urbanecart.products.entity.Tag;

import java.util.List;

public class ProductMapper {

    public static ProductDto toDto(Product product) {
        if (product == null) return null;

        return ProductDto.builder()
                .id(product.getId())
                .title(product.getTitle())
                .description(product.getDescription())
                .category(product.getCategory().getName())
                .price(product.getPrice())
                .discountPercentage(product.getDiscountPercentage())
                .rating(product.getRating())
                .stock(product.getStock())
                .brand(product.getBrand())
                .sku(product.getSku())
                .weight(product.getWeight())
                .thumbnail(product.getThumbnail())
                .minimumOrderQuantity(product.getMinimumOrderQuantity())
                .warrantyInformation(product.getWarrantyInformation())
                .shippingInformation(product.getShippingInformation())
                .availabilityStatus(product.getAvailabilityStatus())
                .returnPolicy(product.getReturnPolicy())

                .tags(product.getTags() != null ?
                        product.getTags().stream().map(Tag::getName).toList() :
                        List.of())

                .dimensions(DimensionsDTO.builder()
                        .width(product.getWidth())
                        .height(product.getHeight())
                        .depth(product.getDepth())
                        .build())

                .meta(MetaDTO.builder()
                        .createdAt(product.getCreatedAt())
                        .updatedAt(product.getUpdatedAt())
                        .barcode(product.getBarcode())
                        .qrCode(product.getQrCode())
                        .build())

                .images(product.getImages() != null ?
                        product.getImages().stream().map(ProductImage::getUrl).toList() :
                        List.of())

                .reviews(product.getReviews() != null ?
                        product.getReviews().stream().map(r -> ReviewDto.builder()
                                .rating(r.getRating())
                                .comment(r.getComment())
                                .date(r.getDate())
                                .reviewerName(r.getReviewerName())
                                .reviewerEmail(r.getReviewerEmail())
                                .build()).toList() :
                        List.of())

                .build();
    }

    public static List<ProductDto> toDtoList(List<Product> products) {
        if (products == null || products.isEmpty()) return List.of();
        return products.stream().map(ProductMapper::toDto).toList();
    }
}

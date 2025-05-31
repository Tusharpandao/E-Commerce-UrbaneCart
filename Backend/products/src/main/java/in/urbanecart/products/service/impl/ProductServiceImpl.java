package in.urbanecart.products.service.impl;


import in.urbanecart.products.DTO.ProductDto;
import in.urbanecart.products.entity.Product;
import in.urbanecart.products.entity.ProductImage;
import in.urbanecart.products.entity.Review;
import in.urbanecart.products.entity.Tag;
import in.urbanecart.products.repository.ProductRepository;
import in.urbanecart.products.repository.TagRepository;
import in.urbanecart.products.service.ProductService;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.stream.Collectors;

@Service
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;
    private final TagRepository tagRepository;

    public ProductServiceImpl(ProductRepository productRepository, TagRepository tagRepository) {
        this.productRepository = productRepository;
        this.tagRepository = tagRepository;
    }

    @Override
    public ProductDto createProduct(ProductDto dto) {

        // 1. Fetch existing tags from DB
        List<Tag> existingTags = tagRepository.findAllByNameIn(dto.getTags());
        Set<String> existingTagNames = existingTags.stream()
                .map(Tag::getName)
                .collect(Collectors.toSet());

        // 2. Find names that are not yet in DB
        List<Tag> newTags = dto.getTags().stream()
                .filter(name -> !existingTagNames.contains(name))
                .map(name -> new Tag(name))
                .collect(Collectors.toList());

        // 3. Save new tags in one batch
        List<Tag> savedNewTags = tagRepository.saveAll(newTags);

        // 4. Combine all tags
        List<Tag> allTags = new ArrayList<>();
        allTags.addAll(existingTags);
        allTags.addAll(savedNewTags);

        // 5. Use allTags in your Product
        Product product = Product.builder()
                .title(dto.getTitle())
                .description(dto.getDescription())
                .category(dto.getCategory())
                .price(dto.getPrice())
                .discountPercentage(dto.getDiscountPercentage())
                .rating(dto.getRating())
                .stock(dto.getStock())
                .tags(allTags)
                .brand(dto.getBrand())
                .images(dto.getImages().stream()
                        .map(image -> new ProductImage(null, image))
                        .collect(Collectors.toList()))
                .sku(dto.getSku())
                .weight(dto.getWeight())
                .width(dto.getDimensions().getWidth())
                .height(dto.getDimensions().getHeight())
                .depth(dto.getDimensions().getDepth())
                .warrantyInformation(dto.getWarrantyInformation())
                .shippingInformation(dto.getShippingInformation())
                .availabilityStatus(dto.getAvailabilityStatus())
                .returnPolicy(dto.getReturnPolicy())
                .minimumOrderQuantity(dto.getMinimumOrderQuantity())
                .createdAt(dto.getMeta().getCreatedAt())
                .updatedAt(dto.getMeta().getUpdatedAt())
                .barcode(dto.getMeta().getBarcode())
                .qrCode(dto.getMeta().getQrCode())
                .thumbnail(dto.getThumbnail())
                .build();

        if (dto.getReviews() != null) {
            product.setReviews(dto.getReviews().stream()
                    .map(r -> Review.builder()
                            .rating(r.getRating())
                            .comment(r.getComment())
                            .date(r.getDate())
                            .reviewerName(r.getReviewerName())
                            .reviewerEmail(r.getReviewerEmail())
                            .product(product)
                            .build())
                    .collect(Collectors.toList()));
        }

        Product saved = productRepository.save(product);
        dto.setId(saved.getId());
        return dto;

    }

    @Override
    public List<ProductDto> createProducts(List<ProductDto> dtos) {
        // Step 1: Collect unique tag names first
        Set<String> tagNames = dtos.stream()
                .flatMap(dto -> dto.getTags().stream())
                .map(name -> name.trim().toLowerCase())
                .collect(Collectors.toSet());

        // Step 2: Preload all tags to prevent duplicate inserts
        Map<String, Tag> tagMap = preloadTags(tagNames);

        // Step 3: Run threaded batch processing
        int threadCount = 4;
        int batchSize = (int) Math.ceil((double) dtos.size() / threadCount);

        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        List<Future<List<ProductDto>>> futures = new ArrayList<>();

        for (int i = 0; i < dtos.size(); i += batchSize) {
            int end = Math.min(i + batchSize, dtos.size());
            List<ProductDto> batch = dtos.subList(i, end);

            // pass the shared tagMap to all threads
            futures.add(executor.submit(() -> processAndSaveBatch(batch, tagMap)));
        }

        List<ProductDto> allResults = new ArrayList<>();
        for (Future<List<ProductDto>> future : futures) {
            try {
                allResults.addAll(future.get());
            } catch (Exception e) {
                throw new RuntimeException("Batch processing failed", e);
            }
        }

        executor.shutdown();
        return allResults;
    }


    @Transactional
    public synchronized Map<String, Tag> preloadTags(Set<String> tagNames) {
        List<Tag> existingTags = tagRepository.findAllByNameIn(tagNames);
        Map<String, Tag> tagMap = existingTags.stream()
                .collect(Collectors.toMap(tag -> tag.getName().toLowerCase(), t -> t));

        // Create tags that don’t exist
        List<Tag> newTags = tagNames.stream()
                .filter(name -> !tagMap.containsKey(name))
                .map(Tag::new)
                .collect(Collectors.toList());

        if (!newTags.isEmpty()) {
            List<Tag> savedTags = tagRepository.saveAll(newTags);
            for (Tag tag : savedTags) {
                tagMap.put(tag.getName().toLowerCase(), tag);
            }
        }

        return tagMap;
    }

    @Transactional
    private List<ProductDto> processAndSaveBatch(List<ProductDto> dtos, Map<String, Tag> tagMap) {
        List<Product> products = dtos.stream().map(dto -> {
            List<Tag> tags = dto.getTags().stream()
                    .map(tagName -> tagMap.get(tagName.trim().toLowerCase()))
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());

            Product product = Product.builder()
                    .title(dto.getTitle())
                    .description(dto.getDescription())
                    .category(dto.getCategory())
                    .price(dto.getPrice())
                    .discountPercentage(dto.getDiscountPercentage())
                    .rating(dto.getRating())
                    .stock(dto.getStock())
                    .tags(tags)
                    .brand(dto.getBrand())
                    .sku(dto.getSku())
                    .weight(dto.getWeight())
                    .width(dto.getDimensions().getWidth())
                    .height(dto.getDimensions().getHeight())
                    .depth(dto.getDimensions().getDepth())
                    .warrantyInformation(dto.getWarrantyInformation())
                    .shippingInformation(dto.getShippingInformation())
                    .availabilityStatus(dto.getAvailabilityStatus())
                    .returnPolicy(dto.getReturnPolicy())
                    .minimumOrderQuantity(dto.getMinimumOrderQuantity())
                    .createdAt(dto.getMeta().getCreatedAt())
                    .updatedAt(dto.getMeta().getUpdatedAt())
                    .barcode(dto.getMeta().getBarcode())
                    .qrCode(dto.getMeta().getQrCode())
                    .thumbnail(dto.getThumbnail())
                    .build();

            List<ProductImage> images = dto.getImages().stream()
                    .map(imageUrl -> {
                        ProductImage img = new ProductImage();
                        img.setUrl(imageUrl);
                        img.setProduct(product);
                        return img;
                    })
                    .collect(Collectors.toList());
            product.setImages(images);

            if (dto.getReviews() != null) {
                product.setReviews(dto.getReviews().stream()
                        .map(r -> Review.builder()
                                .rating(r.getRating())
                                .comment(r.getComment())
                                .date(r.getDate())
                                .reviewerName(r.getReviewerName())
                                .reviewerEmail(r.getReviewerEmail())
                                .product(product)
                                .build())
                        .collect(Collectors.toList()));
            }

            return product;
        }).collect(Collectors.toList());

        List<Product> saved = productRepository.saveAll(products);

        for (int i = 0; i < saved.size(); i++) {
            dtos.get(i).setId(saved.get(i).getId());
        }

        return dtos;
    }

}
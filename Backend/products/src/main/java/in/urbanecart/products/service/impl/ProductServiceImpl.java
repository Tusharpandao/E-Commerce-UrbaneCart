package in.urbanecart.products.service.impl;


import in.urbanecart.products.DTO.BulkProductResponse;
import in.urbanecart.products.DTO.ProductDto;
import in.urbanecart.products.entity.*;
import in.urbanecart.products.exception.CategoryNotFound;
import in.urbanecart.products.exception.ProductNotFound;
import in.urbanecart.products.mapper.ProductMapper;
import in.urbanecart.products.repository.CategoryRepository;
import in.urbanecart.products.repository.ProductRepository;
import in.urbanecart.products.repository.TagRepository;
import in.urbanecart.products.service.ProductService;
import jakarta.transaction.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
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
    private final CategoryRepository categoryRepository;

    public ProductServiceImpl(ProductRepository productRepository, TagRepository tagRepository,
                              CategoryRepository categoryRepository) {

        this.productRepository = productRepository;
        this.tagRepository = tagRepository;
        this.categoryRepository=categoryRepository;
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

        // 5. Find the Category in DB
        String input = dto.getCategory().trim();
        Category category = categoryRepository.findByNameIgnoreCaseOrSlugIgnoreCase(input,input)
                .orElseThrow(() -> new CategoryNotFound("Category not found: " + input));


        // 6. Use allTags in your Product
        Product product = Product.builder()
                .title(dto.getTitle())
                .description(dto.getDescription())
                .category(category)
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
    public ProductDto getProductById(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ProductNotFound("Product not found by id: " + id));

        return ProductMapper.toDto(product);
    }

    @Override
    public Page<ProductDto> getPaginatedProducts(int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<Product> productPage = productRepository.findAll(pageable);
        return productPage.map(ProductMapper::toDto);
    }

    @Override
    public BulkProductResponse createProducts(List<ProductDto> dtos) {
        Set<String> tagNames = dtos.stream()
                .flatMap(dto -> dto.getTags().stream())
                .map(name -> name.trim().toLowerCase())
                .collect(Collectors.toSet());

        Map<String, Tag> tagMap = preloadTags(tagNames);

        int threadCount = 4;
        int batchSize = (int) Math.ceil((double) dtos.size() / threadCount);

        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        List<Future<Map<String, List<ProductDto>>>> futures = new ArrayList<>();

        for (int i = 0; i < dtos.size(); i += batchSize) {
            int end = Math.min(i + batchSize, dtos.size());
            List<ProductDto> batch = dtos.subList(i, end);
            futures.add(executor.submit(() -> processAndSaveBatch(batch, tagMap)));
        }

        List<ProductDto> added = new ArrayList<>();
        List<ProductDto> failed = new ArrayList<>();

        for (Future<Map<String, List<ProductDto>>> future : futures) {
            try {
                Map<String, List<ProductDto>> result = future.get();
                added.addAll(result.getOrDefault("added", Collections.emptyList()));
                failed.addAll(result.getOrDefault("failed", Collections.emptyList()));
            } catch (Exception e) {
                throw new RuntimeException("Batch processing failed", e);
            }
        }

        executor.shutdown();

        BulkProductResponse response = new BulkProductResponse();
        response.setAdded(added);
        response.setFailed(failed);
        return response;
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
    private Map<String, List<ProductDto>> processAndSaveBatch(List<ProductDto> dtos, Map<String, Tag> tagMap) {
        List<Product> productsToSave = new ArrayList<>();
        List<ProductDto> added = new ArrayList<>();
        List<ProductDto> failed = new ArrayList<>();

        for (ProductDto dto : dtos) {
            String input = dto.getCategory().trim();
            Optional<Category> categoryOpt = categoryRepository.findByNameIgnoreCaseOrSlugIgnoreCase(input,input);

            if (categoryOpt.isEmpty()) {
                failed.add(dto);
                continue;
            }

            try {
                Product product = buildProductFromDto(dto, tagMap, categoryOpt.get());
                productsToSave.add(product);
            } catch (Exception e) {
                failed.add(dto);
            }
        }

        List<Product> saved = productRepository.saveAll(productsToSave);

        for (int i = 0; i < saved.size(); i++) {
            ProductDto dto = dtos.get(i);
            dto.setId(saved.get(i).getId());
            added.add(dto);
        }

        Map<String, List<ProductDto>> result = new HashMap<>();
        result.put("added", added);
        result.put("failed", failed);
        return result;
    }


    private Product buildProductFromDto(ProductDto dto, Map<String, Tag> tagMap, Category category) {
        List<Tag> tags = dto.getTags().stream()
                .map(tagName -> tagMap.get(tagName.trim().toLowerCase()))
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        Product product = Product.builder()
                .title(dto.getTitle())
                .description(dto.getDescription())
                .category(category)
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
                .map(imageUrl -> new ProductImage(null, imageUrl, product))
                .collect(Collectors.toList());
        product.setImages(images);

        if (dto.getReviews() != null) {
            List<Review> reviews = dto.getReviews().stream()
                    .map(r -> Review.builder()
                            .rating(r.getRating())
                            .comment(r.getComment())
                            .date(r.getDate())
                            .reviewerName(r.getReviewerName())
                            .reviewerEmail(r.getReviewerEmail())
                            .product(product)
                            .build())
                    .collect(Collectors.toList());
            product.setReviews(reviews);
        }

        return product;
    }


}
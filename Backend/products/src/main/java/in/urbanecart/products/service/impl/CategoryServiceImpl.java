package in.urbanecart.products.service.impl;

import in.urbanecart.products.DTO.CategoryDTO;
import in.urbanecart.products.entity.Category;
import in.urbanecart.products.repository.CategoryRepository;
import in.urbanecart.products.service.CategoryService;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class CategoryServiceImpl implements CategoryService {


    private final CategoryRepository categoryRepository;

    public CategoryServiceImpl(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    @Override
    public Category create(CategoryDTO requestCategory) {

        String categoryName = requestCategory.getName().trim();
        Category category = categoryRepository.findByNameIgnoreCase(categoryName)
                .orElseGet(() -> {
                    Category newCategory = new Category();
                    newCategory.setName(categoryName);
                    newCategory.setSlug(generateSlug(categoryName));
                    newCategory.setImageUrl(requestCategory.getImageUrl());
                    return categoryRepository.save(newCategory);
                });
        return  categoryRepository.save(category);
    }

    @Override
    public Map<String, List<Category>> saveAll(List<CategoryDTO> categories) {

        // 1. Extract all names to check existing ones in bulk
        Set<String> inputNames = categories.stream()
                .map(dto -> dto.getName().trim().toLowerCase())
                .collect(Collectors.toSet());

        // 2. Load existing categories from DB (avoid duplicates)
        List<Category> existingCategories = categoryRepository.findAllByNameInIgnoreCase(inputNames);

        Set<String> existingNames = existingCategories.stream()
                .map(cat -> cat.getName().toLowerCase())
                .collect(Collectors.toSet());

        // 3. Filter out already existing ones and map to new Category entities
        List<Category> newCategories = categories.stream()
                .filter(dto -> !existingNames.contains(dto.getName().trim().toLowerCase()))
                .map(dto -> {
                    Category category = new Category();
                    category.setName(dto.getName().trim());
                    category.setSlug(generateSlug(dto.getName().trim()));
                    category.setImageUrl(dto.getImageUrl());
                    return category;
                })
                .collect(Collectors.toList());

        // 4. Save new categories only
        List<Category> savedCategories = categoryRepository.saveAll(newCategories);

        // 5. Return combined list: existing + newly saved
        Map<String, List<Category>> result = new HashMap<>();
        result.put("existing", existingCategories);
        result.put("new", savedCategories);

        return result;
    }


    private String generateSlug(String input) {
        return input.toLowerCase().replaceAll("[^a-z0-9]+", "-").replaceAll("-+$", "");
    }



}

package in.urbanecart.products.controllers;

import in.urbanecart.products.DTO.CategoryDTO;
import in.urbanecart.products.entity.Category;
import in.urbanecart.products.service.CategoryService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/products/category")
public class CategoryController {

    private final CategoryService categoryService;

    public CategoryController(CategoryService categoryService) {
        this.categoryService = categoryService;
    }

    @PostMapping("/create")
    public ResponseEntity<Category> createCategory(@RequestBody CategoryDTO categoryDTO){

        return  ResponseEntity.ok(categoryService.create(categoryDTO));
    }

        @PostMapping("/create/bulk")
    public  ResponseEntity<Map<String ,List<Category>>> saveAll(@RequestBody List<CategoryDTO> categorys){
        return  ResponseEntity.ok(categoryService.saveAll(categorys));
    }
}

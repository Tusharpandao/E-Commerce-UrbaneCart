package in.urbanecart.products.service;


import in.urbanecart.products.DTO.CategoryDTO;
import in.urbanecart.products.entity.Category;

import java.util.List;
import java.util.Map;

public interface CategoryService {


    public Category create(CategoryDTO category) ;

    Map<String ,List<Category>> saveAll(List<CategoryDTO> categorys);
}

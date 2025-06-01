package in.urbanecart.products.repository;

import in.urbanecart.products.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface CategoryRepository  extends JpaRepository<Category ,Long> {
    Optional<Category> findByNameIgnoreCase(String categoryName);


    List<Category> findAllByNameInIgnoreCase(Set<String> names);

    Optional<Category> findByNameIgnoreCaseOrSlugIgnoreCase(String name, String slug);


}

package in.urbanecart.products.repository;

import in.urbanecart.products.entity.Tag;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface TagRepository extends JpaRepository<Tag,Long> {

    Optional<Tag> findByName(String name);

    List<Tag> findAllByNameIn(Collection<String> names);

}

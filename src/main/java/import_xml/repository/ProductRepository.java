package import_xml.repository;

import import_xml.model.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {
    Optional<Product> findByProductId(String productId);

    Page<Product> findByIsActive(boolean isActive, Pageable pageable);

    @Query("SELECT p FROM Product p JOIN p.categories c WHERE c.categoryId = :categoryId AND p.isActive = :isActive")
    Page<Product> findByCategoryIdAndIsActive(
            @Param("categoryId") String categoryId,
            @Param("isActive") boolean isActive,
            Pageable pageable);

    @Query("SELECT p FROM Product p WHERE " +
            "(LOWER(p.name) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
            "LOWER(p.article) LIKE LOWER(CONCAT('%', :search, '%'))) AND " +
            "p.isActive = :isActive")
    Page<Product> findByNameContainingOrArticleContainingAndIsActive(
            @Param("search") String nameSearch,
            @Param("search") String articleSearch,
            @Param("isActive") boolean isActive,
            Pageable pageable);
}

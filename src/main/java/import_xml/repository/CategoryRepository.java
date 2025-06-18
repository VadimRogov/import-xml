package import_xml.repository;

import import_xml.model.Category;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {
    Optional<Category> findByCategoryId(String categoryId);

    Page<Category> findByIsActive(boolean isActive, Pageable pageable);

    @Query("SELECT c FROM Category c WHERE c.parentId = :parentId AND c.isActive = :isActive")
    Page<Category> findByParentIdAndIsActive(
            @Param("parentId") String parentId,
            @Param("isActive") boolean isActive,
            Pageable pageable);

    @Query("SELECT c FROM Category c WHERE c.isActive = :isActive ORDER BY c.level ASC, c.sortOrder ASC")
    List<Category> findByIsActiveOrderByLevelAscSortOrderAsc(@Param("isActive") boolean isActive);

    @Query("SELECT DISTINCT c FROM Category c " +
            "LEFT JOIN FETCH c.products p " +
            "WHERE c.categoryId = :categoryId AND c.isActive = :isActive")
    Page<Category> findByCategoryIdAndIsActiveWithProducts(
            @Param("categoryId") String categoryId,
            @Param("isActive") boolean isActive,
            Pageable pageable);
}

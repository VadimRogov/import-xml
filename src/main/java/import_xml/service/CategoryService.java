package import_xml.service;

import import_xml.model.Category;
import import_xml.repository.CategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CategoryService {
    private final CategoryRepository categoryRepository;

    public Page<Category> findCategories(String parentId, Boolean active, Pageable pageable) {
        if (parentId != null) {
            return categoryRepository.findByParentIdAndIsActive(parentId, active != null ? active : true, pageable);
        }
        return categoryRepository.findByIsActive(active != null ? active : true, pageable);
    }

    public Optional<Category> findByCategoryId(String categoryId) {
        return categoryRepository.findByCategoryId(categoryId);
    }

    public List<Category> getCategoryTree(boolean active) {
        List<Category> allCategories = categoryRepository.findByIsActiveOrderByLevelAscSortOrderAsc(active);
        Map<String, List<Category>> categoriesByParent = allCategories.stream()
                .collect(Collectors.groupingBy(category ->
                        category.getParentId() != null ? category.getParentId() : "root"));

        return buildCategoryTree("root", categoriesByParent);
    }

    private List<Category> buildCategoryTree(String parentId, Map<String, List<Category>> categoriesByParent) {
        List<Category> children = categoriesByParent.get(parentId);
        if (children == null) {
            return new ArrayList<>();
        }

        return children.stream()
                .peek(category -> {
                    List<Category> childCategories = buildCategoryTree(category.getCategoryId(), categoriesByParent);
                    if (!childCategories.isEmpty()) {
                        category.setProducts(null); // Очищаем продукты для родительских категорий
                    }
                })
                .collect(Collectors.toList());
    }

    public Page<Category> findCategoryWithProducts(String categoryId, Pageable pageable) {
        return categoryRepository.findByCategoryIdAndIsActiveWithProducts(categoryId, true, pageable);
    }
}
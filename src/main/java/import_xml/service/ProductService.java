package import_xml.service;

import import_xml.model.Product;
import import_xml.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProductService {
    private final ProductRepository productRepository;

    public Page<Product> findProducts(String categoryId, String search, Boolean active, Pageable pageable) {
        if (categoryId != null) {
            return productRepository.findByCategoryIdAndIsActive(categoryId, active != null ? active : true, pageable);
        }
        if (search != null) {
            return productRepository.findByNameContainingOrArticleContainingAndIsActive(
                    search, search, active != null ? active : true, pageable);
        }
        return productRepository.findByIsActive(active != null ? active : true, pageable);
    }

    public Optional<Product> findByProductId(String productId) {
        return productRepository.findByProductId(productId);
    }

    public Page<Product> findByCategoryId(String categoryId, Pageable pageable) {
        return productRepository.findByCategoryIdAndIsActive(categoryId, true, pageable);
    }

    public Page<Product> searchProducts(String query, Pageable pageable) {
        return productRepository.findByNameContainingOrArticleContainingAndIsActive(
                query, query, true, pageable);
    }
}

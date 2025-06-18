package import_xml.service;

import import_xml.model.Category;
import import_xml.model.Product;
import import_xml.model.opencart.OpenCartCategory;
import import_xml.model.opencart.OpenCartProduct;
import import_xml.repository.CategoryRepository;
import import_xml.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class SyncService {

    private final ImportXmlApiService importXmlApiService;
    private final OpenCartService openCartService;
    private final CategoryRepository categoryRepository;
    private final ProductRepository productRepository;

    @Scheduled(cron = "${opencart.sync.cron}")
    @Transactional
    public void syncData() {
        log.info("Начинаем синхронизацию данных в {}", LocalDateTime.now());
        try {
            syncCategories();
            syncProducts();
            log.info("Данные синхронизированы успешно в {}", LocalDateTime.now());
        } catch (Exception e) {
            log.error("Ошибка при синхронизации данных", e);
            throw new RuntimeException("Не удалось синхронизировать данные", e);
        }
    }

    private void syncCategories() {
        log.info("Начинаем синхронизацию категорий");
        int page = 0;
        int size = openCartService.getConfig().getSync().getBatchSize();

        while (true) {
            Page<Category> categories = categoryRepository.findAll(PageRequest.of(page, size));
            if (!categories.hasContent()) {
                break;
            }

            for (Category category : categories) {
                try {
                    OpenCartCategory openCartCategory = convertToOpenCartCategory(category);
                    openCartService.updateCategory(openCartCategory);
                } catch (Exception e) {
                    log.error("Ошибка обновления категории: {}", category.getCategoryId(), e);
                }
            }

            if (!categories.hasNext()) {
                break;
            }
            page++;
        }
    }

    private void syncProducts() {
        log.info("Начинаем синхронизацию продуктов");
        int page = 0;
        int size = openCartService.getConfig().getSync().getBatchSize();

        while (true) {
            Page<Product> products = productRepository.findAll(PageRequest.of(page, size));
            if (!products.hasContent()) {
                break;
            }

            for (Product product : products) {
                try {
                    OpenCartProduct openCartProduct = convertToOpenCartProduct(product);
                    openCartService.updateProduct(openCartProduct);
                } catch (Exception e) {
                    log.error("Ошибка обновления продукта: {}", product.getProductId(), e);
                }
            }

            if (!products.hasNext()) {
                break;
            }
            page++;
        }
    }

    private OpenCartCategory convertToOpenCartCategory(Category category) {
        OpenCartCategory openCartCategory = new OpenCartCategory();
        openCartCategory.setCategoryId(Long.parseLong(category.getCategoryId()));
        openCartCategory.setName(category.getName());
        openCartCategory.setDescription(category.getDescription());
        openCartCategory.setStatus(category.getIsActive());
        openCartCategory.setSortOrder(category.getSortOrder());
        openCartCategory.setImage(category.getImage());

        // Установка parent_id
        Optional<Category> parent = categoryRepository.findByCategoryId(category.getParentId());
        parent.ifPresent(p -> openCartCategory.setParentId(Long.parseLong(p.getCategoryId())));

        // Добавление описаний для разных языков
        OpenCartCategory.OpenCartCategoryDescription description = new OpenCartCategory.OpenCartCategoryDescription();
        description.setLanguageId(1); // ID языка по умолчанию
        description.setName(category.getName());
        description.setDescription(category.getDescription());
        description.setMetaTitle(category.getName());
        description.setMetaDescription(category.getDescription());
        openCartCategory.setDescriptions(List.of(description));

        return openCartCategory;
    }

    private OpenCartProduct convertToOpenCartProduct(Product product) {
        OpenCartProduct openCartProduct = new OpenCartProduct();
        openCartProduct.setProductId(Long.parseLong(product.getProductId()));
        openCartProduct.setModel(product.getArticle());
        openCartProduct.setSku(product.getArticle());
        openCartProduct.setQuantity(product.getQuantity());
        openCartProduct.setPrice(product.getPrice());
        openCartProduct.setStatus(product.getIsActive());
        openCartProduct.setImage(product.getImage());

        // Добавление описаний для разных языков
        OpenCartProduct.OpenCartProductDescription description = new OpenCartProduct.OpenCartProductDescription();
        description.setLanguageId(1); // ID языка по умолчанию
        description.setName(product.getName());
        description.setDescription(product.getDescription());
        description.setMetaTitle(product.getName());
        description.setMetaDescription(product.getDescription());
        openCartProduct.setDescriptions(List.of(description));

        // Добавление категорий
        List<Long> categoryIds = product.getCategories().stream()
                .map(category -> Long.parseLong(category.getCategoryId()))
                .collect(Collectors.toList());
        openCartProduct.setCategoryIds(categoryIds);

        return openCartProduct;
    }
}

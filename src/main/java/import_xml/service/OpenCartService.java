package import_xml.service;

import import_xml.config.OpenCartConfig;
import import_xml.model.opencart.OpenCartCategory;
import import_xml.model.opencart.OpenCartProduct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

@Service
@Slf4j
@RequiredArgsConstructor
public class OpenCartService {
    private final RestTemplate restTemplate;
    private final OpenCartConfig config;

    public OpenCartConfig getConfig() {
        return config;
    }

    public void updateCategory(OpenCartCategory category) {
        String url = UriComponentsBuilder.fromHttpUrl(config.getBaseUrl())
                .path("/api/categories")
                .build()
                .toUriString();

        HttpHeaders headers = createHeaders();
        HttpEntity<OpenCartCategory> request = new HttpEntity<>(category, headers);

        try {
            restTemplate.exchange(url, HttpMethod.POST, request, Void.class);
            log.info("Category updated successfully: {}", category.getCategoryId());
        } catch (Exception e) {
            log.error("Ошибка при обновлении категории в OpenCart: {}", category.getCategoryId(), e);
            throw new RuntimeException("Не удалось обновить категорию в OpenCart: " + category.getCategoryId(), e);
        }
    }

    public void updateProduct(OpenCartProduct product) {
        String url = UriComponentsBuilder.fromHttpUrl(config.getBaseUrl())
                .path("/api/products")
                .build()
                .toUriString();

        HttpHeaders headers = createHeaders();
        HttpEntity<OpenCartProduct> request = new HttpEntity<>(product, headers);

        try {
            restTemplate.exchange(url, HttpMethod.POST, request, Void.class);
            log.info("Product updated successfully: {}", product.getProductId());
        } catch (Exception e) {
            log.error("Ошибка при обновлении продукта в OpenCart: {}", product.getProductId(), e);
            throw new RuntimeException("Не удалось обновить продукт в OpenCart: " + product.getProductId(), e);
        }
    }

    private HttpHeaders createHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.set("X-API-Key", config.getApiKey());
        headers.set("Content-Type", "application/json");
        return headers;
    }
}

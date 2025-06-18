package import_xml.service;

import import_xml.config.ImportXmlProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;

import jakarta.annotation.PostConstruct;
import java.io.File;
import java.io.FileOutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Base64;
import java.util.concurrent.TimeUnit;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Slf4j
@Service
@RequiredArgsConstructor
public class ImportXmlApiService {
    private final RestTemplate restTemplate;
    private final ImportXmlProperties properties;
    private final XmlProcessingService xmlProcessingService;

    @Value("${project111.api.base-url}")
    private String baseUrl;

    @Value("${project111.api.username}")
    private String username;

    @Value("${project111.api.password}")
    private String password;

    @Value("${project111.import.directory}")
    private String importDirectory;

    private static final long RATE_LIMIT_DELAY = 200; // 200ms между запросами

    @PostConstruct
    public void init() {
        try {
            Path tempDir = Paths.get(properties.getXmlDownload().getTempDir());
            if (!Files.exists(tempDir)) {
                Files.createDirectories(tempDir);
            }
        } catch (Exception e) {
            log.error("Error during initialization", e);
            throw new RuntimeException("Failed to initialize service", e);
        }
    }

    @Scheduled(cron = "${project111.sync.cron}")
    public void syncData() {
        log.info("Starting data sync from Project111 API at {}", LocalDateTime.now());
        try {
            // Скачиваем и обрабатываем файлы
            downloadAndProcessFile(properties.getImportConfig().getFiles().getProduct(), "products");
            downloadAndProcessFile(properties.getImportConfig().getFiles().getStock(), "stock");

            log.info("Data sync completed successfully at {}", LocalDateTime.now());
        } catch (Exception e) {
            log.error("Error during data sync", e);
            throw new RuntimeException("Failed to sync data", e);
        }
    }

    @Retryable(
            value = {Exception.class},
            maxAttempts = 3,
            backoff = @Backoff(delay = 5000)
    )
    public void importAllData() {
        log.info("Starting full data import at {}", LocalDateTime.now());
        try {
            Path importPath = Paths.get(importDirectory);
            if (!Files.exists(importPath)) {
                Files.createDirectories(importPath);
            }

            // Скачиваем и обрабатываем все типы файлов
            downloadAndProcessFile("project111_product.xml", "products");
            downloadAndProcessFile("project111_stock.xml", "stock");
            downloadAndProcessFile("project111_tree.xml", "tree");
            downloadAndProcessFile("project111_filters.xml", "filters");
            downloadAndProcessFile("project111_complects.xml", "complects");

            log.info("Full data import completed successfully at {}", LocalDateTime.now());
        } catch (Exception e) {
            log.error("Error during data import", e);
            throw new RuntimeException("Failed to import data", e);
        }
    }

    @Retryable(
            value = {Exception.class},
            backoff = @Backoff(delay = 5000)
    )
    private void downloadAndProcessFile(String fileName, String fileType) {
        // Формируем правильный путь к файлу в API
        String url = String.format("%s/export/v2/catalogue/%s", baseUrl, fileName);
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        String localFileName = String.format("%s_%s.xml", fileType, timestamp);
        File localFile = new File(importDirectory, localFileName);

        log.info("Downloading {} from {}", fileName, url);
        try {
            byte[] fileContent = restTemplate.getForObject(url, byte[].class);
            if (fileContent == null) {
                throw new RuntimeException("Empty response from server");
            }

            try (FileOutputStream fos = new FileOutputStream(localFile)) {
                fos.write(fileContent);
            }

            log.info("Processing {} file", fileName);
            processFileByType(localFile, fileType);

            // Удаляем файл после обработки
            Files.deleteIfExists(localFile.toPath());
            log.info("{} file processed and deleted", fileName);

            // Задержка между запросами
            TimeUnit.MILLISECONDS.sleep(RATE_LIMIT_DELAY);
        } catch (Exception e) {
            log.error("Error processing {} file", fileName, e);
            throw new RuntimeException("Failed to process " + fileName, e);
        }
    }

    private void processFileByType(File file, String fileType) {
        switch (fileType) {
            case "products":
                xmlProcessingService.processProductsXml(file);
                break;
            case "stock":
                xmlProcessingService.processStockXml(file);
                break;
            case "tree":
                xmlProcessingService.processTreeXml(file);
                break;
            case "filters":
                xmlProcessingService.processFiltersXml(file);
                break;
            case "complects":
                xmlProcessingService.processComplectsXml(file);
                break;
            default:
                throw new IllegalArgumentException("Unknown file type: " + fileType);
        }
    }

    private HttpEntity<String> createAuthRequest() {
        HttpHeaders headers = new HttpHeaders();
        String auth = username + ":" + password;
        String encodedAuth = Base64.getEncoder().encodeToString(auth.getBytes());
        headers.set("Authorization", "Basic " + encodedAuth);
        return new HttpEntity<>(headers);
    }
}

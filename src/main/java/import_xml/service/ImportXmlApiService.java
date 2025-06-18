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

    @Value("${import-xml.api.base-url}")
    private String baseUrl;

    @Value("${import-xml.api.username}")
    private String username;

    @Value("${import-xml.api.password}")
    private String password;

    @Value("${import-xml.import.directory}")
    private String importDirectory;

    private long rateLimitDelay;

    @PostConstruct
    public void init() {
        try {
            Path tempDir = Paths.get(properties.getXmlDownload().getTempDir());
            if (!Files.exists(tempDir)) {
                Files.createDirectories(tempDir);
            }
            this.rateLimitDelay = properties.getImportConfig().getRateLimit().getDelay();
        } catch (Exception e) {
            log.error("Ошибка инициализации сервиса импорта", e);
            throw new RuntimeException("Не удалось инициализировать сервис импорта", e);
        }
    }

    @Scheduled(cron = "${import-xml.sync.cron}")
    public void syncData() {
        log.info("Starting data sync from import-xml API at {}", LocalDateTime.now());
        try {
            // Скачиваем и обрабатываем файлы
            downloadAndProcessFile(properties.getImportConfig().getFiles().getProduct(), "products");
            downloadAndProcessFile(properties.getImportConfig().getFiles().getStock(), "stock");
            downloadAndProcessFile(properties.getImportConfig().getFiles().getTree(), "tree");
            downloadAndProcessFile(properties.getImportConfig().getFiles().getFilters(), "filters");
            downloadAndProcessFile(properties.getImportConfig().getFiles().getComplects(), "complects");
            downloadAndProcessFile(properties.getImportConfig().getFiles().getCatalogue(), "catalogue");
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
            downloadAndProcessFile("product.xml", "products");
            downloadAndProcessFile("stock.xml", "stock");
            downloadAndProcessFile("tree.xml", "tree");
            downloadAndProcessFile("filters.xml", "filters");
            downloadAndProcessFile("complects.xml", "complects");
            downloadAndProcessFile("catalogue.xml", "catalogue");

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

            // Задержка между запросами из конфига
            TimeUnit.MILLISECONDS.sleep(rateLimitDelay);
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
            case "catalogue":
                xmlProcessingService.processCatalogueXml(file);
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

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

    private long rateLimitDelay;

    @PostConstruct
    public void init() {
        try {
            Path tempDir = Paths.get(properties.getXmlDownload().getTempDir());
            if (!Files.exists(tempDir)) {
                Files.createDirectories(tempDir);
            }
            this.rateLimitDelay = properties.getImportSection().getRateLimit().getDelay();
        } catch (Exception e) {
            log.error("Ошибка инициализации сервиса импорта", e);
            throw new RuntimeException("Не удалось инициализировать сервис импорта", e);
        }
    }

    @Scheduled(cron = "${import-xml.sync.cron}")
    public void syncData() {
        log.info("Starting data sync from import-xml API at {}", LocalDateTime.now());
        log.info("product: {}, stock: {}, tree: {}, filters: {}, complects: {}, catalogue: {}",
            properties.getImportSection().getFiles().getProduct(),
            properties.getImportSection().getFiles().getStock(),
            properties.getImportSection().getFiles().getTree(),
            properties.getImportSection().getFiles().getFilters(),
            properties.getImportSection().getFiles().getComplects(),
            properties.getImportSection().getFiles().getCatalogue());
        try {
            // Скачиваем и обрабатываем файлы
            downloadAndProcessFile(properties.getImportSection().getFiles().getProduct(), "products");
            downloadAndProcessFile(properties.getImportSection().getFiles().getStock(), "stock");
            downloadAndProcessFile(properties.getImportSection().getFiles().getTree(), "tree");
            downloadAndProcessFile(properties.getImportSection().getFiles().getFilters(), "filters");
            downloadAndProcessFile(properties.getImportSection().getFiles().getComplects(), "complects");
            downloadAndProcessFile(properties.getImportSection().getFiles().getCatalogue(), "catalogue");
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
            Path importPath = Paths.get(properties.getImportSection().getDirectory());
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
        String url = String.format("%s/export/v2/catalogue/%s", properties.getApi().getBaseUrl(), fileName);
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        String localFileName = String.format("%s_%s.xml", fileType, timestamp);
        File importDir = new File(properties.getImportSection().getDirectory());
        if (!importDir.exists()) {
            importDir.mkdirs();
        }
        File localFile = new File(importDir, localFileName);

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

            Files.deleteIfExists(localFile.toPath());
            log.info("{} file processed and deleted", fileName);

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
        String auth = properties.getApi().getUsername() + ":" + properties.getApi().getPassword();
        String encodedAuth = Base64.getEncoder().encodeToString(auth.getBytes());
        headers.set("Authorization", "Basic " + encodedAuth);
        return new HttpEntity<>(headers);
    }
}

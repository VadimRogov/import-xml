package import_xml.controller;

import import_xml.service.XmlProcessingService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/v1/stock")
@RequiredArgsConstructor
public class StockController {
    private final XmlProcessingService xmlProcessingService;

    @PostMapping("/upload-xml")
    public ResponseEntity<String> uploadStockXml(@RequestParam("file") MultipartFile file) {
        try {
            java.io.File tempFile = java.io.File.createTempFile("stock", ".xml");
            file.transferTo(tempFile);
            xmlProcessingService.processStockXml(tempFile);
            tempFile.delete();
            return ResponseEntity.ok("Stock XML uploaded and processed successfully");
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error processing Stock XML: " + e.getMessage());
        }
    }
} 
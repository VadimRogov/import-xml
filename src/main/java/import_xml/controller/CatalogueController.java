package import_xml.controller;

import import_xml.service.XmlProcessingService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/v1/catalogue")
@RequiredArgsConstructor
public class CatalogueController {
    private final XmlProcessingService xmlProcessingService;

    @PostMapping("/upload-xml")
    public ResponseEntity<String> uploadCatalogueXml(@RequestParam("file") MultipartFile file) {
        try {
            java.io.File tempFile = java.io.File.createTempFile("catalogue", ".xml");
            file.transferTo(tempFile);
            xmlProcessingService.processCatalogueXml(tempFile);
            tempFile.delete();
            return ResponseEntity.ok("Catalogue XML uploaded and processed successfully");
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error processing Catalogue XML: " + e.getMessage());
        }
    }
} 
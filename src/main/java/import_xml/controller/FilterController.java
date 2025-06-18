package import_xml.controller;

import import_xml.service.XmlProcessingService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/v1/filters")
@RequiredArgsConstructor
public class FilterController {
    private final XmlProcessingService xmlProcessingService;

    @PostMapping("/upload-xml")
    public ResponseEntity<String> uploadFiltersXml(@RequestParam("file") MultipartFile file) {
        try {
            java.io.File tempFile = java.io.File.createTempFile("filters", ".xml");
            file.transferTo(tempFile);
            xmlProcessingService.processFiltersXml(tempFile);
            tempFile.delete();
            return ResponseEntity.ok("Filters XML uploaded and processed successfully");
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error processing Filters XML: " + e.getMessage());
        }
    }
} 
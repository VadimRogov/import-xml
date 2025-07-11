package import_xml.controller;

import import_xml.service.XmlProcessingService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/v1/complects")
@RequiredArgsConstructor
public class ComplectController {
    private final XmlProcessingService xmlProcessingService;

    @PostMapping("/upload-xml")
    public ResponseEntity<String> uploadComplectsXml(@RequestParam("file") MultipartFile file) {
        try {
            java.io.File tempFile = java.io.File.createTempFile("complects", ".xml");
            file.transferTo(tempFile);
            xmlProcessingService.processComplectsXml(tempFile);
            tempFile.delete();
            return ResponseEntity.ok("Complects XML uploaded and processed successfully");
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error processing Complects XML: " + e.getMessage());
        }
    }
} 
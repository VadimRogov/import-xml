package import_xml.controller;

import import_xml.model.Category;
import import_xml.service.CategoryService;
import import_xml.service.XmlProcessingService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/v1/categories")
@RequiredArgsConstructor
public class CategoryController {
    private final CategoryService categoryService;
    private final XmlProcessingService xmlProcessingService;

    @GetMapping
    public ResponseEntity<Page<Category>> getAllCategories(
            @RequestParam(required = false) String parentId,
            @RequestParam(required = false) Boolean active,
            Pageable pageable) {
        return ResponseEntity.ok(categoryService.findCategories(parentId, active, pageable));
    }

    @GetMapping("/{categoryId}")
    public ResponseEntity<Category> getCategory(@PathVariable String categoryId) {
        return categoryService.findByCategoryId(categoryId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/tree")
    public ResponseEntity<List<Category>> getCategoryTree(
            @RequestParam(required = false) Boolean active) {
        return ResponseEntity.ok(categoryService.getCategoryTree(active != null ? active : true));
    }

    @GetMapping("/{categoryId}/products")
    public ResponseEntity<Page<Category>> getCategoryWithProducts(
            @PathVariable String categoryId,
            Pageable pageable) {
        return ResponseEntity.ok(categoryService.findCategoryWithProducts(categoryId, pageable));
    }

    @PostMapping("/upload-xml")
    public ResponseEntity<String> uploadCategoryXml(@RequestParam("file") MultipartFile file, @RequestParam("type") String type) {
        try {
            java.io.File tempFile = java.io.File.createTempFile("category", ".xml");
            file.transferTo(tempFile);
            if ("tree".equalsIgnoreCase(type)) {
                xmlProcessingService.processTreeXml(tempFile);
            } else if ("catalogue".equalsIgnoreCase(type)) {
                xmlProcessingService.processCatalogueXml(tempFile);
            } else {
                return ResponseEntity.badRequest().body("Unknown type: " + type);
            }
            tempFile.delete();
            return ResponseEntity.ok("Category XML uploaded and processed successfully");
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error processing Category XML: " + e.getMessage());
        }
    }
}

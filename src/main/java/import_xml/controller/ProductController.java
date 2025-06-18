package import_xml.controller;

import import_xml.model.Product;
import import_xml.service.ProductService;
import import_xml.service.XmlProcessingService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/v1/products")
@RequiredArgsConstructor
public class ProductController {
    private final ProductService productService;
    private final XmlProcessingService xmlProcessingService;

    @GetMapping
    public ResponseEntity<Page<Product>> getAllProducts(
            @RequestParam(required = false) String categoryId,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) Boolean active,
            Pageable pageable) {
        return ResponseEntity.ok(productService.findProducts(categoryId, search, active, pageable));
    }

    @GetMapping("/{productId}")
    public ResponseEntity<Product> getProduct(@PathVariable String productId) {
        return productService.findByProductId(productId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/category/{categoryId}")
    public ResponseEntity<Page<Product>> getProductsByCategory(
            @PathVariable String categoryId,
            Pageable pageable) {
        return ResponseEntity.ok(productService.findByCategoryId(categoryId, pageable));
    }

    @GetMapping("/search")
    public ResponseEntity<Page<Product>> searchProducts(
            @RequestParam String query,
            Pageable pageable) {
        return ResponseEntity.ok(productService.searchProducts(query, pageable));
    }

    @PostMapping("/upload-xml")
    public ResponseEntity<String> uploadProductXml(@RequestParam("file") MultipartFile file) {
        try {
            java.io.File tempFile = java.io.File.createTempFile("product", ".xml");
            file.transferTo(tempFile);
            xmlProcessingService.processProductsXml(tempFile);
            tempFile.delete();
            return ResponseEntity.ok("Product XML uploaded and processed successfully");
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error processing Product XML: " + e.getMessage());
        }
    }
}
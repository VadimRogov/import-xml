package import_xml.controller;

import import_xml.service.SyncService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class SyncController {
    private final SyncService syncService;

    @PostMapping("/sync")
    public ResponseEntity<String> sync() {
        try {
            syncService.syncData();
            return ResponseEntity.ok("Синхронизация с OpenCart запущена");
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Ошибка синхронизации: " + e.getMessage());
        }
    }
} 
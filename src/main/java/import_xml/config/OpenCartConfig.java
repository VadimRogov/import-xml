package import_xml.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Primary;

@Data
@Primary
@ConfigurationProperties(prefix = "opencart")
public class OpenCartConfig {
    private String baseUrl;
    private String apiKey;
    private String username;
    private String password;
    private Sync sync = new Sync();

    @Data
    public static class Sync {
        private int batchSize = 100;
        private int retryAttempts = 3;
        private int retryDelay = 5000;
        private String cron = "0 0 */4 * * *"; // Каждые 4 часа
    }
}

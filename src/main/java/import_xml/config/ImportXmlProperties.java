package import_xml.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "import-xml")
public class ImportXmlProperties {
    private Api api = new Api();
    private XmlDownload xmlDownload = new XmlDownload();
    private Sync sync = new Sync();
    private Import importConfig = new Import();

    @Data
    public static class Api {
        private String baseUrl;
        private String username;
        private String password;
        private String siteUrl;
        private String ip;
    }

    @Data
    public static class XmlDownload {
        private String tempDir = "./temp/xml";
        private int rateLimit = 5;
        private int retryAttempts = 3;
        private int retryDelay = 5;
    }

    @Data
    public static class Sync {
        private String cron = "0 0 */4 * * *"; // Каждые 4 часа
    }

    @Data
    public static class Import {
        private String directory;
        private RateLimit rateLimit = new RateLimit();
        private Files files = new Files();
        private String catalogue;

        @Data
        public static class RateLimit {
            private int delay = 1000;
        }

        @Data
        public static class Files {
            private String product;
            private String stock;
            private String tree;
            private String filters;
            private String complects;
            private String catalogue;
        }

        public String getCatalogue() { return catalogue; }
        public void setCatalogue(String catalogue) { this.catalogue = catalogue; }
    }
}

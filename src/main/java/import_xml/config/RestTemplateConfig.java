package import_xml.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;
import java.util.Base64;
import java.util.Collections;
import import_xml.config.ImportXmlProperties;

@Configuration
public class RestTemplateConfig {

    private final ImportXmlProperties properties;

    public RestTemplateConfig(ImportXmlProperties properties) {
        this.properties = properties;
    }

    @Bean
    public RestTemplate restTemplate() {
        int connectTimeout = properties.getHttp().getClient().getConnectTimeout();
        int socketTimeout = properties.getHttp().getClient().getSocketTimeout();
        String username = properties.getApi().getUsername();
        String password = properties.getApi().getPassword();

        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(connectTimeout);
        factory.setReadTimeout(socketTimeout);

        RestTemplate restTemplate = new RestTemplate(factory);

        // Add Basic Auth interceptor
        ClientHttpRequestInterceptor authInterceptor = (request, body, execution) -> {
            String auth = username + ":" + password;
            String encodedAuth = Base64.getEncoder().encodeToString(auth.getBytes());
            request.getHeaders().set("Authorization", "Basic " + encodedAuth);
            return execution.execute(request, body);
        };

        restTemplate.setInterceptors(Collections.singletonList(authInterceptor));
        return restTemplate;
    }
}

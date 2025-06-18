package import_xml.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;
import java.util.Base64;
import java.util.Collections;

@Configuration
public class RestTemplateConfig {

    @Value("${http.client.connect-timeout}")
    private int connectTimeout;

    @Value("${http.client.socket-timeout}")
    private int socketTimeout;

    @Value("${project111.api.username}")
    private String username;

    @Value("${project111.api.password}")
    private String password;

    @Bean
    public RestTemplate restTemplate() {
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

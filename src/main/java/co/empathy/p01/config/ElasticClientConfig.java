package co.empathy.p01.config;

import org.apache.http.HttpHost;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;

@Configuration
public class ElasticClientConfig {
    @Value("${elasticsearch.host}")
    private String host;

    @Value("${elasticsearch.port}")
    private int port;

    @Bean
    @Scope(value = ConfigurableBeanFactory.SCOPE_SINGLETON)
    public RestHighLevelClient restClient() {
        return new RestHighLevelClient(
            RestClient.builder(new HttpHost(host, port, "http")));
    }
}

package co.empathy.p01.infra;

import org.apache.http.HttpHost;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestClientBuilder;
import org.elasticsearch.client.RestHighLevelClient;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;

import co.empathy.p01.config.ElasticConfiguration;

@Configuration
public class ElasticClientConfig {
    public final static String DEFAULT_CLUSTER_NAME = "docker-cluster";

    private final ElasticConfiguration config;

    public ElasticClientConfig(ElasticConfiguration config) {
        this.config = config;
    }

    private RestClientBuilder clientBuilder() {
        return RestClient.builder(new HttpHost(config.host(), config.port(), "http"));
    }
    
    @Bean
    @Scope(value = ConfigurableBeanFactory.SCOPE_SINGLETON)
    public RestHighLevelClient restClient() {
        return new RestHighLevelClient(clientBuilder());
    }
}

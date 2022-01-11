package co.empathy.p01.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.ConstructorBinding;

@ConstructorBinding
@ConfigurationProperties(prefix = "elasticsearch")
public record ElasticConfiguration(String host, int port, boolean waits, String indexName) {
}

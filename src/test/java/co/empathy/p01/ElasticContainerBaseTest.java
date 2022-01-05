package co.empathy.p01;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.ActiveProfiles;
import org.testcontainers.elasticsearch.ElasticsearchContainer;
import org.testcontainers.junit.jupiter.Container;

@ActiveProfiles("test")
public abstract class ElasticContainerBaseTest {
    @Container
    protected final static ElasticsearchContainer CONTAINER = new TestElasticsearchContainer();

    @BeforeAll
    static void setUp() {
        CONTAINER.start();
    }

    @AfterAll
    static void destroy() {
        CONTAINER.stop();
    }

    @Test
	void contextLoads() {
	}
}

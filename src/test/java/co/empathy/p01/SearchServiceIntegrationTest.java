package co.empathy.p01;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.apache.http.HttpHost;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.testcontainers.elasticsearch.ElasticsearchContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import co.empathy.p01.app.SearchService;
import co.empathy.p01.app.SearchServiceImpl;

@SpringBootTest
@Testcontainers
@AutoConfigureMockMvc
class SearchServiceIntegrationTest {

	@Container
	private final static ElasticsearchContainer CONTAINER = new ElasticsearchContainer(TestValues.ELASTIC_IMAGE);

	private static SearchService service;

	@BeforeAll
	public static void init() {
		System.out.println("BeforeAll init() method called");
		var host = CONTAINER.getHost();
		var port = CONTAINER.getFirstMappedPort();
		service = new SearchServiceImpl(new RestHighLevelClient(
				RestClient.builder(new HttpHost(host, port, "http"))));
	}

	@Test
	void contextLoads() {
	}

	@Test
	void searchQuery() throws Exception {
		var testParam = "TestString";
		var result = service.search(testParam);
		assertTrue(result.equals(testParam));
	}

	@Test
	void searchEmptyQuery() throws Exception {
		var exception = assertThrows(IllegalArgumentException.class,
				() -> service.search(""));

		var expected = "The query can't be empty";
		var msg = exception.getMessage();

		assertTrue(msg.contains(expected));
	}

	@Test
	void clusterName() throws Exception {
		var expected = "docker-cluster";
		var cName = service.getClusterName();
		assertTrue(cName.equals(expected));
	}
}

package co.empathy.p01;


import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.testcontainers.elasticsearch.ElasticsearchContainer;
import org.testcontainers.junit.jupiter.Container;

import co.empathy.p01.infra.ElasticClientConfig;

@SpringBootTest
@AutoConfigureMockMvc
class SearchControllerIntegrationTest {

	@BeforeAll
	static void setUp() {
		CONTAINER.start();
	}
	
	@AfterAll
	static void destroy() {
		CONTAINER.stop();
	}

	@Container
	private final static ElasticsearchContainer CONTAINER = new TestElasticsearchContainer();

	@Autowired
	private MockMvc mvc;

	@Test
	void contextLoads() {
	}
  
	@Test
	void searchQuery() throws Exception {
		mvc.perform(MockMvcRequestBuilders.get("/search").param("query", "test"))
				.andExpect(MockMvcResultMatchers.status().isOk())
				.andExpect(MockMvcResultMatchers.content().contentType("application/json"))
				.andExpect(MockMvcResultMatchers.jsonPath("$.query").value("test"))
				.andExpect(MockMvcResultMatchers.jsonPath("$.clusterName").value(ElasticClientConfig.DEFAULT_CLUSTER_NAME));
	}

	@Test
	void searchEmptyQuery() throws Exception {
		mvc.perform(MockMvcRequestBuilders.get("/search").param("query", ""))
				.andExpect(MockMvcResultMatchers.status().isBadRequest());
	}

	@Test
	void searchNoQuery() throws Exception {
		mvc.perform(MockMvcRequestBuilders.get("/search"))
				.andExpect(MockMvcResultMatchers.status().isBadRequest());
	}
}

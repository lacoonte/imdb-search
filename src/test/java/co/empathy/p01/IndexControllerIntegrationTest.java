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


@SpringBootTest
@AutoConfigureMockMvc
public class IndexControllerIntegrationTest {
    
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
	void index() throws Exception {
        var path = "/Users/alvaro/dev/academy/p01/src/main/resources/data.tsv";
        mvc.perform(MockMvcRequestBuilders.post("/index").param("path", path))
        .andExpect(MockMvcResultMatchers.status().isOk());
	}
}

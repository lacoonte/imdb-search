package co.empathy.p01;

import java.io.File;

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
		ClassLoader classLoader = getClass().getClassLoader();
		File file = new File(classLoader.getResource("testsample.tsv").getFile());
		String absolutePath = file.getAbsolutePath();
		mvc.perform(MockMvcRequestBuilders.post("/index").param("path", absolutePath))
				.andExpect(MockMvcResultMatchers.status().isOk());
	}
}

package co.empathy.p01;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.File;
import java.io.IOException;

import org.apache.http.util.EntityUtils;
import org.elasticsearch.client.Request;
import org.elasticsearch.client.RestClient;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;


@SpringBootTest
@AutoConfigureMockMvc
public class IndexControllerIntegrationTest extends ElasticContainerBaseTest {

	@Autowired
	private MockMvc mvc;

	@Autowired
	private RestClient cli;

	@Test
	void index() throws Exception {
		ClassLoader classLoader = getClass().getClassLoader();
		File file = new File(classLoader.getResource("testsample.tsv").getFile());
		String absolutePath = file.getAbsolutePath();
		mvc.perform(MockMvcRequestBuilders.post("/index").param("path", absolutePath))
				.andExpect(MockMvcResultMatchers.status().isOk());
		var x = countIndex();
		assertEquals(10000, x);
	}

	private long countIndex() throws IOException {
		var rq = new Request("GET", "/_cat/count/imdb");
		var response = cli.performRequest(rq);
		String responseBody = EntityUtils.toString(response.getEntity());
		var sSplsit = responseBody.split(" ");
		var f = Long.parseLong(sSplsit[2].strip());
		return f;
	}
}

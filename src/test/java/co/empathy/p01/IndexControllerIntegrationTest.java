package co.empathy.p01;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.File;
import java.io.IOException;

import org.apache.http.util.EntityUtils;
import org.elasticsearch.client.Request;
import org.elasticsearch.client.RestHighLevelClient;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import co.empathy.p01.config.ElasticConfiguration;

@SpringBootTest
@AutoConfigureMockMvc
public class IndexControllerIntegrationTest extends ElasticContainerBaseTest {

	@Autowired
	private MockMvc mvc;

	@Autowired
	private RestHighLevelClient cli;

	@Autowired
	private ElasticConfiguration config;

	@Test
	void pathNotExists() throws Exception {
		String pathWhichNotExists = "rand0mPath25";
		mvc.perform(MockMvcRequestBuilders.post("/index").param("path", pathWhichNotExists))
				.andExpect(MockMvcResultMatchers.status().isBadRequest());
	}

	@Test
	void index() throws Exception {
		performCreateTestIndex()
				.andExpect(MockMvcResultMatchers.status().isOk());
		var n = countIndex();
		assertEquals(10000, n);
		deleteTestIndex(); //Restore state before the test because we want them to be idempotent.
	}

	@Test
	void indexAlreadyExists() throws Exception {
		performCreateTestIndex()
				.andExpect(MockMvcResultMatchers.status().isOk());
		performCreateTestIndex()
				.andExpect(MockMvcResultMatchers.status().isBadRequest());
		deleteTestIndex();
	}

	private long countIndex() throws IOException {
		var rq = new Request("GET", "/_cat/count/" + config.indexName());
		var response = cli.getLowLevelClient().performRequest(rq);
		String responseBody = EntityUtils.toString(response.getEntity());
		var sSplsit = responseBody.split(" ");
		var f = Long.parseLong(sSplsit[2].strip());
		return f;
	}

	private void deleteTestIndex() throws IOException {
		var rq = new Request("DELETE", "/" + config.indexName());
		cli.getLowLevelClient().performRequest(rq);
	}

	private ResultActions performCreateTestIndex() throws Exception {
		ClassLoader classLoader = getClass().getClassLoader();
		File file = new File(classLoader.getResource("testsample.tsv").getFile());
		String absolutePath = file.getAbsolutePath();
		return mvc.perform(MockMvcRequestBuilders.post("/index").param("path", absolutePath));
	}
}

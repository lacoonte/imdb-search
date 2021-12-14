package co.empathy.p01;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

@SpringBootTest
@AutoConfigureMockMvc
class P01ApplicationTests {

	@Autowired
	private MockMvc mvc;

	@Test
	void contextLoads() {
	}

	@Test
	void searchQuery() throws Exception {
		//Text query
		mvc.perform(MockMvcRequestBuilders.get("/search").param("query", "test"))
				.andExpect(MockMvcResultMatchers.status().isOk())
				.andExpect(MockMvcResultMatchers.content().contentType("application/json"))
				.andExpect(MockMvcResultMatchers.jsonPath("$.query").value("test"))
				.andExpect(MockMvcResultMatchers.jsonPath("$.clusterName").value("docker-cluster"));

		//Empty query
		mvc.perform(MockMvcRequestBuilders.get("/search").param("query", ""))
				.andExpect(MockMvcResultMatchers.status().isOk())
				.andExpect(MockMvcResultMatchers.content().contentType("application/json"))
				.andExpect(MockMvcResultMatchers.jsonPath("$.query").value(""))
				.andExpect(MockMvcResultMatchers.jsonPath("$.clusterName").value("docker-cluster"));

		//No query
		mvc.perform(MockMvcRequestBuilders.get("/search"))
				.andExpect(MockMvcResultMatchers.status().isBadRequest());
	}
}

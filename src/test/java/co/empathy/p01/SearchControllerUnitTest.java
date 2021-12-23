package co.empathy.p01;

import static org.mockito.ArgumentMatchers.anyString;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import co.empathy.p01.app.ElasticUnavailableException;
import co.empathy.p01.app.SearchService;
import co.empathy.p01.infra.ElasticClientConfig;

@SpringBootTest
@AutoConfigureMockMvc
class SearchControllerUnitTest {
	@MockBean
	SearchService searchService;

	@Autowired
	private MockMvc mvc;

	@Test
	void contextLoads() {
	}

	@Test
	void searchQuery() throws Exception {
		Mockito.when(searchService.search(anyString())).thenAnswer(invocation -> invocation.getArgument(0));
		Mockito.when(searchService.getClusterName()).thenAnswer(invocation -> ElasticClientConfig.DEFAULT_CLUSTER_NAME);

		mvc.perform(MockMvcRequestBuilders.get("/search").param("query", "test"))
				.andExpect(MockMvcResultMatchers.status().isOk())
				.andExpect(MockMvcResultMatchers.content().contentType("application/json"))
				.andExpect(MockMvcResultMatchers.jsonPath("$.query").value("test"))
				.andExpect(MockMvcResultMatchers.jsonPath("$.clusterName").value(ElasticClientConfig.DEFAULT_CLUSTER_NAME));
	}

	@Test
	void searchEmptyQuery() throws Exception {
		Mockito.when(searchService.search(anyString())).thenThrow(new IllegalArgumentException());

		mvc.perform(MockMvcRequestBuilders.get("/search").param("query", ""))
				.andExpect(MockMvcResultMatchers.status().isBadRequest());
	}

	@Test
	void searchNoQuery() throws Exception {
		mvc.perform(MockMvcRequestBuilders.get("/search"))
				.andExpect(MockMvcResultMatchers.status().isBadRequest());
	}

	@Test
	void searchGetClusterNameFail() throws Exception {
		Mockito.when(searchService.getClusterName()).thenThrow(new ElasticUnavailableException());
		mvc.perform(MockMvcRequestBuilders.get("/search").param("query", "test"))
				.andExpect(MockMvcResultMatchers.status().isInternalServerError());
	}
}

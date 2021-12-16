package co.empathy.p01;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;

import co.empathy.p01.app.SearchService;

@SpringBootTest
@AutoConfigureMockMvc
class SearchServiceTest {

	@Autowired
	private SearchService service;

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

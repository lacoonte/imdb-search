package co.empathy.p01;

import static org.junit.Assert.assertThrows;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.IOException;

import org.elasticsearch.action.admin.cluster.settings.ClusterGetSettingsRequest;
import org.elasticsearch.action.admin.cluster.settings.ClusterGetSettingsResponse;
import org.elasticsearch.client.ClusterClient;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.junit.jupiter.api.BeforeAll;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import co.empathy.p01.app.ClusterNameUnavailableException;
import co.empathy.p01.app.SearchService;
import co.empathy.p01.app.SearchServiceImpl;
import co.empathy.p01.controllers.ClusterNameNotFoundAdvice;

@SpringBootTest
class SearchServiceUnitTest {

    @MockBean
    private RestHighLevelClient restClient;
    
    @Autowired
    private SearchService service;

    @Test
	void contextLoads() {
	}

    @Test
    void clusterName() throws Exception {
        var expected = "docker-cluster";
        var settingsResponse = mock(ClusterGetSettingsResponse.class);
        var clusterClient = mock(ClusterClient.class);
        when(restClient.cluster()).thenReturn(clusterClient);
        when(settingsResponse.getSetting("cluster.name")).thenReturn("docker-cluster");
        when(clusterClient.getSettings(any(ClusterGetSettingsRequest.class), eq(RequestOptions.DEFAULT)))
                .thenReturn(settingsResponse);
        var result = service.getClusterName();
        assertEquals(expected,result);
    }

    @Test
    void clusterNameNotAvailable() throws Exception {
        var clusterClient = mock(ClusterClient.class);
        when(restClient.cluster()).thenReturn(clusterClient);
        when(clusterClient.getSettings(any(ClusterGetSettingsRequest.class), eq(RequestOptions.DEFAULT)))
                .thenThrow(new IOException());
        assertThrows(ClusterNameUnavailableException.class, service::getClusterName);
    }

    @Test
    void searchQuery() throws Exception {
        var query = "query";
        assertEquals(query, service.search(query));
        assertThrows(ClusterNameUnavailableException.class, service::getClusterName);
    }

    @Test
    void searchEmptyQuery() throws Exception {
        assertThrows(IllegalArgumentException.class, () -> service.search(""));
    }
}
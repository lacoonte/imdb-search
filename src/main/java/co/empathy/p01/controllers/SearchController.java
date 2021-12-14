package co.empathy.p01.controllers;

import java.io.IOException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import org.apache.http.HttpHost;
import org.elasticsearch.action.ActionListener;
import org.elasticsearch.action.admin.cluster.settings.ClusterGetSettingsRequest;
import org.elasticsearch.action.admin.cluster.settings.ClusterGetSettingsResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import co.empathy.p01.responses.SearchDtoResponse;

@RestController
public class SearchController {

    @Autowired
    private RestHighLevelClient cli;

    @GetMapping("/search")
    public SearchDtoResponse main(@RequestParam String query)
            throws InterruptedException, ExecutionException {
        var cluster = cli.cluster();
        var request = new ClusterGetSettingsRequest();
        request.includeDefaults(true);
        var cf = new CompletableFuture<ClusterGetSettingsResponse>();
        var cFName = cf.thenApply((response) -> response.getSetting("cluster.name"));
        cluster.getSettingsAsync(request, RequestOptions.DEFAULT, ActionListener.wrap(cf::complete,
                (ex) -> cf.completeExceptionally(new ClusterNameUnavailableException(ex))));
        var clusterName = cFName.get();
        var re = new SearchDtoResponse(query, clusterName);
        return re;
    }
}

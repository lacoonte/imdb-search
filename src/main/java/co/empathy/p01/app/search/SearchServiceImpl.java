package co.empathy.p01.app.search;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.stream.StreamSupport;


import org.elasticsearch.action.admin.cluster.settings.ClusterGetSettingsRequest;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import co.empathy.p01.app.ElasticUnavailableException;
import co.empathy.p01.model.Title;

@Service
public class SearchServiceImpl implements SearchService {

    private final RestHighLevelClient cli;

    @Autowired
    public SearchServiceImpl(RestHighLevelClient cli, RestClient lowCli) {
        this.cli = cli;
    }

    @Override
    public SearchServiceResult search(String query) throws ElasticUnavailableException, EmptyQueryException {
        if (query.isEmpty())
            throw new EmptyQueryException();

        var rq = new SearchRequest("imdb");

        var rqBuilder = new SearchSourceBuilder();
        rqBuilder.query(QueryBuilders.matchQuery("primaryTitle", query));
        rq.source(rqBuilder);
        
        try {
            var response = cli.search(rq, RequestOptions.DEFAULT);
            var hits = response.getHits();
            var titles = StreamSupport.stream(hits.spliterator(), true)
                    .map(hit -> mapTitle(hit.getId(), hit.getSourceAsMap())).toList();
            var result = new SearchServiceResult(hits.getTotalHits().value, titles);
            return result;
        } catch (IOException e) {
            throw new ElasticUnavailableException(e);
        }
    }

    @SuppressWarnings("unchecked")
    private Title mapTitle(String id, Map<String, Object> map) {
        return new Title(id, (String) map.get("type"), (String) map.get("primaryTitle"),
                (String) map.get("originalTitle"), (Boolean) map.get("isAdult"), (Integer) map.get("startYear"),
                (Integer) map.get("endYear"), (Integer) map.get("runtimeMinutes"), (List<String>) map.get("genres"));
    }

    @Override
    public String getClusterName() throws ElasticUnavailableException {
        var cluster = cli.cluster();
        var request = new ClusterGetSettingsRequest();
        request.includeDefaults(true);
        try {
            var settings = cluster.getSettings(request, RequestOptions.DEFAULT);
            var cName = settings.getSetting("cluster.name");
            return cName;
        } catch (Exception e) {
            throw new ElasticUnavailableException(e);
        }
    }

}

package co.empathy.p01.app.search;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.stream.StreamSupport;

import org.elasticsearch.action.admin.cluster.settings.ClusterGetSettingsRequest;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import co.empathy.p01.app.ElasticUnavailableException;
import co.empathy.p01.config.ElasticConfiguration;
import co.empathy.p01.model.Title;

@Service
public class SearchServiceImpl implements SearchService {

    private final RestHighLevelClient cli;
    private final ElasticConfiguration config;

    @Autowired
    public SearchServiceImpl(RestHighLevelClient cli, ElasticConfiguration config) {
        this.cli = cli;
        this.config = config;
    }

    @Override
    public SearchServiceResult search(String query, List<String> genres, List<String> types)
            throws ElasticUnavailableException, EmptyQueryException {
        if (query.isEmpty())
            throw new EmptyQueryException();

        var rq = new SearchRequest(config.indexName());

        var rqBuilder = new SearchSourceBuilder();
        var titleQuery = QueryBuilders.matchQuery("primaryTitle", query);
        if (genres.isEmpty() && types.isEmpty()) {
            rqBuilder.query(titleQuery);
        } else if (!genres.isEmpty() && !types.isEmpty()) {
            var boolBuilder = new BoolQueryBuilder();
            var genresBoolBuilder = new BoolQueryBuilder();
            var typesBoolBuilder = new BoolQueryBuilder();
            genresBoolBuilder.minimumShouldMatch(1);
            typesBoolBuilder.minimumShouldMatch(1);

            genres.forEach(genre -> genresBoolBuilder.should(QueryBuilders.matchQuery("genres", genre)));
            types.forEach(type -> typesBoolBuilder.should(QueryBuilders.matchQuery("type", type)));

            boolBuilder.must(titleQuery);
            boolBuilder.must(genresBoolBuilder);
            boolBuilder.must(typesBoolBuilder);
            rqBuilder.query(boolBuilder);
        } else {
            var boolBuilder = new BoolQueryBuilder();
            boolBuilder.minimumShouldMatch(1);
            genres.forEach(genre -> boolBuilder.should(QueryBuilders.matchQuery("genres", genre)));
            types.forEach(type -> boolBuilder.should(QueryBuilders.matchQuery("type", type)));
            boolBuilder.must(titleQuery);
            rqBuilder.query(boolBuilder);
        }
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

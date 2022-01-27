package co.empathy.p01.app.search;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import org.elasticsearch.action.admin.cluster.settings.ClusterGetSettingsRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.search.aggregations.bucket.MultiBucketsAggregation;
import org.elasticsearch.search.aggregations.bucket.MultiBucketsAggregation.Bucket;
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
    public SearchServiceResult search(String query, List<String> genres, List<String> types, List<YearFilter> years)
            throws ElasticUnavailableException, EmptyQueryException {
        if (query.isEmpty())
            throw new EmptyQueryException();

        var searchRqBuilder = new SearchTitleRequestBuilder(query, config.indexName());
        if (!genres.isEmpty())
            searchRqBuilder.addGenresFilter(genres);
        if (!types.isEmpty())
            searchRqBuilder.addTypesFilter(types);
        if (!years.isEmpty())
            searchRqBuilder.addYearsFilter(years);
        var rq = searchRqBuilder.buildSearchRequest();

        try {
            var response = cli.search(rq, RequestOptions.DEFAULT);
            var hits = response.getHits();
            var titles = StreamSupport.stream(hits.spliterator(), true)
                    .map(hit -> mapTitle(hit.getId(), hit.getSourceAsMap())).toList();
            var genresAgg = getAggregation("genres", response);
            var typeAgg = getAggregation("type", response);
            var rangesAgg = getRangeAggregation(response);
            var result = new SearchServiceResult(hits.getTotalHits().value, titles,
                    new Aggregations(genresAgg, typeAgg, rangesAgg));
            return result;
        } catch (IOException e) {
            throw new ElasticUnavailableException(e);
        }
    }

    private Map<String, Long> getAggregation(String key, SearchResponse response) {
        MultiBucketsAggregation terms = response.getAggregations().get(key);
        return terms.getBuckets().stream().collect(Collectors.toMap(Bucket::getKeyAsString, Bucket::getDocCount));
    }

    private Map<String, Long> getRangeAggregation(SearchResponse response) {
        MultiBucketsAggregation terms = response.getAggregations().get("year");
        return terms.getBuckets().stream()
                .collect(Collectors.toMap(
                        bucket -> Long.toString((long) ((double) bucket.getKey()))
                                + " - "
                                + Long.toString((long) ((double) bucket.getKey()) + Aggregations.YEAR_RANGE),
                        Bucket::getDocCount));
    }

    @SuppressWarnings("unchecked")
    private Title mapTitle(String id, Map<String, Object> map) {
        return new Title(id, (String) map.get("type"), (String) map.get("primaryTitle"),
                (String) map.get("originalTitle"), (Boolean) map.get("isAdult"), (Integer) map.get("startYear"),
                (Integer) map.get("endYear"), (Integer) map.get("runtimeMinutes"), (List<String>) map.get("genres"),
                Optional.ofNullable(map.get("numVotes")).map(val -> (Double) val).orElse(0D),
                Optional.ofNullable(map.get("numVotes")).map(val -> Long.valueOf((Integer) val)).orElse(0L));
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

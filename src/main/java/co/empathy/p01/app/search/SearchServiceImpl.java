package co.empathy.p01.app.search;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import org.elasticsearch.action.admin.cluster.settings.ClusterGetSettingsRequest;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.search.aggregations.bucket.MultiBucketsAggregation;
import org.elasticsearch.search.aggregations.bucket.MultiBucketsAggregation.Bucket;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.suggest.SuggestBuilder;
import org.elasticsearch.search.suggest.SuggestBuilders;
import org.elasticsearch.search.suggest.SuggestionBuilder;
import org.elasticsearch.search.suggest.phrase.PhraseSuggestion;
import org.elasticsearch.search.suggest.phrase.PhraseSuggestionBuilder;
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
    public SearchServiceResult search(String query, List<String> genres, List<String> types, List<YearFilter> years, int start, int nRows)
            throws ElasticUnavailableException, EmptyQueryException {
        if (query.isEmpty())
            throw new EmptyQueryException();

        var searchRqBuilder = new SearchTitleRequestBuilder(query, config.indexName(), start, nRows);
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
            var hitsN = hits.getTotalHits().value;
            if (hitsN > 0L) {
                var titles = StreamSupport.stream(hits.spliterator(), true)
                        .map(hit -> mapTitle(hit.getId(), hit.getSourceAsMap(), hit.getScore())).toList();
                var genresAgg = getAggregation("genres", response);
                var typeAgg = getAggregation("type", response);
                var rangesAgg = getRangeAggregation(response);
                var result = new SearchServiceResult(hitsN, titles, new Aggregations(genresAgg, typeAgg, rangesAgg));
                return result;
            } else {
                SuggestionBuilder<PhraseSuggestionBuilder> suggestRq = SuggestBuilders
                        .phraseSuggestion("primaryTitle.trigram").text(query).gramSize(3);
                var rqBuilder = new SearchSourceBuilder();
                var suggestSearchRq = new SearchRequest();
                var suggestBuilder = new SuggestBuilder();
                suggestBuilder.addSuggestion("spellcheck", suggestRq);
                rqBuilder.suggest(suggestBuilder);
                suggestSearchRq.source(rqBuilder);
                System.out.println(suggestSearchRq.source().toString());
                var suggestResponse = cli.search(suggestSearchRq, RequestOptions.DEFAULT);
                PhraseSuggestion suggest = suggestResponse.getSuggest().getSuggestion("spellcheck");
                var suggestions = suggest.getEntries().stream()
                        .flatMap(entry -> entry.getOptions().stream())
                        .map(option -> new TitleSuggestion(option.getScore(), option.getText().string()));
                var result = new SearchServiceResult(suggestions.toList());
                return result;
            }

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
    private TitleResult mapTitle(String id, Map<String, Object> map, float score) {
        return new TitleResult(new Title(id, (String) map.get("type"), (String) map.get("primaryTitle"),
                (String) map.get("originalTitle"), (Boolean) map.get("isAdult"), (Integer) map.get("startYear"),
                (Integer) map.get("endYear"), (Integer) map.get("runtimeMinutes"), (List<String>) map.get("genres"),
                Optional.ofNullable(map.get("averageRating")).map(val -> (Double) val).orElse(0D),
                Optional.ofNullable(map.get("numVotes")).map(val -> Long.valueOf((Integer) val)).orElse(0L)), score);
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

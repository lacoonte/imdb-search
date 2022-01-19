package co.empathy.p01.app.search;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.MatchQueryBuilder;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.builder.SearchSourceBuilder;

public class SearchTitleRequestBuilder {
    private final String titleName;
    private final String indexName;

    private List<BoolQueryBuilder> queryBuilders;

    public SearchTitleRequestBuilder(String titleName, String indexName) {
        queryBuilders = new ArrayList<>();
        this.indexName = indexName;
        this.titleName = titleName;
    }

    public void addGenresFilter(List<String> genres) {
        var genresQuery = buildBoolQueryBuilder();
        genres.forEach(genre -> genresQuery.should(QueryBuilders.matchQuery("genres", genre)));
        queryBuilders.add(genresQuery);
    }

    public void addYearsFilter(List<YearFilter> filters) {
        var rangesQuery = buildBoolQueryBuilder();
        var areRanges = filters.stream().collect(Collectors.partitioningBy(x -> x.isRange()));
        areRanges.get(true).stream()
                .map(range -> QueryBuilders.rangeQuery("startYear").gte(range.start()).lte(range.end()))
                .forEach(qB -> rangesQuery.should(qB));
        areRanges.get(false).stream().map(year -> QueryBuilders.matchQuery("startYear", year.start()))
                .forEach(qB -> rangesQuery.should(qB));
        queryBuilders.add(rangesQuery);
    }

    public void addTypesFilter(List<String> types) {
        var typesQuery = buildBoolQueryBuilder();
        types.forEach(type -> typesQuery.should(QueryBuilders.matchQuery("type", type)));
        queryBuilders.add(typesQuery);
    }

    public SearchRequest buildSearchRequest() {
        var searchRequest = new SearchRequest(indexName);
        var q = buildQueryBuilder();
        var rqBuilder = new SearchSourceBuilder();
        rqBuilder.query(q);
        rqBuilder.aggregation(AggregationBuilders.terms("genres").field("genres"));
        rqBuilder.aggregation(AggregationBuilders.terms("type").field("type"));
        rqBuilder
                .aggregation(AggregationBuilders.histogram("year").field("startYear").interval(Aggregations.YEAR_RANGE));
        searchRequest.source(rqBuilder);
        return searchRequest;
    }

    private QueryBuilder buildQueryBuilder() {
        var titleQuery = new MatchQueryBuilder("primaryTitle", titleName);
        var filtersN = queryBuilders.size();
        switch (filtersN) {
            case 0:
                return titleQuery;
            case 1:
                return queryBuilders.get(0).must(titleQuery);
            default:
                var result = new BoolQueryBuilder();
                result.must(titleQuery);
                queryBuilders.forEach(qB -> result.must(qB));
                return result;
        }
    }

    private BoolQueryBuilder buildBoolQueryBuilder() {
        var result = new BoolQueryBuilder();
        result.minimumShouldMatch(1);
        return result;
    }
}

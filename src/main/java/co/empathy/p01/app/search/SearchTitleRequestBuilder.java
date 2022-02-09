package co.empathy.p01.app.search;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.common.lucene.search.function.FieldValueFactorFunction.Modifier;
import org.elasticsearch.common.lucene.search.function.FunctionScoreQuery.ScoreMode;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.functionscore.FieldValueFactorFunctionBuilder;
import org.elasticsearch.index.query.functionscore.FunctionScoreQueryBuilder;
import org.elasticsearch.index.query.functionscore.ScoreFunctionBuilders;
import org.elasticsearch.index.query.functionscore.FunctionScoreQueryBuilder.FilterFunctionBuilder;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.builder.SearchSourceBuilder;

public class SearchTitleRequestBuilder {
    private final String titleName;
    private final String indexName;
    private final int nRows;
    private final int start;
    private List<BoolQueryBuilder> queryBuilders;

    public SearchTitleRequestBuilder(String titleName, String indexName, int start, int nRows) {
        queryBuilders = new ArrayList<>();
        this.indexName = indexName;
        this.titleName = titleName;
        this.start = start;
        this.nRows = nRows;
    }

    public void addGenresFilter(List<String> genres) {
        var genresQuery = QueryBuilders.boolQuery().minimumShouldMatch(1);
        genres.forEach(genre -> genresQuery.should(QueryBuilders.termQuery("genres", genre).caseInsensitive(true)));
        queryBuilders.add(genresQuery);
    }

    public void addYearsFilter(List<YearFilter> filters) {
        var rangesQuery = QueryBuilders.boolQuery().minimumShouldMatch(1);
        var areRanges = filters.stream().collect(Collectors.partitioningBy(x -> x.isRange()));
        areRanges.get(true).stream()
                .map(range -> QueryBuilders.rangeQuery("startYear").gte(range.start()).lte(range.end()))
                .forEach(qB -> rangesQuery.should(qB));
        areRanges.get(false).stream().map(year -> QueryBuilders.termQuery("startYear", year.start()))
                .forEach(qB -> rangesQuery.should(qB));
        queryBuilders.add(rangesQuery);
    }

    public void addTypesFilter(List<String> types) {
        var typesQuery = QueryBuilders.boolQuery().minimumShouldMatch(1);
        types.forEach(type -> typesQuery.should(QueryBuilders.termQuery("type", type).caseInsensitive(true)));
        queryBuilders.add(typesQuery);
    }

    public SearchRequest buildSearchRequest() {
        var searchRequest = new SearchRequest(indexName);
        var q = buildQueryBuilder();
        var rqBuilder = new SearchSourceBuilder();
        
        rqBuilder.from(start);
        rqBuilder.size(nRows);
        
        rqBuilder.query(q);
        rqBuilder.aggregation(AggregationBuilders.terms("genres").field("genres"));
        rqBuilder.aggregation(AggregationBuilders.terms("type").field("type"));
        rqBuilder
                .aggregation(
                        AggregationBuilders.histogram("year").field("startYear").interval(Aggregations.YEAR_RANGE).missing(0).minDocCount(1L));
        searchRequest.source(rqBuilder);
        return searchRequest;
    }

    private QueryBuilder buildQueryBuilder() {
        var termQuery = QueryBuilders.termQuery("primaryTitle.raw", titleName).boost(10).caseInsensitive(true);
        var typeQuery = QueryBuilders.termQuery("type", "movie").boost(5);
        var titleQuery = QueryBuilders.matchPhraseQuery("primaryTitle", titleName);
        var result = QueryBuilders.boolQuery().should(termQuery).should(typeQuery).must(titleQuery);
        var filtersN = queryBuilders.size();
        switch (filtersN) {
            case 0:
                return buildMainQuery(result);
            case 1:
                return buildMainQuery(result.must(queryBuilders.get(0)));
            default:
                queryBuilders.forEach(qB -> result.must(qB));
                return buildMainQuery(result);
        }
    }

    private FunctionScoreQueryBuilder buildMainQuery(BoolQueryBuilder boolQueryBuilder) {
        var nVotes = new FieldValueFactorFunctionBuilder("numVotes");
        nVotes.missing(0.1).modifier(Modifier.LOG1P);
        var gauss = ScoreFunctionBuilders.gaussDecayFunction("startYear", "2022", "1");
        var result = new FunctionScoreQueryBuilder(boolQueryBuilder, new FilterFunctionBuilder[]{new FunctionScoreQueryBuilder.FilterFunctionBuilder(gauss),new FunctionScoreQueryBuilder.FilterFunctionBuilder(nVotes)});
        result.scoreMode(ScoreMode.SUM);
        return result;
    }
}

package co.empathy.p01.app.search;

import java.util.List;
import java.util.Objects;


public record SearchServiceResult(long total, List<TitleResult> items, Aggregations aggregations,
        List<TitleSuggestion> suggestions) {
    public SearchServiceResult {
        Objects.requireNonNull(items);
        Objects.requireNonNull(aggregations);
        Objects.requireNonNull(suggestions);
    }

    public SearchServiceResult(long total, List<TitleResult> items, Aggregations aggregations) {
        this(total, items, aggregations, List.of());
    }

    public SearchServiceResult(List<TitleSuggestion> suggestions) {
        this(0L, List.of(), Aggregations.EMPTY_AGGREGATIONS, suggestions);
    }
}

package co.empathy.p01.app.search;

import java.util.Map;

public record Aggregations(Map<String, Long> genres, Map<String, Long> types, Map<String, Long> years) {
    public final static long YEAR_RANGE = 10;
    public final static Aggregations EMPTY_AGGREGATIONS = new Aggregations(Map.of(), Map.of(), Map.of());
}

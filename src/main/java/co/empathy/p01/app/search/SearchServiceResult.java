package co.empathy.p01.app.search;

import java.util.List;
import java.util.Map;

import co.empathy.p01.model.Title;

public record SearchServiceResult(long total, List<Title> items
// , List<Map<String,Integer>> agregations
) {
    
}

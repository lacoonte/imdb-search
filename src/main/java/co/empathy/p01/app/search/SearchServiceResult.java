package co.empathy.p01.app.search;

import java.util.List;

import co.empathy.p01.model.Title;

public record SearchServiceResult(long total, List<Title> items
 , Aggregations aggregations
) {
    
}

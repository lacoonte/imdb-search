package co.empathy.p01.app;

import java.util.List;

import co.empathy.p01.model.Title;

public record SearchServiceResult(long total, List<Title> items) {
    
}

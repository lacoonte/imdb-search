package co.empathy.p01.controllers;

import java.io.IOException;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import co.empathy.p01.app.ElasticUnavailableException;
import co.empathy.p01.app.search.EmptyQueryException;
import co.empathy.p01.app.search.SearchService;
import co.empathy.p01.app.search.SearchServiceResult;
import co.empathy.p01.app.search.YearFilter;

@RestController
public class SearchController {

    @Autowired
    private SearchService service;

    @GetMapping("/search")
    public SearchServiceResult main(@RequestParam String query, @RequestParam(defaultValue = "") List<String> genres,
            @RequestParam(defaultValue = "") List<String> types, @RequestParam(defaultValue = "") List<YearFilter> years)
            throws ElasticUnavailableException, IOException, EmptyQueryException {
        var result = service.search(query, genres, types, years);
        return result;
    }
}

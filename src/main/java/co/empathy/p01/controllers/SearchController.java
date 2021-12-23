package co.empathy.p01.controllers;

import java.io.IOException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import co.empathy.p01.app.ElasticUnavailableException;
import co.empathy.p01.app.SearchService;
import co.empathy.p01.app.SearchServiceResult;
import co.empathy.p01.responses.SearchDtoResponse;

@RestController
public class SearchController {

    @Autowired
    private SearchService service;

    @GetMapping("/search")
    public SearchServiceResult main(@RequestParam String query) throws ElasticUnavailableException, IOException {
        if(query.isEmpty()) {
            throw new ResponseStatusException(
                HttpStatus.BAD_REQUEST, "Query can't be empty");        
        }
        var result = service.search(query);
        return result;
    }
}

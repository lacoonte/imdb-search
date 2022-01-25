package co.empathy.p01.controllers;

import java.io.IOException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import co.empathy.p01.app.index.IndexAlreadyExistsException;
import co.empathy.p01.app.index.IndexFailedException;
import co.empathy.p01.app.index.TitleIndexService;
import co.empathy.p01.app.index.FileNotExistsExcetion;

@RestController
public class IndexController {
    @Autowired
    private TitleIndexService service;

    @PostMapping("/index")
    public ResponseEntity<?> main(@RequestParam String path, @RequestParam(required = false) String ratingsPath)
            throws IndexAlreadyExistsException, IndexFailedException, FileNotExistsExcetion {
        try {
            if (ratingsPath == null)
                service.indexTitlesFromTabFile(path);
            else
                service.indexTitlesWithRatingsFromTabFiles(path, ratingsPath);
            return ResponseEntity.ok().build();
        } catch (IOException e) {
            return ResponseEntity.badRequest().body(e.toString());
        }
    }
}

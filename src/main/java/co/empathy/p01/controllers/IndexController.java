package co.empathy.p01.controllers;

import java.io.IOException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import co.empathy.p01.app.TitleIndexService;

@RestController
public class IndexController {
    @Autowired
    private TitleIndexService service;

    @PostMapping("/index")
    public ResponseEntity<?> main(@RequestParam String path) {
        try {
            service.indexTitlesFromTabFile(path);
            return ResponseEntity.ok().build();
        } catch (IOException e) {
            return ResponseEntity.badRequest().build();
        }
    }
}

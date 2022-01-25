package co.empathy.p01.controllers;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

import co.empathy.p01.app.ElasticUnavailableException;
import co.empathy.p01.app.index.IndexAlreadyExistsException;
import co.empathy.p01.app.index.IndexFailedException;
import co.empathy.p01.app.index.FileNotExistsExcetion;
import co.empathy.p01.app.search.EmptyQueryException;

@ControllerAdvice
public class ControllerAdvisor {
    @ResponseBody
    @ExceptionHandler(ElasticUnavailableException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    String clusterNameNotFoundHandler(ElasticUnavailableException ex) {
        return ex.getMessage();
    }

    @ResponseBody
    @ExceptionHandler(EmptyQueryException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    String emptyQueryHandler(EmptyQueryException ex) {
        return ex.getMessage();
    }

    @ResponseBody
    @ExceptionHandler(IndexAlreadyExistsException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    String indexAlreadyExistsHandler(IndexAlreadyExistsException ex) {
        return ex.getMessage();
    }

    @ResponseBody
    @ExceptionHandler(IndexFailedException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    String indexingProcessFailureHandler(ElasticUnavailableException ex) {
        return ex.getMessage();
    }

    @ResponseBody
    @ExceptionHandler(FileNotExistsExcetion.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    String titlesFilesNotExistsHandler(FileNotExistsExcetion ex) {
        return ex.getMessage();
    }
}

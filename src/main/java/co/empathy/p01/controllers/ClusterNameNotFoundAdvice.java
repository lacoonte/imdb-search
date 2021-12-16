package co.empathy.p01.controllers;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

import co.empathy.p01.app.ClusterNameUnavailableException;

@ControllerAdvice
public class ClusterNameNotFoundAdvice {
    @ResponseBody
    @ExceptionHandler(ClusterNameUnavailableException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    String clusterNameNotFoundHandler(ClusterNameUnavailableException ex) {
        return ex.getMessage();
    }
}

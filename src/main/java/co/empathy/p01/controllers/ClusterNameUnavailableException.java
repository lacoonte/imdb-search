package co.empathy.p01.controllers;

public class ClusterNameUnavailableException extends RuntimeException {
    ClusterNameUnavailableException(Throwable ex) {
        super("We could not get the name from the cluster", ex);
    }
}

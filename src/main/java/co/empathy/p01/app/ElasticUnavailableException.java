package co.empathy.p01.app;

public class ElasticUnavailableException extends Exception {
    public ElasticUnavailableException(Throwable ex) {
        super("We could not reach elastic nodes", ex);
    }
    public ElasticUnavailableException() {
        super("We could not reach elastic nodes");
    }
}

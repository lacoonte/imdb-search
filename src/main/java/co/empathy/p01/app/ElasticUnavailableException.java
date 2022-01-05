package co.empathy.p01.app;

public class ElasticUnavailableException extends Exception {
    private final static String MSG = "We could not reach elastic nodes";
    
    public ElasticUnavailableException(Throwable ex) {
        super(MSG, ex);
    }
    public ElasticUnavailableException() {
        super(MSG);
    }
}

package co.empathy.p01.app;

public class IndexFailedException extends Exception {
    public IndexFailedException(Throwable ex) {
        super("The title indexing has failed", ex);
    }

    public IndexFailedException() {
        super("The title indexing has failed");
    }
}

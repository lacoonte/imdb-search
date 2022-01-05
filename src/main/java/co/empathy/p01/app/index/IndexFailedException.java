package co.empathy.p01.app.index;

public class IndexFailedException extends Exception {
    private final static String MSG = "The title indexing has failed";
    public IndexFailedException(Throwable ex) {
        super(MSG, ex);
    }

    public IndexFailedException() {
        super(MSG);
    }
}

package co.empathy.p01.app.index;

public class IndexAlreadyExistsException extends Exception{
    private final static String MSG = "The index already exists so we can't reindex it.";
    public IndexAlreadyExistsException(Throwable ex) {
        super(MSG, ex);
    }

    public IndexAlreadyExistsException() {
        super(MSG);
    }
}

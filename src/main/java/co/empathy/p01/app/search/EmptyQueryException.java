package co.empathy.p01.app.search;

public class EmptyQueryException extends Exception {
    private final static String MSG = "Query can't be empty";
    public EmptyQueryException(Throwable ex) {
        super(MSG, ex);
    }

    public EmptyQueryException() {
        super(MSG);
    }
}

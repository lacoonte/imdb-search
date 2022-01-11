package co.empathy.p01.app.index;

public class TitlesFileNotExistsExcetion extends Exception {
    private final static String MSG = "The following path does not exist: ";

    public TitlesFileNotExistsExcetion(String path, Throwable ex) {
        super(MSG + path, ex);
    }

    public TitlesFileNotExistsExcetion(String path) {
        super(MSG + path);
    }
}

package co.empathy.p01.app.index;

public class FileNotExistsExcetion extends Exception {
    private final static String MSG = "The following path does not exist: ";

    public FileNotExistsExcetion(String path, Throwable ex) {
        super(MSG + path, ex);
    }

    public FileNotExistsExcetion(String path) {
        super(MSG + path);
    }
}

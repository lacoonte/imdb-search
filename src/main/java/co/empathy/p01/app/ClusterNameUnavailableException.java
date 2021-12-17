package co.empathy.p01.app;

public class ClusterNameUnavailableException extends Exception {
    public ClusterNameUnavailableException(Throwable ex) {
        super("We could not get the name from the cluster", ex);
    }
    public ClusterNameUnavailableException() {
        super("We could not get the name from the cluster");
    }
}

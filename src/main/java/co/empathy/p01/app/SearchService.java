package co.empathy.p01.app;

public interface SearchService {
    
    /**
     * Searches for the query.
     * @param query
     * @return The search result.
     */
    String search(String query);

    /**
     * Gets the name from the ElasticSerach Cluster.
     * @return Cluster's name.
     * @throws ClusterNameUnavailableException If it couldn't get the name.
     */
    String getClusterName() throws ClusterNameUnavailableException;
}

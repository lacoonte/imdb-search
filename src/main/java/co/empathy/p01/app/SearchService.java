package co.empathy.p01.app;

public interface SearchService {
    
    /**
     * Searches for the query.
     * @param query
     * @return The search result.
     * @throws ElasticUnavailableException If we could not reach any elastic node.
     */
    SearchServiceResult search(String query) throws ElasticUnavailableException;

    /**
     * Gets the name from the ElasticSerach Cluster.
     * @return Cluster's name.
     * @throws ElasticUnavailableException If we could not reach any elastic node.
     */
    String getClusterName() throws ElasticUnavailableException;
}

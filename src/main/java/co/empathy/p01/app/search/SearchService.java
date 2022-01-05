package co.empathy.p01.app.search;

import co.empathy.p01.app.ElasticUnavailableException;

public interface SearchService {
    
    /**
     * Searches for the query.
     * @param query
     * @return The search result.
     * @throws ElasticUnavailableException If we could not reach any elastic node.
     * @throws EmptyQueryException If query is empty.
     */
    SearchServiceResult search(String query) throws ElasticUnavailableException, EmptyQueryException;

    /**
     * Gets the name from the ElasticSerach Cluster.
     * @return Cluster's name.
     * @throws ElasticUnavailableException If we could not reach any elastic node.
     */
    String getClusterName() throws ElasticUnavailableException;
}

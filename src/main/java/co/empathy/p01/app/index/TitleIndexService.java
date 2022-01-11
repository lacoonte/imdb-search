package co.empathy.p01.app.index;

import java.io.IOException;

import co.empathy.p01.app.ElasticUnavailableException;

public interface TitleIndexService {
    /**
     * Indexes all titles from a Tab separated file, empty fields should be
     * represented with \N
     * @param path The .tsv file path
     * @throws InterruptedException
     * @throws ElasticUnavailableException
     */
    public void indexTitlesFromTabFile(String path) throws IOException, InterruptedException;
}
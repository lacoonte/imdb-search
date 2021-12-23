package co.empathy.p01.app;

import java.io.IOException;

public interface TitleIndexService {
    /**
     * Indexes all titles from a Tab separated file, empty fields should be
     * represented with \N
     * @param path The .tsv file path
     */
    public void indexTitlesFromTabFile(String path) throws IOException;
}
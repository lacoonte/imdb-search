package co.empathy.p01.app;

import co.empathy.p01.model.Title;

public interface TitleParser {
    /**
     * Parses a title from a given string.
     * @param s The string you want to parse.
     * @return The parsed title.
     */
    public Title parseTitle(String s);
}

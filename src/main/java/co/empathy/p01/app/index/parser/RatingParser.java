package co.empathy.p01.app.index.parser;

public interface RatingParser {
      /**
     * Parses a rating from a given string.
     * @param s The string you want to parse.
     * @return The parsed rating.
     */
    public ParsedRating parseRating(String s);
}

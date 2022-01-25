package co.empathy.p01.app.index.parser;

import org.springframework.stereotype.Service;

@Service
public class TabsRatingParser implements RatingParser {
    @Override
    public ParsedRating parseRating(String s) {
        var values = s.split("\t");
        var id = values[0];
        var average = Float.parseFloat(values[1]);
        var num = Long.parseLong(values[2]);
        return new ParsedRating(id, average, num);
    }
}

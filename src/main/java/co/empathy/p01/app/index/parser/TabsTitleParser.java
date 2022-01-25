package co.empathy.p01.app.index.parser;

import java.util.List;

import org.springframework.stereotype.Service;

import co.empathy.p01.model.Title;


@Service
public class TabsTitleParser implements TitleParser {
    private final static String NULL_TOKEN = "\\N";

    @Override
    public Title parseTitle(String s) {
        var values = s.split("\t");
        var id = values[0];
        var type = getStringOrNull(values[1]);
        var primaryTitle = getStringOrNull(values[2]);
        var originalTitle = getStringOrNull(values[3]);
        var isAdult = getBoolOrNull(values[4]);
        var startYear = getIntOrNull(values[5]);
        var endYear = getIntOrNull(values[6]);
        var runtimeMinutes = getIntOrNull(values[7]);
        var genres = getGenresList(values[8]);
        return new Title(id, type, primaryTitle, originalTitle, isAdult, startYear, endYear, runtimeMinutes, genres, null, null);
    }

    private String getStringOrNull(String s) {
        return s.equals(NULL_TOKEN) ? null : s;
    }

    private Boolean getBoolOrNull(String s) {
        return s.equals(NULL_TOKEN) ? null
                : s.equals("1") ? true : false;
    }

    private Integer getIntOrNull(String s) {
        return s.equals(NULL_TOKEN) ? null : Integer.parseInt(s);
    }

    private List<String> getGenresList(String s) {
        return s.equals(NULL_TOKEN) ? List.of() : List.of(s.split(","));
    }
}

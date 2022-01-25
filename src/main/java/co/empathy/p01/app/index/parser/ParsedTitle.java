package co.empathy.p01.app.index.parser;

import java.util.List;

public record ParsedTitle(String id, String type, String primaryTitle, String originalTitle, Boolean isAdult,
        Integer startYear, Integer endYear, Integer runtimeMinutes, List<String> genres) {

}

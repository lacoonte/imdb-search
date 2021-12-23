package co.empathy.p01.model;

import java.util.List;

public record Title(String id, String type, String primaryTitle, String originalTitle, Boolean isAdult,
                Integer startYear, Integer endYear, Integer runtimeMinutes, List<String> genres) {
}

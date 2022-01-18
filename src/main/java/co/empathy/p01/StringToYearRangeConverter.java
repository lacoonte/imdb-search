package co.empathy.p01;

import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

import co.empathy.p01.app.search.YearFilter;

@Component
public class StringToYearRangeConverter implements Converter<String, YearFilter> {

    @Override
    public YearFilter convert(String source) {
        if (!source.contains("/"))
            return new YearFilter(Long.parseLong(source), 0, false);
        var parts = source.split("/");
        var range = new YearFilter(Long.parseLong(parts[0]), Long.parseLong(parts[1]), true);
        return range;
    }

}

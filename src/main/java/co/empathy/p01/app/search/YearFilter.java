package co.empathy.p01.app.search;

//TODO: Add validations
//TODO: Search a better way of differencing between ranges and single years
public record YearFilter(long start, long end, boolean isRange) {
}

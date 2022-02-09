package co.empathy.p01;

import java.io.File;
import java.io.IOException;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import co.empathy.p01.app.index.IndexAlreadyExistsException;
import co.empathy.p01.app.index.IndexFailedException;
import co.empathy.p01.app.index.TitleIndexService;
import co.empathy.p01.app.index.FileNotExistsExcetion;

@SpringBootTest
@AutoConfigureMockMvc
class SearchControllerIntegrationTest extends ElasticContainerBaseTest {
	private final static String RETURN_MOVIE_NAME = "The Return";
	private final static String RETURN_SHORT_1916_DRAMASHORT_ID = "tt0221515";
	private final static String RETURN_TV_1960_WESTERN_ID = "tt0526635";
	private final static String RETURN_MOVIE_2003_DRAMA_ID = "tt0376968";
	private final static String RETURN_MOVIE_2021_SCIFI_ID = "tt14360612";
	private final static String RETURN_MOVIE_ROMANCE_ID = "tt14549854";

	@Autowired
	private MockMvc mvc;

	@BeforeAll
	static void setUp(@Autowired TitleIndexService service)
			throws IOException, IndexAlreadyExistsException, IndexFailedException, FileNotExistsExcetion {
		CONTAINER.start();
		ClassLoader classLoader = SearchControllerIntegrationTest.class.getClassLoader();
		File file = new File(classLoader.getResource("search_integration_test.tsv").getFile());
		String absolutePath = file.getAbsolutePath();
		service.indexTitlesFromTabFile(absolutePath);
	}

	@Test
	void exactSearch() throws Exception {
		mvc.perform(MockMvcRequestBuilders.get("/search").param("query", "Jukebox"))
				.andExpect(MockMvcResultMatchers.status().isOk())
				.andExpect(MockMvcResultMatchers.content().contentType("application/json"))
				.andExpect(MockMvcResultMatchers.jsonPath("$.items[0].title.primaryTitle").value("Jukebox"))
				.andExpect(MockMvcResultMatchers.jsonPath("$.items[0].title.id").value("tt0077440"));
	}

	@Test
	void ignoreHyphen() throws Exception {
		var resultMatcher = Matchers.containsInAnyOrder("tt0100669", "tt0145487");
		mvc.perform(MockMvcRequestBuilders.get("/search").param("query", "Spiderman"))
				.andExpect(MockMvcResultMatchers.status().isOk())
				.andExpect(MockMvcResultMatchers.content().contentType("application/json"))
				.andExpect(MockMvcResultMatchers.jsonPath("$..items[:2].title.id").value(resultMatcher));

		mvc.perform(MockMvcRequestBuilders.get("/search").param("query", "Spider-man"))
				.andExpect(MockMvcResultMatchers.status().isOk())
				.andExpect(MockMvcResultMatchers.content().contentType("application/json"))
				.andExpect(MockMvcResultMatchers.jsonPath("$..items[:2].title.id").value(resultMatcher));
	}

	@Test
	void naturalToRomanNumerals() throws Exception {
		mvc.perform(MockMvcRequestBuilders.get("/search").param("query", "5 for Vendetta"))
				.andExpect(MockMvcResultMatchers.status().isOk())
				.andExpect(MockMvcResultMatchers.content().contentType("application/json"))
				.andExpect(MockMvcResultMatchers.jsonPath("$.items[0].title.primaryTitle").value("V for Vendetta"))
				.andExpect(MockMvcResultMatchers.jsonPath("$.items[0].title.id").value("tt0434409"));
	}

	@Test
	void romanNumeralsToNatural() throws Exception {
		mvc.perform(MockMvcRequestBuilders.get("/search").param("query", "Rocky 5"))
				.andExpect(MockMvcResultMatchers.status().isOk())
				.andExpect(MockMvcResultMatchers.content().contentType("application/json"))
				.andExpect(MockMvcResultMatchers.jsonPath("$.items[0].title.primaryTitle").value("Rocky V"))
				.andExpect(MockMvcResultMatchers.jsonPath("$.items[0].title.id").value("tt0100507"));
	}

	@Test
	void ignoreAccents() throws Exception {
		mvc.perform(MockMvcRequestBuilders.get("/search").param("query", "L'enfant temoin"))
				.andExpect(MockMvcResultMatchers.status().isOk())
				.andExpect(MockMvcResultMatchers.content().contentType("application/json"))
				.andExpect(MockMvcResultMatchers.jsonPath("$.items[0].title.primaryTitle").value("L'enfant témoin"))
				.andExpect(MockMvcResultMatchers.jsonPath("$.items[0].title.id").value("tt0113495"));
	}

	@Test
	void suggest() throws Exception {
		mvc.perform(MockMvcRequestBuilders.get("/search").param("query", "the retxrn"))
				.andExpect(MockMvcResultMatchers.status().isOk())
				.andExpect(MockMvcResultMatchers.content().contentType("application/json"))
				.andExpect(MockMvcResultMatchers.jsonPath("$.suggestions[0].suggested").value("the return"));
	}

	@Test
	void suggest2Perm() throws Exception {
		mvc.perform(MockMvcRequestBuilders.get("/search").param("query", "the retxnn"))
				.andExpect(MockMvcResultMatchers.status().isOk())
				.andExpect(MockMvcResultMatchers.content().contentType("application/json"))
				.andExpect(MockMvcResultMatchers.jsonPath("$.suggestions[0].suggested").value("the return"));
	}

	@Test
	void suggest3Perm() throws Exception {
		mvc.perform(MockMvcRequestBuilders.get("/search").param("query", "the relxnn"))
				.andExpect(MockMvcResultMatchers.status().isOk())
				.andExpect(MockMvcResultMatchers.content().contentType("application/json"))
				.andExpect(MockMvcResultMatchers.jsonPath("$.suggestions.length()").value(0));
	}

	@Test
	void ignoreCaps() throws Exception {
		mvc.perform(MockMvcRequestBuilders.get("/search").param("query", "jukebox"))
				.andExpect(MockMvcResultMatchers.status().isOk())
				.andExpect(MockMvcResultMatchers.content().contentType("application/json"))
				.andExpect(MockMvcResultMatchers.jsonPath("$.items[0].title.primaryTitle").value("Jukebox"))
				.andExpect(MockMvcResultMatchers.jsonPath("$.items[0].title.id").value("tt0077440"));
	}

	@Test
	void searchNoResults() throws Exception {
		mvc.perform(MockMvcRequestBuilders.get("/search").param("query", "sldkfjkkk"))
				.andExpect(MockMvcResultMatchers.status().isOk())
				.andExpect(MockMvcResultMatchers.content().contentType("application/json"))
				.andExpect(MockMvcResultMatchers.jsonPath("$.total").value("0"));
	}

	@Test
	void searchWithNonExistantGenre() throws Exception {
		// Make sure the search without the fake genre does not fail.
		mvc.perform(MockMvcRequestBuilders.get("/search").param("query", "Jukebox"))
				.andExpect(MockMvcResultMatchers.status().isOk())
				.andExpect(MockMvcResultMatchers.content().contentType("application/json"))
				.andExpect(MockMvcResultMatchers.jsonPath("$.items[0].title.primaryTitle").value("Jukebox"))
				.andExpect(MockMvcResultMatchers.jsonPath("$.items[0].title.id").value("tt0077440"));

		mvc.perform(MockMvcRequestBuilders.get("/search").param("query", "Jukebox").param("genres", "fakeGenre"))
				.andExpect(MockMvcResultMatchers.status().isOk())
				.andExpect(MockMvcResultMatchers.content().contentType("application/json"))
				.andExpect(MockMvcResultMatchers.jsonPath("$.items.length()").value("0"));
	}

	@Test
	void searchWithNonExistantType() throws Exception {
		// Make sure the search without the fake type does not fail.
		mvc.perform(MockMvcRequestBuilders.get("/search").param("query", "Jukebox"))
				.andExpect(MockMvcResultMatchers.status().isOk())
				.andExpect(MockMvcResultMatchers.content().contentType("application/json"))
				.andExpect(MockMvcResultMatchers.jsonPath("$.items[0].title.primaryTitle").value("Jukebox"))
				.andExpect(MockMvcResultMatchers.jsonPath("$.items[0].title.id").value("tt0077440"));

		mvc.perform(MockMvcRequestBuilders.get("/search").param("query", "Jukebox").param("types", "fakeType"))
				.andExpect(MockMvcResultMatchers.status().isOk())
				.andExpect(MockMvcResultMatchers.content().contentType("application/json"))
				.andExpect(MockMvcResultMatchers.jsonPath("$.items.length()").value("0"));
	}

	@Test
	void searchWithOneGenreFilter() throws Exception {
		// First test that all four appear with no filter.
		checkTheReturnNoFilter();

		// Now test the filter, only the drama movie from 2003 (a really good one) and a
		// short from 1916 should appear.
		mvc.perform(MockMvcRequestBuilders.get("/search").param("query", RETURN_MOVIE_NAME).param("genres", "Drama"))
				.andExpect(MockMvcResultMatchers.status().isOk())
				.andExpect(MockMvcResultMatchers.content().contentType("application/json"))
				.andExpect(MockMvcResultMatchers.jsonPath("$.items[*].title.id")
						.value(Matchers.containsInAnyOrder(RETURN_SHORT_1916_DRAMASHORT_ID,
								RETURN_MOVIE_2003_DRAMA_ID)));
	}

	@Test
	void searchWithOneGenreFilterAndIgnoreCase() throws Exception {
		// First test that all four appear with no filter.
		checkTheReturnNoFilter();

		// Now test the filter, only the drama movie from 2003 (a really good one) and a
		// short from 1916 should appear.
		mvc.perform(MockMvcRequestBuilders.get("/search").param("query", RETURN_MOVIE_NAME).param("genres", "drama"))
				.andExpect(MockMvcResultMatchers.status().isOk())
				.andExpect(MockMvcResultMatchers.content().contentType("application/json"))
				.andExpect(MockMvcResultMatchers.jsonPath("$.items[*].title.id")
						.value(Matchers.containsInAnyOrder(RETURN_SHORT_1916_DRAMASHORT_ID,
								RETURN_MOVIE_2003_DRAMA_ID)));

		mvc.perform(MockMvcRequestBuilders.get("/search").param("query", RETURN_MOVIE_NAME).param("genres", "DRAMA"))
				.andExpect(MockMvcResultMatchers.status().isOk())
				.andExpect(MockMvcResultMatchers.content().contentType("application/json"))
				.andExpect(MockMvcResultMatchers.jsonPath("$.items[*].title.id")
						.value(Matchers.containsInAnyOrder(RETURN_SHORT_1916_DRAMASHORT_ID,
								RETURN_MOVIE_2003_DRAMA_ID)));
	}

	@Test
	void paginationTest() throws Exception {
		// First test that all four appear.
		checkTheReturnNoFilter();

		int testVal = 2;
		mvc.perform(MockMvcRequestBuilders.get("/search").param("query", RETURN_MOVIE_NAME).param("rows",
				Integer.toString(testVal)))
				.andExpect(MockMvcResultMatchers.status().isOk())
				.andExpect(MockMvcResultMatchers.content().contentType("application/json"))
				.andExpect(MockMvcResultMatchers.jsonPath("$.items.length()")
						.value(testVal));

		// Test start at 4th (inclusive)
		mvc.perform(MockMvcRequestBuilders.get("/search").param("query", RETURN_MOVIE_NAME).param("rows",
				"4").param("start", "4"))
				.andExpect(MockMvcResultMatchers.status().isOk())
				.andExpect(MockMvcResultMatchers.content().contentType("application/json"))
				.andExpect(MockMvcResultMatchers.jsonPath("$.items.length()")
						.value(1));
	}

	@Test
	void paginationTestStartAfterHitN() throws Exception {
		// First test that all four appear.
		checkTheReturnNoFilter();

		// Now, if we start at 5, there should not be any result.
		mvc.perform(MockMvcRequestBuilders.get("/search").param("query", RETURN_MOVIE_NAME).param("rows",
				"4").param("start", "5"))
				.andExpect(MockMvcResultMatchers.status().isOk())
				.andExpect(MockMvcResultMatchers.content().contentType("application/json"))
				.andExpect(MockMvcResultMatchers.jsonPath("$.items.length()")
						.value(0));
	}

	@Test
	void paginationTest0Rows() throws Exception {
		// First test that all four appear.
		checkTheReturnNoFilter();

		int testVal = 0;
		mvc.perform(MockMvcRequestBuilders.get("/search").param("query", RETURN_MOVIE_NAME).param("rows",
				Integer.toString(testVal)))
				.andExpect(MockMvcResultMatchers.status().isOk())
				.andExpect(MockMvcResultMatchers.content().contentType("application/json"))
				.andExpect(MockMvcResultMatchers.jsonPath("$.items.length()")
						.value(testVal));
	}

	@Test
	void searchWithMultipleGenreFilter() throws Exception {
		// First test that all four appear with no filter.
		checkTheReturnNoFilter();

		// Now test the filter, only the scifi and the romance movie should appear
		mvc.perform(
				MockMvcRequestBuilders.get("/search").param("query", RETURN_MOVIE_NAME).param("genres", "Romance",
						"Sci-Fi"))
				.andExpect(MockMvcResultMatchers.status().isOk())
				.andExpect(MockMvcResultMatchers.content().contentType("application/json"))
				.andExpect(MockMvcResultMatchers.jsonPath("$.items[*].title.id")
						.value(Matchers.containsInAnyOrder(RETURN_MOVIE_ROMANCE_ID, RETURN_MOVIE_2021_SCIFI_ID)));
	}

	@Test
	void searchWithOneTypeFilter() throws Exception {
		// First test that all four appear with no filter.
		checkTheReturnNoFilter();

		// Now test the filter, only the short should appear
		mvc.perform(MockMvcRequestBuilders.get("/search").param("query", RETURN_MOVIE_NAME).param("types", "short"))
				.andExpect(MockMvcResultMatchers.status().isOk())
				.andExpect(MockMvcResultMatchers.content().contentType("application/json"))
				.andExpect(MockMvcResultMatchers.jsonPath("$.items[*].title.id")
						.value(Matchers.containsInAnyOrder(RETURN_SHORT_1916_DRAMASHORT_ID)));
	}

	@Test
	void searchWithOneTypeFilterAndIgnoreCase() throws Exception {
		// First test that all four appear with no filter.
		checkTheReturnNoFilter();

		// Now test the filter, only the short should appear ("short" is the exact type
		// name)
		mvc.perform(MockMvcRequestBuilders.get("/search").param("query", RETURN_MOVIE_NAME).param("types", "SHORT"))
				.andExpect(MockMvcResultMatchers.status().isOk())
				.andExpect(MockMvcResultMatchers.content().contentType("application/json"))
				.andExpect(MockMvcResultMatchers.jsonPath("$.items[*].title.id")
						.value(Matchers.containsInAnyOrder(RETURN_SHORT_1916_DRAMASHORT_ID)));

		// Now test the filter, only the short should appear ("short" is the exact type
		// name)

		mvc.perform(MockMvcRequestBuilders.get("/search").param("query", RETURN_MOVIE_NAME).param("types", "Short"))
				.andExpect(MockMvcResultMatchers.status().isOk())
				.andExpect(MockMvcResultMatchers.content().contentType("application/json"))
				.andExpect(MockMvcResultMatchers.jsonPath("$.items[*].title.id")
						.value(Matchers.containsInAnyOrder(RETURN_SHORT_1916_DRAMASHORT_ID)));
	}

	@Test
	void searchWithMultipleTypeFilter() throws Exception {
		// First test that all four appear with no filter.
		checkTheReturnNoFilter();

		// Now test the filter, only the short and tv episode should appear
		mvc.perform(
				MockMvcRequestBuilders.get("/search").param("query", RETURN_MOVIE_NAME).param("types", "short",
						"tvEpisode"))
				.andExpect(MockMvcResultMatchers.status().isOk())
				.andExpect(MockMvcResultMatchers.content().contentType("application/json"))
				.andExpect(MockMvcResultMatchers.jsonPath("$.items[*].title.id")
						.value(Matchers.containsInAnyOrder(RETURN_TV_1960_WESTERN_ID,
								RETURN_SHORT_1916_DRAMASHORT_ID)));
	}

	@Test
	void searchWithGenreAndTypeFilter() throws Exception {
		// First test that all four appear with no filter.
		checkTheReturnNoFilter();

		// Now test the filter, only the romance movie should appear
		mvc.perform(MockMvcRequestBuilders.get("/search").param("query", RETURN_MOVIE_NAME).param("types", "movie")
				.param("genres", "Romance"))
				.andExpect(MockMvcResultMatchers.status().isOk())
				.andExpect(MockMvcResultMatchers.content().contentType("application/json"))
				.andExpect(MockMvcResultMatchers.jsonPath("$.items[*].title.id")
						.value(Matchers.containsInAnyOrder(RETURN_MOVIE_ROMANCE_ID)));
	}

	@Test
	void searchNoQueryParam() throws Exception {
		mvc.perform(MockMvcRequestBuilders.get("/search"))
				.andExpect(MockMvcResultMatchers.status().isBadRequest());
	}

	@Test
	void searchWithSingleDateRange() throws Exception {
		// First test that all four appear with no filter.
		checkTheReturnNoFilter();

		// Now only the movies from 1960 to 2021 (both included) should appear
		mvc.perform(MockMvcRequestBuilders.get("/search").param("query", RETURN_MOVIE_NAME).param("years", "1960/2021"))
				.andExpect(MockMvcResultMatchers.status().isOk())
				.andExpect(MockMvcResultMatchers.content().contentType("application/json"))
				.andExpect(MockMvcResultMatchers.jsonPath("$.items[*].title.id")
						.value(Matchers.containsInAnyOrder(RETURN_TV_1960_WESTERN_ID, RETURN_MOVIE_2003_DRAMA_ID,
								RETURN_MOVIE_2021_SCIFI_ID)));
	}

	@Test
	void searchWithMultipleDateRanges() throws Exception {
		// First test that all four appear with no filter.
		checkTheReturnNoFilter();

		// Now only from 1916 to 1960 (both included) and from 2021 should appear
		mvc.perform(MockMvcRequestBuilders.get("/search").param("query", RETURN_MOVIE_NAME).param("years", "1916/1960",
				"2021/2022"))
				.andExpect(MockMvcResultMatchers.status().isOk())
				.andExpect(MockMvcResultMatchers.content().contentType("application/json"))
				.andExpect(MockMvcResultMatchers.jsonPath("$.items[*].title.id")
						.value(Matchers.containsInAnyOrder(RETURN_TV_1960_WESTERN_ID, RETURN_MOVIE_2021_SCIFI_ID,
								RETURN_SHORT_1916_DRAMASHORT_ID)));
	}

	@Test
	void searchWithSingleDateInDateRangesParam() throws Exception {
		// First test that all four appear with no filter.
		checkTheReturnNoFilter();

		// Now only 1916 should appear
		mvc.perform(MockMvcRequestBuilders.get("/search").param("query", RETURN_MOVIE_NAME).param("years", "1916"))
				.andExpect(MockMvcResultMatchers.status().isOk())
				.andExpect(MockMvcResultMatchers.content().contentType("application/json"))
				.andExpect(MockMvcResultMatchers.jsonPath("$.items[*].title.id")
						.value(Matchers.containsInAnyOrder(RETURN_SHORT_1916_DRAMASHORT_ID)));
	}

	@Test
	void searchWithSingleDateAndRangeParam() throws Exception {
		// First test that all four appear with no filter.
		checkTheReturnNoFilter();

		// Now only 1916 should appear
		mvc.perform(MockMvcRequestBuilders.get("/search").param("query", RETURN_MOVIE_NAME).param("years", "1916")
				.param("years", "2003/2021"))
				.andExpect(MockMvcResultMatchers.status().isOk())
				.andExpect(MockMvcResultMatchers.content().contentType("application/json"))
				.andExpect(MockMvcResultMatchers.jsonPath("$.items[*].title.id")
						.value(Matchers.containsInAnyOrder(RETURN_SHORT_1916_DRAMASHORT_ID, RETURN_MOVIE_2003_DRAMA_ID,
								RETURN_MOVIE_2021_SCIFI_ID)));
	}

	@Test
	void searchWithDashedDateRange() throws Exception {
		// If years are separated by a dash, it should throw BadRequest.
		mvc.perform(MockMvcRequestBuilders.get("/search").param("query", RETURN_MOVIE_NAME).param("years", "1916-1920"))
				.andExpect(MockMvcResultMatchers.status().isBadRequest());
	}

	@Test
	void searchWithSeparatedLettersInDateRange() throws Exception {
		// If years are separated by a dash, it should throw BadRequest.
		mvc.perform(MockMvcRequestBuilders.get("/search").param("query", RETURN_MOVIE_NAME).param("years", "asdfa/sdf"))
				.andExpect(MockMvcResultMatchers.status().isBadRequest());
	}

	@Test
	void searchWithLettersInDateRange() throws Exception {
		// If years are separated by a dash, it should throw BadRequest.
		mvc.perform(MockMvcRequestBuilders.get("/search").param("query", RETURN_MOVIE_NAME).param("years", "asdfasdf"))
				.andExpect(MockMvcResultMatchers.status().isBadRequest());
	}

	@Test
	void searchDateRangeAggregation() throws Exception {
		mvc.perform(MockMvcRequestBuilders.get("/search").param("query", RETURN_MOVIE_NAME))
				.andExpect(MockMvcResultMatchers.status().isOk())
				.andExpect(MockMvcResultMatchers.content().contentType("application/json"))
				.andExpect(MockMvcResultMatchers.jsonPath("$.aggregations.years['1910 - 1920']").value(1))
				.andExpect(MockMvcResultMatchers.jsonPath("$.aggregations.years['1920 - 1930']").doesNotExist())
				.andExpect(MockMvcResultMatchers.jsonPath("$.aggregations.years['1930 - 1940']").doesNotExist())
				.andExpect(MockMvcResultMatchers.jsonPath("$.aggregations.years['1950 - 1960']").doesNotExist())
				.andExpect(MockMvcResultMatchers.jsonPath("$.aggregations.years['1960 - 1970']").value(1))
				.andExpect(MockMvcResultMatchers.jsonPath("$.aggregations.years['1970 - 1980']").doesNotExist())
				.andExpect(MockMvcResultMatchers.jsonPath("$.aggregations.years['2000 - 2010']").value(1))
				.andExpect(MockMvcResultMatchers.jsonPath("$.aggregations.years['2020 - 2030']").value(1))
				.andExpect(MockMvcResultMatchers.jsonPath("$.items[*].title.id")
						.value(Matchers.containsInAnyOrder(RETURN_SHORT_1916_DRAMASHORT_ID, RETURN_TV_1960_WESTERN_ID,
								RETURN_MOVIE_2003_DRAMA_ID, RETURN_MOVIE_2021_SCIFI_ID,
								RETURN_MOVIE_ROMANCE_ID)));
	}

	@Test
	void searchGenresAggregation() throws Exception {
		mvc.perform(MockMvcRequestBuilders.get("/search").param("query", RETURN_MOVIE_NAME))
				.andExpect(MockMvcResultMatchers.status().isOk())
				.andExpect(MockMvcResultMatchers.content().contentType("application/json"))
				.andExpect(MockMvcResultMatchers.jsonPath("$.aggregations.genres.Western").value(1))
				.andExpect(MockMvcResultMatchers.jsonPath("$.aggregations.genres.Drama").value(2))
				.andExpect(MockMvcResultMatchers.jsonPath("$.aggregations.genres.Short").value(1))
				.andExpect(MockMvcResultMatchers.jsonPath("$.aggregations.genres.Romance").value(1))
				.andExpect(MockMvcResultMatchers.jsonPath("$.aggregations.genres['Sci-Fi']").value(1))
				.andExpect(MockMvcResultMatchers.jsonPath("$.items[*].title.id")
						.value(Matchers.containsInAnyOrder(RETURN_SHORT_1916_DRAMASHORT_ID, RETURN_TV_1960_WESTERN_ID,
								RETURN_MOVIE_2003_DRAMA_ID, RETURN_MOVIE_2021_SCIFI_ID,
								RETURN_MOVIE_ROMANCE_ID)));
	}

	@Test
	void searchTypesAggregation() throws Exception {
		mvc.perform(MockMvcRequestBuilders.get("/search").param("query", RETURN_MOVIE_NAME))
				.andExpect(MockMvcResultMatchers.status().isOk())
				.andExpect(MockMvcResultMatchers.content().contentType("application/json"))
				.andExpect(MockMvcResultMatchers.jsonPath("$.aggregations.types.short").value(1))
				.andExpect(MockMvcResultMatchers.jsonPath("$.aggregations.types.tvEpisode").value(1))
				.andExpect(MockMvcResultMatchers.jsonPath("$.aggregations.types.movie").value(3))
				.andExpect(MockMvcResultMatchers.jsonPath("$.items[*].title.id")
						.value(Matchers.containsInAnyOrder(RETURN_SHORT_1916_DRAMASHORT_ID, RETURN_TV_1960_WESTERN_ID,
								RETURN_MOVIE_2003_DRAMA_ID, RETURN_MOVIE_2021_SCIFI_ID,
								RETURN_MOVIE_ROMANCE_ID)));
	}

	private void checkTheReturnNoFilter() throws Exception {
		mvc.perform(MockMvcRequestBuilders.get("/search").param("query", RETURN_MOVIE_NAME))
				.andExpect(MockMvcResultMatchers.status().isOk())
				.andExpect(MockMvcResultMatchers.content().contentType("application/json"))
				.andExpect(MockMvcResultMatchers.jsonPath("$.items[*].title.id")
						.value(Matchers.containsInAnyOrder(RETURN_SHORT_1916_DRAMASHORT_ID, RETURN_MOVIE_2003_DRAMA_ID,
								RETURN_MOVIE_ROMANCE_ID, RETURN_MOVIE_2021_SCIFI_ID,
								RETURN_TV_1960_WESTERN_ID)));
	}
}

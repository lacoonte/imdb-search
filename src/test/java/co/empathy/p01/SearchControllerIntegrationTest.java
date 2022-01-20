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
import co.empathy.p01.app.index.TitlesFileNotExistsExcetion;

@SpringBootTest
@AutoConfigureMockMvc
class SearchControllerIntegrationTest extends ElasticContainerBaseTest {

	@Autowired
	private MockMvc mvc;

	@BeforeAll
	static void setUp(@Autowired TitleIndexService service)
			throws IOException, IndexAlreadyExistsException, IndexFailedException, TitlesFileNotExistsExcetion {
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
				.andExpect(MockMvcResultMatchers.jsonPath("$.items[0].primaryTitle").value("Jukebox"))
				.andExpect(MockMvcResultMatchers.jsonPath("$.items[0].id").value("tt0077440"));
	}

	@Test
	void ignoreHyphen() throws Exception {
		var resultMatcher = Matchers.containsInAnyOrder("tt0100669", "tt0145487");
		mvc.perform(MockMvcRequestBuilders.get("/search").param("query", "Spiderman"))
				.andExpect(MockMvcResultMatchers.status().isOk())
				.andExpect(MockMvcResultMatchers.content().contentType("application/json"))
				.andExpect(MockMvcResultMatchers.jsonPath("$..items[:2].id").value(resultMatcher));

		mvc.perform(MockMvcRequestBuilders.get("/search").param("query", "Spider-man"))
				.andExpect(MockMvcResultMatchers.status().isOk())
				.andExpect(MockMvcResultMatchers.content().contentType("application/json"))
				.andExpect(MockMvcResultMatchers.jsonPath("$..items[:2].id").value(resultMatcher));
	}

	@Test
	void naturalToRomanNumerals() throws Exception {
		mvc.perform(MockMvcRequestBuilders.get("/search").param("query", "5 for Vendetta"))
				.andExpect(MockMvcResultMatchers.status().isOk())
				.andExpect(MockMvcResultMatchers.content().contentType("application/json"))
				.andExpect(MockMvcResultMatchers.jsonPath("$.items[0].primaryTitle").value("V for Vendetta"))
				.andExpect(MockMvcResultMatchers.jsonPath("$.items[0].id").value("tt0434409"));
	}

	@Test
	void romanNumeralsToNatural() throws Exception {
		mvc.perform(MockMvcRequestBuilders.get("/search").param("query", "Rocky 5"))
				.andExpect(MockMvcResultMatchers.status().isOk())
				.andExpect(MockMvcResultMatchers.content().contentType("application/json"))
				.andExpect(MockMvcResultMatchers.jsonPath("$.items[0].primaryTitle").value("Rocky V"))
				.andExpect(MockMvcResultMatchers.jsonPath("$.items[0].id").value("tt0100507"));
	}

	@Test
	void ignoreAccents() throws Exception {
		mvc.perform(MockMvcRequestBuilders.get("/search").param("query", "L'enfant temoin"))
				.andExpect(MockMvcResultMatchers.status().isOk())
				.andExpect(MockMvcResultMatchers.content().contentType("application/json"))
				.andExpect(MockMvcResultMatchers.jsonPath("$.items[0].primaryTitle").value("L'enfant témoin"))
				.andExpect(MockMvcResultMatchers.jsonPath("$.items[0].id").value("tt0113495"));
	}

	@Test
	void ignoreCaps() throws Exception {
		mvc.perform(MockMvcRequestBuilders.get("/search").param("query", "jukebox"))
				.andExpect(MockMvcResultMatchers.status().isOk())
				.andExpect(MockMvcResultMatchers.content().contentType("application/json"))
				.andExpect(MockMvcResultMatchers.jsonPath("$.items[0].primaryTitle").value("Jukebox"))
				.andExpect(MockMvcResultMatchers.jsonPath("$.items[0].id").value("tt0077440"));
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
				.andExpect(MockMvcResultMatchers.jsonPath("$.items[0].primaryTitle").value("Jukebox"))
				.andExpect(MockMvcResultMatchers.jsonPath("$.items[0].id").value("tt0077440"));

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
				.andExpect(MockMvcResultMatchers.jsonPath("$.items[0].primaryTitle").value("Jukebox"))
				.andExpect(MockMvcResultMatchers.jsonPath("$.items[0].id").value("tt0077440"));

		mvc.perform(MockMvcRequestBuilders.get("/search").param("query", "Jukebox").param("types", "fakeType"))
				.andExpect(MockMvcResultMatchers.status().isOk())
				.andExpect(MockMvcResultMatchers.content().contentType("application/json"))
				.andExpect(MockMvcResultMatchers.jsonPath("$.items.length()").value("0"));
	}

	@Test
	void searchWithOneGenreFilter() throws Exception {
		// First test that all four appear with no filter.
		var dramaShortId = "tt0221515";
		var dramaMovieId = "tt0376968";
		var romanceMovieId = "tt14549854";
		var sciFiMovieId = "tt14360612";
		var westernShowId = "tt0526635";

		mvc.perform(MockMvcRequestBuilders.get("/search").param("query", "The Return"))
				.andExpect(MockMvcResultMatchers.status().isOk())
				.andExpect(MockMvcResultMatchers.content().contentType("application/json"))
				.andExpect(MockMvcResultMatchers.jsonPath("$.items[*].id")
						.value(Matchers.containsInAnyOrder(dramaShortId, dramaMovieId, romanceMovieId, sciFiMovieId,
								westernShowId)));

		// Now test the filter, only the drama movie from 2003 (a really good one) and a
		// short from 1916 should appear.
		mvc.perform(MockMvcRequestBuilders.get("/search").param("query", "The Return").param("genres", "Drama"))
				.andExpect(MockMvcResultMatchers.status().isOk())
				.andExpect(MockMvcResultMatchers.content().contentType("application/json"))
				.andExpect(MockMvcResultMatchers.jsonPath("$.items[*].id")
						.value(Matchers.containsInAnyOrder(dramaShortId, dramaMovieId)));
	}

	@Test
	void searchWithMultipleGenreFilter() throws Exception {
		// First test that all four appear with no filter.
		var dramaShortId = "tt0221515";
		var dramaMovieId = "tt0376968";
		var romanceMovieId = "tt14549854";
		var sciFiMovieId = "tt14360612";
		var westernShowId = "tt0526635";

		mvc.perform(MockMvcRequestBuilders.get("/search").param("query", "The Return"))
				.andExpect(MockMvcResultMatchers.status().isOk())
				.andExpect(MockMvcResultMatchers.content().contentType("application/json"))
				.andExpect(MockMvcResultMatchers.jsonPath("$.items[*].id")
						.value(Matchers.containsInAnyOrder(dramaShortId, dramaMovieId, romanceMovieId, sciFiMovieId,
								westernShowId)));

		// Now test the filter, only the scifi and the romance movie should appear
		mvc.perform(
				MockMvcRequestBuilders.get("/search").param("query", "The Return").param("genres", "Romance", "Sci-Fi"))
				.andExpect(MockMvcResultMatchers.status().isOk())
				.andExpect(MockMvcResultMatchers.content().contentType("application/json"))
				.andExpect(MockMvcResultMatchers.jsonPath("$.items[*].id")
						.value(Matchers.containsInAnyOrder(romanceMovieId, sciFiMovieId)));
	}

	@Test
	void searchWithOneTypeFilter() throws Exception {
		// First test that all four appear with no filter.
		var dramaShortId = "tt0221515";
		var dramaMovieId = "tt0376968";
		var romanceMovieId = "tt14549854";
		var sciFiMovieId = "tt14360612";
		var westernShowId = "tt0526635";

		mvc.perform(MockMvcRequestBuilders.get("/search").param("query", "The Return"))
				.andExpect(MockMvcResultMatchers.status().isOk())
				.andExpect(MockMvcResultMatchers.content().contentType("application/json"))
				.andExpect(MockMvcResultMatchers.jsonPath("$.items[*].id")
						.value(Matchers.containsInAnyOrder(dramaShortId, dramaMovieId, romanceMovieId, sciFiMovieId,
								westernShowId)));

		// Now test the filter, only the short should appear
		mvc.perform(MockMvcRequestBuilders.get("/search").param("query", "The Return").param("types", "short"))
				.andExpect(MockMvcResultMatchers.status().isOk())
				.andExpect(MockMvcResultMatchers.content().contentType("application/json"))
				.andExpect(MockMvcResultMatchers.jsonPath("$.items[*].id")
						.value(Matchers.containsInAnyOrder(dramaShortId)));
	}

	@Test
	void searchWithMultipleTypeFilter() throws Exception {
		// First test that all four appear with no filter.
		var dramaShortId = "tt0221515";
		var dramaMovieId = "tt0376968";
		var romanceMovieId = "tt14549854";
		var sciFiMovieId = "tt14360612";
		var westernShowId = "tt0526635";

		mvc.perform(MockMvcRequestBuilders.get("/search").param("query", "The Return"))
				.andExpect(MockMvcResultMatchers.status().isOk())
				.andExpect(MockMvcResultMatchers.content().contentType("application/json"))
				.andExpect(MockMvcResultMatchers.jsonPath("$.items[*].id")
						.value(Matchers.containsInAnyOrder(dramaShortId, dramaMovieId, romanceMovieId, sciFiMovieId,
								westernShowId)));

		// Now test the filter, only the short and tv episode should appear
		mvc.perform(
				MockMvcRequestBuilders.get("/search").param("query", "The Return").param("types", "short", "tvEpisode"))
				.andExpect(MockMvcResultMatchers.status().isOk())
				.andExpect(MockMvcResultMatchers.content().contentType("application/json"))
				.andExpect(MockMvcResultMatchers.jsonPath("$.items[*].id")
						.value(Matchers.containsInAnyOrder(westernShowId, dramaShortId)));
	}

	@Test
	void searchWithGenreAndTypeFilter() throws Exception {
		// First test that all four appear with no filter.
		var dramaShortId = "tt0221515";
		var dramaMovieId = "tt0376968";
		var romanceMovieId = "tt14549854";
		var sciFiMovieId = "tt14360612";
		var westernShowId = "tt0526635";

		mvc.perform(MockMvcRequestBuilders.get("/search").param("query", "The Return"))
				.andExpect(MockMvcResultMatchers.status().isOk())
				.andExpect(MockMvcResultMatchers.content().contentType("application/json"))
				.andExpect(MockMvcResultMatchers.jsonPath("$.items[*].id")
						.value(Matchers.containsInAnyOrder(dramaShortId, dramaMovieId, romanceMovieId, sciFiMovieId,
								westernShowId)));

		// Now test the filter, only the romance movie should appear
		mvc.perform(MockMvcRequestBuilders.get("/search").param("query", "The Return").param("types", "movie")
				.param("genres", "Romance"))
				.andExpect(MockMvcResultMatchers.status().isOk())
				.andExpect(MockMvcResultMatchers.content().contentType("application/json"))
				.andExpect(MockMvcResultMatchers.jsonPath("$.items[*].id")
						.value(Matchers.containsInAnyOrder(romanceMovieId)));
	}

	@Test
	void searchNoQueryParam() throws Exception {
		mvc.perform(MockMvcRequestBuilders.get("/search"))
				.andExpect(MockMvcResultMatchers.status().isBadRequest());
	}

	@Test
	void searchWithSingleDateRange() throws Exception {
		// First test that all four appear with no filter.
		var short1916Id = "tt0221515";
		var show1960Id = "tt0526635";
		var movie2003Id = "tt0376968";
		var movie2021Id = "tt14360612";
		var showNoYearId = "tt14549854";

		mvc.perform(MockMvcRequestBuilders.get("/search").param("query", "The Return"))
				.andExpect(MockMvcResultMatchers.status().isOk())
				.andExpect(MockMvcResultMatchers.content().contentType("application/json"))
				.andExpect(MockMvcResultMatchers.jsonPath("$.items[*].id")
						.value(Matchers.containsInAnyOrder(short1916Id, show1960Id, movie2003Id, movie2021Id,
								showNoYearId)));

		// Now only the movies from 1960 to 2021 (both included) should appear
		mvc.perform(MockMvcRequestBuilders.get("/search").param("query", "The Return").param("years", "1960/2021"))
				.andExpect(MockMvcResultMatchers.status().isOk())
				.andExpect(MockMvcResultMatchers.content().contentType("application/json"))
				.andExpect(MockMvcResultMatchers.jsonPath("$.items[*].id")
						.value(Matchers.containsInAnyOrder(show1960Id, movie2003Id, movie2021Id)));
	}

	@Test
	void searchWithMultipleDateRanges() throws Exception {
		// First test that all four appear with no filter.
		var short1916Id = "tt0221515";
		var show1960Id = "tt0526635";
		var movie2003Id = "tt0376968";
		var movie2021Id = "tt14360612";
		var showNoYearId = "tt14549854";

		mvc.perform(MockMvcRequestBuilders.get("/search").param("query", "The Return"))
				.andExpect(MockMvcResultMatchers.status().isOk())
				.andExpect(MockMvcResultMatchers.content().contentType("application/json"))
				.andExpect(MockMvcResultMatchers.jsonPath("$.items[*].id")
						.value(Matchers.containsInAnyOrder(short1916Id, show1960Id, movie2003Id, movie2021Id,
								showNoYearId)));

		// Now only from 1916 to 1960 (both included) and from 2021 should appear
		mvc.perform(MockMvcRequestBuilders.get("/search").param("query", "The Return").param("years", "1916/1960",
				"2021/2022"))
				.andExpect(MockMvcResultMatchers.status().isOk())
				.andExpect(MockMvcResultMatchers.content().contentType("application/json"))
				.andExpect(MockMvcResultMatchers.jsonPath("$.items[*].id")
						.value(Matchers.containsInAnyOrder(show1960Id, movie2021Id, short1916Id)));
	}

	@Test
	void searchWithSingleDateInDateRangesParam() throws Exception {
		// First test that all four appear with no filter.
		var short1916Id = "tt0221515";
		var show1960Id = "tt0526635";
		var movie2003Id = "tt0376968";
		var movie2021Id = "tt14360612";
		var showNoYearId = "tt14549854";

		mvc.perform(MockMvcRequestBuilders.get("/search").param("query", "The Return"))
				.andExpect(MockMvcResultMatchers.status().isOk())
				.andExpect(MockMvcResultMatchers.content().contentType("application/json"))
				.andExpect(MockMvcResultMatchers.jsonPath("$.items[*].id")
						.value(Matchers.containsInAnyOrder(short1916Id, show1960Id, movie2003Id, movie2021Id,
								showNoYearId)));

		// Now only 1916 should appear
		mvc.perform(MockMvcRequestBuilders.get("/search").param("query", "The Return").param("years", "1916"))
				.andExpect(MockMvcResultMatchers.status().isOk())
				.andExpect(MockMvcResultMatchers.content().contentType("application/json"))
				.andExpect(MockMvcResultMatchers.jsonPath("$.items[*].id")
						.value(Matchers.containsInAnyOrder(short1916Id)));
	}

	@Test
	void searchWithSingleDateAndRangeParam() throws Exception {
		// First test that all four appear with no filter.
		var short1916Id = "tt0221515";
		var show1960Id = "tt0526635";
		var movie2003Id = "tt0376968";
		var movie2021Id = "tt14360612";
		var showNoYearId = "tt14549854";

		mvc.perform(MockMvcRequestBuilders.get("/search").param("query", "The Return"))
				.andExpect(MockMvcResultMatchers.status().isOk())
				.andExpect(MockMvcResultMatchers.content().contentType("application/json"))
				.andExpect(MockMvcResultMatchers.jsonPath("$.items[*].id")
						.value(Matchers.containsInAnyOrder(short1916Id, show1960Id, movie2003Id, movie2021Id,
								showNoYearId)));

		// Now only 1916 should appear
		mvc.perform(MockMvcRequestBuilders.get("/search").param("query", "The Return").param("years", "1916")
				.param("years", "2003/2021"))
				.andExpect(MockMvcResultMatchers.status().isOk())
				.andExpect(MockMvcResultMatchers.content().contentType("application/json"))
				.andExpect(MockMvcResultMatchers.jsonPath("$.items[*].id")
						.value(Matchers.containsInAnyOrder(short1916Id, movie2003Id, movie2021Id)));
	}

	@Test
	void searchWithDashedDateRange() throws Exception {
		// If years are separated by a dash, it should throw BadRequest.
		mvc.perform(MockMvcRequestBuilders.get("/search").param("query", "The Return").param("years", "1916-1920"))
				.andExpect(MockMvcResultMatchers.status().isBadRequest());
	}

	@Test
	void searchDateRangeAggregation() throws Exception {
		// First test that all four appear with no filter.
		var short1916Id = "tt0221515";
		var show1960Id = "tt0526635";
		var movie2003Id = "tt0376968";
		var movie2021Id = "tt14360612";
		var showNoYearId = "tt14549854";

		mvc.perform(MockMvcRequestBuilders.get("/search").param("query", "The Return"))
				.andExpect(MockMvcResultMatchers.status().isOk())
				.andExpect(MockMvcResultMatchers.content().contentType("application/json"))
				.andExpect(MockMvcResultMatchers.jsonPath("$.aggregations.years['1910 - 1920']").value(1))
				.andExpect(MockMvcResultMatchers.jsonPath("$.aggregations.years['1960 - 1970']").value(1))
				.andExpect(MockMvcResultMatchers.jsonPath("$.aggregations.years['2000 - 2010']").value(1))
				.andExpect(MockMvcResultMatchers.jsonPath("$.aggregations.years['2020 - 2030']").value(1))
				.andExpect(MockMvcResultMatchers.jsonPath("$.items[*].id")
						.value(Matchers.containsInAnyOrder(short1916Id, show1960Id, movie2003Id, movie2021Id,
								showNoYearId)));
	}

	@Test
	void searchGenresAggregation() throws Exception {
		// First test that all four appear with no filter.
		var short1916Id = "tt0221515";
		var show1960Id = "tt0526635";
		var movie2003Id = "tt0376968";
		var movie2021Id = "tt14360612";
		var showNoYearId = "tt14549854";

		mvc.perform(MockMvcRequestBuilders.get("/search").param("query", "The Return"))
				.andExpect(MockMvcResultMatchers.status().isOk())
				.andExpect(MockMvcResultMatchers.content().contentType("application/json"))
				.andExpect(MockMvcResultMatchers.jsonPath("$.aggregations.genres.Western").value(1))
				.andExpect(MockMvcResultMatchers.jsonPath("$.aggregations.genres.Drama").value(2))
				.andExpect(MockMvcResultMatchers.jsonPath("$.aggregations.genres.Short").value(1))
				.andExpect(MockMvcResultMatchers.jsonPath("$.aggregations.genres.Romance").value(1))
				.andExpect(MockMvcResultMatchers.jsonPath("$.aggregations.genres['Sci-Fi']").value(1))
				.andExpect(MockMvcResultMatchers.jsonPath("$.items[*].id")
						.value(Matchers.containsInAnyOrder(short1916Id, show1960Id, movie2003Id, movie2021Id,
								showNoYearId)));
	}

	@Test
	void searchTypesAggregation() throws Exception {
		// First test that all four appear with no filter.
		var short1916Id = "tt0221515";
		var show1960Id = "tt0526635";
		var movie2003Id = "tt0376968";
		var movie2021Id = "tt14360612";
		var showNoYearId = "tt14549854";

		mvc.perform(MockMvcRequestBuilders.get("/search").param("query", "The Return"))
				.andExpect(MockMvcResultMatchers.status().isOk())
				.andExpect(MockMvcResultMatchers.content().contentType("application/json"))
				.andExpect(MockMvcResultMatchers.jsonPath("$.aggregations.types.short").value(1))
				.andExpect(MockMvcResultMatchers.jsonPath("$.aggregations.types.tvEpisode").value(1))
				.andExpect(MockMvcResultMatchers.jsonPath("$.aggregations.types.movie").value(3))
				.andExpect(MockMvcResultMatchers.jsonPath("$.items[*].id")
						.value(Matchers.containsInAnyOrder(short1916Id, show1960Id, movie2003Id, movie2021Id,
								showNoYearId)));
	}
}

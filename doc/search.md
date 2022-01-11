# Search

Searches the database for titles containing your query as their primary title. It returns the first 10 titles alongside the total item count.

**URL**: `/search?query=`

**Method**: `GET`

**URL Params**:

 * `query=[string]` (Required)

**Auth required**: No

**Permissions required**: None

**Request example**: `/search?query=spiderman`

## Success Response

**Condition** : If the query parameter is not empty and there's no error in ElasticSearch.

**Code** : `200 OK`

**Content example**

```json
{
   "items" : [
      {
         "endYear" : null,
         "genres" : [
            "Comedy",
            "Short"
         ],
         "id" : "tt1779548",
         "isAdult" : false,
         "originalTitle" : "Spiderman",
         "primaryTitle" : "Spiderman",
         "runtimeMinutes" : null,
         "startYear" : 2008,
         "type" : "tvEpisode"
      },
      {
         "endYear" : null,
         "genres" : [
            "Documentary",
            "Short"
         ],
         "id" : "tt1785572",
         "isAdult" : false,
         "originalTitle" : "Spiderman",
         "primaryTitle" : "Spiderman",
         "runtimeMinutes" : 15,
         "startYear" : 2010,
         "type" : "short"
      },
      {
         "endYear" : null,
         "genres" : [
            "Comedy"
         ],
         "id" : "tt6786512",
         "isAdult" : false,
         "originalTitle" : "Spiderman",
         "primaryTitle" : "Spiderman",
         "runtimeMinutes" : 22,
         "startYear" : 2015,
         "type" : "tvEpisode"
      },
      {
         "endYear" : null,
         "genres" : [
            "Crime",
            "Documentary",
            "Mystery"
         ],
         "id" : "tt0964012",
         "isAdult" : false,
         "originalTitle" : "Spiderman",
         "primaryTitle" : "Spiderman",
         "runtimeMinutes" : null,
         "startYear" : 2000,
         "type" : "tvEpisode"
      },
      {
         "endYear" : null,
         "genres" : [
            "Reality-TV"
         ],
         "id" : "tt2012885",
         "isAdult" : false,
         "originalTitle" : "Spiderman",
         "primaryTitle" : "Spiderman",
         "runtimeMinutes" : null,
         "startYear" : 2011,
         "type" : "tvEpisode"
      },
      {
         "endYear" : null,
         "genres" : [
            "Animation"
         ],
         "id" : "tt8857268",
         "isAdult" : false,
         "originalTitle" : "Spiderman",
         "primaryTitle" : "Spiderman",
         "runtimeMinutes" : null,
         "startYear" : 2018,
         "type" : "tvEpisode"
      },
      {
         "endYear" : null,
         "genres" : [
            "Comedy"
         ],
         "id" : "tt2125854",
         "isAdult" : false,
         "originalTitle" : "Spiderman",
         "primaryTitle" : "Spiderman",
         "runtimeMinutes" : 6,
         "startYear" : 2011,
         "type" : "tvEpisode"
      },
      {
         "endYear" : null,
         "genres" : [
            "News"
         ],
         "id" : "tt8535296",
         "isAdult" : false,
         "originalTitle" : "Spiderman",
         "primaryTitle" : "Spiderman",
         "runtimeMinutes" : null,
         "startYear" : 2016,
         "type" : "tvEpisode"
      },
      {
         "endYear" : null,
         "genres" : [
            "Short"
         ],
         "id" : "tt0100669",
         "isAdult" : false,
         "originalTitle" : "Spiderman",
         "primaryTitle" : "Spiderman",
         "runtimeMinutes" : 5,
         "startYear" : 1990,
         "type" : "short"
      },
      {
         "endYear" : null,
         "genres" : [
            "News"
         ],
         "id" : "tt3449184",
         "isAdult" : false,
         "originalTitle" : "Spiderman",
         "primaryTitle" : "Spiderman",
         "runtimeMinutes" : null,
         "startYear" : 2013,
         "type" : "tvEpisode"
      }
   ],
   "total" : 1541
}
```
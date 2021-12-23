package co.empathy.p01.app;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.apache.commons.collections4.ListUtils;
import org.elasticsearch.client.Request;
import org.elasticsearch.client.RestClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import co.empathy.p01.model.Title;

@Service
public class TitleIndexServiceImpl implements TitleIndexService {

    private final TitleParser titleParser;
    private final RestClient restClient;
    private final ObjectMapper mapper;

    @Autowired
    public TitleIndexServiceImpl(TitleParser titleParser, RestClient restClient) {
        this.titleParser = titleParser;
        this.restClient = restClient;
        this.mapper = new ObjectMapper();
    }

    @Override
    public void indexTitlesFromTabFile(String path) throws IOException {
        var pathObj = Paths.get(path);
        var stream = Files.lines(pathObj);
        var titlesList = stream.skip(1).map(s -> titleParser.parseTitle(s))
                .map(t -> serialize(t)).toList();
        stream.close();
        var batches = ListUtils.partition(titlesList, 500);
        batches.parallelStream().forEach(batch -> {
            try {
                var json = batch.parallelStream().collect(Collectors.joining("\n")) + "\n";
                var request = new Request("POST", "/_bulk");
                request.setJsonEntity(json);
                var response = restClient.performRequest(request);
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }

    private String serialize(Title t) {
        var i = new Index("imdb", t.id());
        var title = new TitleToIndex(t.type(), t.primaryTitle(), t.originalTitle(), t.isAdult(), t.startYear(),
                t.endYear(),
                t.runtimeMinutes(), t.genres());
        try {
            var iSe = mapper.writeValueAsString(i);
            var titleSe = mapper.writeValueAsString(title);
            return "{\"index\":" + iSe + "}\n" + titleSe;
        } catch (JsonProcessingException e) {
            // Something has gone very wrong if we can's JSON a simple record.
            throw new RuntimeException(e);
        }
    }

    record Index(String _index, String _id) {
    }

    record TitleToIndex(String type, String primaryTitle, String originalTitle, Boolean isAdult,
            Integer startYear, Integer endYear, Integer runtimeMinutes, List<String> genres) {
    }
}

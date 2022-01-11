package co.empathy.p01.app.index;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.concurrent.TimeUnit;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.elasticsearch.action.bulk.BulkProcessor;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.client.Request;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.unit.ByteSizeUnit;
import org.elasticsearch.common.unit.ByteSizeValue;
import org.elasticsearch.common.xcontent.XContentType;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import co.empathy.p01.app.index.parser.TitleParser;
import co.empathy.p01.config.ElasticConfiguration;
import co.empathy.p01.model.Title;
import org.slf4j.Logger;

@Service
public class TitleIndexServiceImpl implements TitleIndexService {

    private final TitleParser titleParser;
    private final RestHighLevelClient cli;
    private final ObjectMapper mapper;
    private final Logger logger = LoggerFactory.getLogger(TitleIndexServiceImpl.class);
    private final ElasticConfiguration config;

    @Autowired
    public TitleIndexServiceImpl(TitleParser titleParser, RestHighLevelClient cli, ElasticConfiguration config) {
        this.titleParser = titleParser;
        this.cli = cli;
        this.mapper = new ObjectMapper();
        this.config = config;
    }

    @Override
    public void indexTitlesFromTabFile(String path) throws IOException, InterruptedException {
        createIndex();

        var pathObj = Paths.get(path);
        var stream = Files.lines(pathObj);

        var listener = new BulkListener(logger, config.waits());
        var builder = BulkProcessor.builder(
                (request, bulkListener) -> cli.bulkAsync(request, RequestOptions.DEFAULT, bulkListener), listener,
                "bulk-processor-name");

        builder.setBulkSize(new ByteSizeValue(1L, ByteSizeUnit.MB));
        var processor = builder.build();
        stream.skip(1).map(s -> titleParser.parseTitle(s)).map(t -> buildRequest(t)).forEach(rq -> processor.add(rq));
        stream.close();
        processor.awaitClose(100000L, TimeUnit.MINUTES);
    }

    private void createIndex() throws IOException {
        var mappingStream = getClass().getClassLoader().getResourceAsStream("mapping.json");
        var mappings = new String(mappingStream.readAllBytes(), StandardCharsets.UTF_8);
        var rq = new Request("PUT", "/" + config.indexName());
        rq.setJsonEntity(mappings);
        cli.getLowLevelClient().performRequest(rq);
    }

    private String serialize(Title t) {
        var title = new TitleToIndex(t.type(), t.primaryTitle(), t.originalTitle(), t.isAdult(), t.startYear(),
                t.endYear(),
                t.runtimeMinutes(), t.genres());
        try {
            var titleSe = mapper.writeValueAsString(title);
            return titleSe;
        } catch (JsonProcessingException e) {
            // Something has gone very wrong if we can't JSON a simple record.
            throw new RuntimeException(e);
        }
    }

    private IndexRequest buildRequest(Title t) {
        var json = serialize(t);
        var rq = new IndexRequest(config.indexName());
        rq.id(t.id());
        rq.source(json, XContentType.JSON);
        return rq;
    }

    record TitleToIndex(String type, String primaryTitle, String originalTitle, Boolean isAdult,
            Integer startYear, Integer endYear, Integer runtimeMinutes, List<String> genres) {
    }
}

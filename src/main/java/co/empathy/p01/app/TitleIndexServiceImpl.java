package co.empathy.p01.app;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.concurrent.TimeUnit;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.elasticsearch.action.bulk.BulkProcessor;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.unit.ByteSizeUnit;
import org.elasticsearch.common.unit.ByteSizeValue;
import org.elasticsearch.common.xcontent.XContentType;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import co.empathy.p01.model.Title;
import org.slf4j.Logger;

@Service
public class TitleIndexServiceImpl implements TitleIndexService {

    private final TitleParser titleParser;
    private final RestHighLevelClient cli;
    private final ObjectMapper mapper;
    Logger logger = LoggerFactory.getLogger(TitleIndexServiceImpl.class);

    @Autowired
    public TitleIndexServiceImpl(TitleParser titleParser, RestHighLevelClient cli) {
        this.titleParser = titleParser;
        this.cli = cli;
        this.mapper = new ObjectMapper();
    }

    @Override
    public void indexTitlesFromTabFile(String path) throws IOException, InterruptedException {
        var pathObj = Paths.get(path);
        var stream = Files.lines(pathObj);

        BulkProcessor.Listener listener = new BulkProcessor.Listener() {
            @Override
            public void afterBulk(long executionId, BulkRequest request,
                    Throwable failure) {
                logger.error("Failed to execute bulk", failure);
            }

            @Override
            public void beforeBulk(long executionId, BulkRequest request) {
                int numberOfActions = request.numberOfActions();
                logger.debug("Executing bulk [{}] with {} requests",
                        executionId, numberOfActions);
            }

            @Override
            public void afterBulk(long executionId, BulkRequest request,
                    BulkResponse response) {
                if (response.hasFailures()) {
                    logger.warn("Bulk [{}] executed with failures", executionId);
                } else {
                    logger.debug("Bulk [{}] completed in {} milliseconds",
                            executionId, response.getTook().getMillis());
                }
            }
        };
        BulkProcessor.Builder builder = BulkProcessor.builder(
                (request, bulkListener) -> cli.bulkAsync(request, RequestOptions.DEFAULT, bulkListener), listener,
                "bulk-processor-name");
        builder.setBulkSize(new ByteSizeValue(1L, ByteSizeUnit.MB));
        var processor = builder.build();
        stream.skip(1).map(s -> titleParser.parseTitle(s)).map(t -> buildRequest(t)).forEach(rq -> processor.add(rq));
        stream.close();
        processor.awaitClose(100L, TimeUnit.SECONDS); 
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
        var rq = new IndexRequest("imdb");
        rq.id(t.id());
        rq.source(json, XContentType.JSON);
        return rq;
    }

    record TitleToIndex(String type, String primaryTitle, String originalTitle, Boolean isAdult,
            Integer startYear, Integer endYear, Integer runtimeMinutes, List<String> genres) {
    }
}

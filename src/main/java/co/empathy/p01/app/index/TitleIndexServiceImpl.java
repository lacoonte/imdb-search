package co.empathy.p01.app.index;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;

import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.elasticsearch.action.bulk.BulkProcessor;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.client.Request;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.ResponseException;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.unit.ByteSizeUnit;
import org.elasticsearch.common.unit.ByteSizeValue;
import org.elasticsearch.common.xcontent.XContentType;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import co.empathy.p01.app.index.parser.RatingParser;
import co.empathy.p01.app.index.parser.TitleParser;
import co.empathy.p01.config.ElasticConfiguration;
import co.empathy.p01.model.Title;
import org.slf4j.Logger;

@Service
public class TitleIndexServiceImpl implements TitleIndexService {

    private final TitleParser titleParser;
    private final RatingParser ratingParser;
    private final RestHighLevelClient cli;
    private final ObjectMapper mapper;
    private final Logger logger = LoggerFactory.getLogger(TitleIndexServiceImpl.class);
    private final ElasticConfiguration config;

    @Autowired
    public TitleIndexServiceImpl(TitleParser titleParser, RatingParser ratingParser, RestHighLevelClient cli,
            ElasticConfiguration config) {
        this.titleParser = titleParser;
        this.cli = cli;
        this.mapper = new ObjectMapper();
        this.mapper.addMixIn(Title.class, TitleIgnoreIdMixIn.class);
        this.ratingParser = ratingParser;
        this.config = config;
    }

    @Override
    public void indexTitlesFromTabFile(String path)
            throws IOException, IndexAlreadyExistsException, IndexFailedException, FileNotExistsExcetion {

        var pathObj = Paths.get(path);
        if (Files.notExists(pathObj))
            throw new FileNotExistsExcetion(path);

        createIndex();

        var stream = Files.lines(pathObj);

        var listener = new BulkListener(logger, config.waits());
        var builder = BulkProcessor.builder(
                (request, bulkListener) -> cli.bulkAsync(request, RequestOptions.DEFAULT, bulkListener), listener,
                "bulk-processor-name");

        builder.setBulkSize(new ByteSizeValue(1L, ByteSizeUnit.MB));
        var processor = builder.build();
        stream.skip(1).map(s -> titleParser.parseTitle(s)).map(t -> buildRequest(t)).forEach(rq -> processor.add(rq));
        stream.close();
        try {
            processor.awaitClose(100000L, TimeUnit.MINUTES);
        } catch (InterruptedException e) {
            logger.error(e.getMessage(), e);
            throw new IndexFailedException(e);
        }
    }

    @Override
    public void indexTitlesWithRatingsFromTabFiles(String titlesPath, String ratingsPath)
            throws IOException, IndexAlreadyExistsException, IndexFailedException, FileNotExistsExcetion {

        var pathObj = Paths.get(titlesPath);
        var ratingsPathObj = Paths.get(ratingsPath);
        if (Files.notExists(pathObj) || Files.notExists(ratingsPathObj))
            throw new FileNotExistsExcetion(titlesPath);

        var ratingsStream = Files.lines(ratingsPathObj);
        var stream = Files.lines(pathObj);
        createIndex();

        var listener = new BulkListener(logger, config.waits());
        var builder = BulkProcessor.builder(
                (request, bulkListener) -> cli.bulkAsync(request, RequestOptions.DEFAULT, bulkListener), listener,
                "bulk-processor-name");

        builder.setBulkSize(new ByteSizeValue(1L, ByteSizeUnit.MB));
        var processor = builder.build();

        var parsedTitles = stream.skip(1).map(s -> titleParser.parseTitle(s));
        var parsedRatings = ratingsStream.skip(1).map(r -> ratingParser.parseRating(r))
                .collect(Collectors.toMap(r -> r.id(), r -> r));

        parsedTitles.map(pT -> Optional.ofNullable(parsedRatings.getOrDefault(pT.id(), null))
                .map(rS -> pT.withRating(rS.averageRating(), rS.numVotes())).orElse(pT))
                .map(title -> buildRequest(title)).forEach(rq -> processor.add(rq));

        ratingsStream.close();
        stream.close();

        try {
            processor.awaitClose(100000L, TimeUnit.MINUTES);
        } catch (InterruptedException e) {
            logger.error(e.getMessage(), e);
            throw new IndexFailedException(e);
        }
    }

    private void createIndex() throws IOException, IndexAlreadyExistsException {
        var mappingStream = getClass().getClassLoader().getResourceAsStream("mapping.json");
        var mappings = new String(mappingStream.readAllBytes(), StandardCharsets.UTF_8);
        var rq = new Request("PUT", "/" + config.indexName());
        rq.setJsonEntity(mappings);
        try {
            cli.getLowLevelClient().performRequest(rq);
        } catch (ResponseException e) {
            logger.error(e.getMessage(), e);
            // TODO: Research if we have a more accurate way of detecting
            // resource_already_exists error.
            if (e.getResponse().getStatusLine().getStatusCode() == HttpStatus.BAD_REQUEST.value())
                throw new IndexAlreadyExistsException(e);
            else
                throw e;
        }
    }

    private String serialize(Title t) {
        try {
            return mapper.writeValueAsString(t);
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
}

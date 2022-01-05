package co.empathy.p01.app.index;

import org.elasticsearch.action.bulk.BulkProcessor;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.support.WriteRequest;
import org.slf4j.Logger;

class BulkListener implements BulkProcessor.Listener {

    private final Logger logger;
    private final boolean waitForIndexing;

    public BulkListener(Logger logger, boolean waitForIndexing) {
        this.logger = logger;
        this.waitForIndexing = waitForIndexing;
    }

    @Override
    public void afterBulk(long executionId, BulkRequest request,
            Throwable failure) {
        logger.error("Failed to execute bulk", failure);
    }

    @Override
    public void beforeBulk(long executionId, BulkRequest request) {
        int numberOfActions = request.numberOfActions();

        //Useful for testing, wait for everything to be refreshed before giving a response.
        if (waitForIndexing)
            request.setRefreshPolicy(WriteRequest.RefreshPolicy.WAIT_UNTIL);
        
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

}

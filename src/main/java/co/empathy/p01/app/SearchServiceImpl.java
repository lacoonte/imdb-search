package co.empathy.p01.app;

import org.elasticsearch.action.admin.cluster.settings.ClusterGetSettingsRequest;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class SearchServiceImpl implements SearchService {

    private final RestHighLevelClient cli;

    @Autowired
    public SearchServiceImpl(RestHighLevelClient cli) {
        this.cli = cli;
    }

    @Override
    public String search(String query) {
        if (query.isEmpty())
            throw new IllegalArgumentException("The query can't be empty");
        return query;
    }

    @Override
    public String getClusterName() throws ClusterNameUnavailableException {
        var cluster = cli.cluster();
        var request = new ClusterGetSettingsRequest();
        request.includeDefaults(true);
        try {
            var settings = cluster.getSettings(request, RequestOptions.DEFAULT);
            var cName = settings.getSetting("cluster.name");
            return cName;
        } catch (Exception e) {
            throw new ClusterNameUnavailableException(e);
        }
    }

}

package models;

import com.google.inject.Inject;
import com.typesafe.config.Config;
import play.libs.Json;
import play.libs.ws.WSClient;
import play.libs.ws.WSRequest;

import java.util.concurrent.CompletionStage;

/**
 * @author Hop Nguyen
 * @version 1: Hop Nguyen implements the project framework, search, and topic feature.
 */
public class GithubClient {
    private final WSClient client;
    private final String baseURL;

    @Inject
    public GithubClient(WSClient client, Config config) {
        this.client = client;
        this.baseURL = config.getString("github.url");
    }

    public CompletionStage<SearchResult> searchRepositories(String query, boolean isTopic) {
        WSRequest request = client.url(baseURL + "/search/repositories");
        return request.addHeader("Accept", "application/vnd.github.v3+json")
                .addQueryParameter("q", (isTopic ? "topic:" : "") + query)
                .addQueryParameter("sort", "updated")
                .addQueryParameter("per_page", "10")
                .get()
                .thenApply(r -> {
                    SearchResult searchResult = Json.fromJson(r.asJson(), SearchResult.class);
                    searchResult.input = query;
                    return searchResult;
                });
    }
}

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

    /**
     * @author Sagar Sanghani
     * @param user name of the user
     * @param repo name of the repository
     * @return Object of RepositoryProfile
     */
    public CompletionStage<RepositoryProfile> getRepositoryDetails(String user, String repo){
        WSRequest request = client.url(baseURL + "/repos/" + user + "/" + repo);
        return request.addHeader("Accept", "application/vnd.github.v3+json")
                .get()
                .thenApply(r -> {
                    RepositoryProfile repositoryProfile = Json.fromJson(r.asJson(), RepositoryProfile.class);
                    return repositoryProfile;
                });
    }
}

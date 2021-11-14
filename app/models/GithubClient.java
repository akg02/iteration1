package models;

import java.util.List;
import java.util.concurrent.CompletionStage;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.inject.Inject;
import com.typesafe.config.Config;

import play.libs.Json;
import play.libs.ws.WSClient;
import play.libs.ws.WSRequest;

/**
 * @author Hop Nguyen
 * @version 1: Hop Nguyen implements the project framework, search, and topic
 *          feature.
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
		System.out.println("Hello");
		return request.addHeader("Accept", "application/vnd.github.v3+json")
				.addQueryParameter("q", (isTopic ? "topic:" : "") + query).addQueryParameter("sort", "updated")
				.addQueryParameter("per_page", "10").get().thenApply(r -> {
					SearchResult searchResult = Json.fromJson(r.asJson(), SearchResult.class);
					searchResult.input = query;
					return searchResult;
				});
	}

	public CompletionStage<List<Issue>> getIssues(String authorName, String repositoryName) {
		WSRequest request = client.url(baseURL + "/repos/" + authorName + "/" + repositoryName + "/issues");
		ObjectMapper objectMapper = new ObjectMapper();

		return request.addHeader("Accept", "application/vnd.github.v3+json").get().thenApply(r -> {
			List<Issue> issues = null;
			try {
				objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
				issues = objectMapper.readValue(r.asJson().toPrettyString(), new TypeReference<List<Issue>>() {

				});

			} catch (Exception e) {
				return null;
			}
			return issues;
		});
	}
}

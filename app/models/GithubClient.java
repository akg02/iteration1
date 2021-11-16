package models;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.inject.Inject;
import com.typesafe.config.Config;
import play.libs.Json;
import play.libs.ws.WSClient;
import play.libs.ws.WSRequest;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

/**
 * @author Hop Nguyen
 * @version 1: Hop Nguyen implements the project framework, search, and topic feature.
 * The GithubClient class, to hold the content for a Github client
 */
public class GithubClient {
    /** The WSClient client */
    private final WSClient client;
    /** The String baseURL */
    private final String baseURL;
    /** The authorization Github token */
    private final String token;

    /** The constructor */
    @Inject
    public GithubClient(WSClient client, Config config) {
        this.client = client;
        this.baseURL = config.getString("github.url");
        this.token = config.getString("github.token");
    }

    /**
     * The method searRepositories, to search the repositories based on the given query and whether it's a topic
     * @param query the given query
     * @param isTopic indicates if the query based on the topic
     * @return the search results
     */
    public CompletionStage<SearchResult> searchRepositories(String query, boolean isTopic) {
        WSRequest request = client.url(baseURL + "/search/repositories");
        return request
                .addHeader("Authorization", token)
                .addHeader("Accept", "application/vnd.github.v3+json")
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
     * Method to call live GitHub api to fetch issues
     * @author Meet Mehta
     * @param authorName username of github repository
     * @param repositoryName repository name
     * @return CompletionStage object of list of issues
     * 
     */
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




    public CompletableFuture<ArrayList<String>> getAllCommitList(String userName, String repoName){
        WSRequest request = client.url(baseURL + "/repos/"+userName+"/" +
                repoName+"/commits");
        return request.addHeader("Accept", "application/vnd.github.v3+json")
                .addHeader("Authorization", token)
                .addQueryParameter("per_page", "100")
                .get()
                .thenApplyAsync( r -> {
                    ArrayList<String> commitIDList = new ArrayList<>();
                   int f = 0;
                   while(r.asJson().get(f)!=null){
                       commitIDList.add(r.asJson().get(f).get("sha").asText());
                       f++;
                   }
                    return commitIDList;
                }).toCompletableFuture();
    }

    public CompletableFuture<CommitStats> getCommitStatByID(String userName, String repoName, String commitID){
        WSRequest request = client.url(baseURL + "/repos/"+userName+"/" +
                repoName+"/commits/"+commitID);

        return request.addHeader("Accept", "application/vnd.github.v3+json")
                .addHeader("Authorization", token)
                .addQueryParameter("per_page", "5")
                .get()
                .thenApplyAsync( r-> {
                   CommitStats commitStats;
                    commitStats = Json.fromJson(r.asJson().get("commit").get("author"), CommitStats.class);
                    commitStats.setName(r.asJson().get("author").get("login").asText());
                    commitStats.setSha(r.asJson().get("sha").asText());
                    commitStats.setAddition(r.asJson().get("stats").get("additions").asInt());
                    commitStats.setDeletion(r.asJson().get("stats").get("deletions").asInt());
                    return commitStats;
                }).toCompletableFuture();
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

    public ArrayList<CommitStats> getCommitStatFromList(String user, String repo, ArrayList<String> list) throws Exception {
        ArrayList<CommitStats> commitStatList = new ArrayList<>();
        for(String s: list){
            commitStatList.add(getCommitStatByID(user, repo, s).get());
        }
        return commitStatList;

    }

}

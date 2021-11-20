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
import java.util.concurrent.ExecutionException;

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

	public CompletionStage<List<Issue>> getIssues(String authorName, String repositoryName) {
		WSRequest request = client.url(baseURL + "/repos/" + authorName + "/" + repositoryName + "/issues");
		ObjectMapper objectMapper = new ObjectMapper();

		return request.addHeader("Authorization", token)
				.addHeader("Accept", "application/vnd.github.v3+json")
                .addHeader("sort", "created")
                .addHeader("direction", "desc")
                .addHeader("state", "all")
                .get().thenApply(r -> {
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


    /**
     * By using username and repository name, function will make one arraylist of id of latest 100 commits
     * If there are less than 100 commits then it will take all commits possible.
     * @param userName github username of owner of repository
     * @param repoName repository name
     * @return Arraylist containing at most 100 latest commits' ID.
     *
     * @author Smit Parmar
     */
    public ArrayList<String> getAllCommitList(String userName, String repoName, int perPage) throws ExecutionException, InterruptedException {
        WSRequest request = client.url(baseURL + "/repos/"+userName+"/" +
                repoName+"/commits");
        return request.addHeader("Accept", "application/vnd.github.v3+json")
                .addHeader("Authorization", token)
                .addQueryParameter("per_page",  new Integer(perPage).toString())
                .get()
                .thenApplyAsync( r -> {
                    ArrayList<String> commitIDList = new ArrayList<>();
                   int f = 0;
                   while(r.asJson().get(f)!=null){
                       commitIDList.add(r.asJson().get(f).get("sha").asText());
                       f++;
                   }
                    return commitIDList;
                }).toCompletableFuture().get();
    }

    /**
     * This function will return new CommitStats model object which has username, email, sha, addition and deletion
     * @param userName github username of owner of repository
     * @param repoName repository name
     * @param commitID ID of commit
     * @return CommitStats object
     *
     * @author Smit Parmar
     */
    public CompletableFuture<CommitStats> getCommitStatByID(String userName, String repoName, String commitID){
        WSRequest request = client.url(baseURL + "/repos/"+userName+"/" +
                repoName+"/commits/"+commitID);

        return request.addHeader("Accept", "application/vnd.github.v3+json")
                .addHeader("Authorization", token)
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
     * This method returns a completionStage object of type RepositoryProfile model. Fetches the repository details from the github api for the given username and repository names.
     * @author Sagar Sanghani
     * @param user name of the user
     * @param repo name of the repository
     * @param issueList list of issues of the repository
     * @return Object of RepositoryProfile
     */
    public CompletionStage<RepositoryProfile> getRepositoryDetails(String user, String repo, List<Issue> issueList) {
        WSRequest request = client.url(baseURL + "/repos/" + user + "/" + repo);
        return request.addHeader("Accept", "application/vnd.github.v3+json")
                .get()
                .thenApply(r -> {
                    RepositoryProfile repositoryProfile = Json.fromJson(r.asJson(), RepositoryProfile.class);
                    repositoryProfile.issues = issueList;
                    return repositoryProfile;
                });
    }

    /**
     * By using loop, this function will give list of commitStats of object on which we can apply stream function.
     *
     * @param user github username of owner of repository
     * @param repo repository name
     * @param list contains all Commit IDs.
     * @return List of CommitStat Objet
     *
     * @author Smit Parmar
     */
    public ArrayList<CommitStats> getCommitStatFromList(String user, String repo, ArrayList<String> list)  {
        ArrayList<CommitStats> commitStatList = new ArrayList<>();
        for(String s: list){
            try {
                commitStatList.add(getCommitStatByID(user, repo, s).get());
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (ExecutionException e) {
                e.printStackTrace();
            }
        }
        return commitStatList;
    }

}

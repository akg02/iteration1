package models;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.inject.Inject;
import com.typesafe.config.Config;
import play.api.http.Status;
import play.cache.AsyncCacheApi;
import play.libs.Json;
import play.libs.ws.WSClient;
import play.libs.ws.WSRequest;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.ExecutionException;

/**
 * Class GithubClient
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

    private final AsyncCacheApi cache;
    /** The constructor
     * @author Hop Nguyen
     */
    @Inject
    public GithubClient(WSClient client, AsyncCacheApi cache, Config config) {
        this.client = client;
        this.cache = cache;
        this.baseURL = config.getString("github.url");
        this.token = config.getString("github.token");
    }

    /**
     * The method searRepositories, to search the repositories based on the given query and whether it's a topic
     * @author Hop Nguyen
     * @param query the given query
     * @param isTopic indicates if the query based on the topic
     * @return the search results
     */
    public CompletionStage<SearchResult> searchRepositories(String query, boolean isTopic) {
        String githubQuery = (isTopic ? "topic:" : "") + query;
        return cache.getOrElseUpdate("search://" + githubQuery, () -> {
            WSRequest request = client.url(baseURL + "/search/repositories");
            return request
                    .addHeader("Authorization", token)
                    .addHeader("Accept", "application/vnd.github.v3+json")
                    .addQueryParameter("q", githubQuery)
                    .addQueryParameter("sort", "updated")
                    .addQueryParameter("per_page", "10")
                    .get()
                    .thenApplyAsync(r -> {
                        SearchResult searchResult = Json.fromJson(r.asJson(), SearchResult.class);
                        searchResult.setInput(query);
                        searchResult.setSuccess(Status.isSuccessful(r.getStatus()));
                        return searchResult;
                    });
        }, 3);
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
     * This method getRepositoryDetails, fetches the repository details like repository name, description, topics, etc. from the GitHub api for the given username and repository names.
     * @author Sagar Sanghani
     * @param user name of the user
     * @param repo name of the repository
     * @param issueList list of issues of the repository
     * @return CompletionStage Object of type RepositoryProfile
     */
    public CompletionStage<RepositoryProfile> getRepositoryDetails(String user, String repo, List<Issue> issueList) {
        WSRequest request = client.url(baseURL + "/repos/" + user + "/" + repo);
        return request.addHeader("Accept", "application/vnd.github.v3+json")
                .addHeader("Authorization", token)
                .get()
                .thenApply(r -> {
                    RepositoryProfile repositoryProfile = Json.fromJson(r.asJson(), RepositoryProfile.class);
                    repositoryProfile.setIssues(issueList);
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
    public ArrayList<CommitStats> getCommitStatFromList(String user, String repo, ArrayList<String> list) throws Exception  {
        ArrayList<CommitStats> commitStatList = new ArrayList<>();
        for(String s: list){
                commitStatList.add(getCommitStatByID(user, repo, s).get());
        }
        return commitStatList;
    }
    
    /**
     * Uses username to fetch public information from the github user
     * and save this information into a ProfileInfo class in addition to the list of repositories
     * 
     * @author Joon Seung Hwang
     * @param user github username
     * @param repoList list of repositories owned by the user
     * @return ProfileInfo object containing details of the user and list of repositories 
     */
    public CompletionStage<ProfileInfo> displayUserProfile(String user, List<String> repoList){
    	WSRequest request = client.url(baseURL + "/users/" + user);
    	return request.addHeader("Accept", "application/vnd.github.v3+json")
                .addHeader("Authorization", token)
    			.get()
    			.thenApply(r -> {
    				ProfileInfo profileInfo = Json.fromJson(r.asJson(), ProfileInfo.class);
    				profileInfo.setRepos(repoList);
    				
    				return profileInfo;
    			});
    	
    }
    
    /**
     * Username is used to return an arraylist of repositories owned by the user
     * 
     * @author Joon Seung Hwang
     * @param user github username
     * @return ArrayList containing list of user's repositories
     */
    public CompletionStage<List<String>> getAllRepoList(String user) {
    	WSRequest request = client.url(baseURL + "/users/" + user + "/repos");
        return request
                .addHeader("Authorization", token)
                .addHeader("Accept", "application/vnd.github.v3+json")
                .get()
                .thenApply(r -> {
                        List<String> repoList = new ArrayList<>();
                        String name = "name";
                        int f = 0;
                        while (r.asJson().get(f) != null) {
                            repoList.add(r.asJson().get(f).get(name).asText());
                            f++;
                        }
                        return repoList;
                });
    }

}

package models;

import com.google.inject.Inject;
import com.typesafe.config.Config;
import play.libs.Json;
import play.libs.ws.WSClient;
import play.libs.ws.WSRequest;

import java.util.ArrayList;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

/**
 * @author Hop Nguyen
 * @version 1: Hop Nguyen implements the project framework, search, and topic feature.
 */
public class GithubClient {
    private final WSClient client;
    private final WSClient statClient;
    private final String baseURL;
    private final String token;
    private ArrayList<CommitStats> list;

    public ArrayList<CommitStats> getList() {
        return list;
    }

    public void setList(ArrayList<CommitStats> list) {
        this.list = list;
    }

    @Inject
    public GithubClient(WSClient client, WSClient statClient, Config config) {
        this.client = client;
        this.statClient = statClient;
        this.baseURL = config.getString("github.url");
        this.token = config.getString("github.token");
        this.list = new ArrayList<>();
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

    public ArrayList<CommitStats> getCommitStatFromList(String user, String repo, ArrayList<String> list) throws Exception {
        ArrayList<CommitStats> commitStatList = new ArrayList<>();
        for(String s: list){
            commitStatList.add(getCommitStatByID(user, repo, s).get());
        }
        return commitStatList;
    }
}

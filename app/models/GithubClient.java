package models;

import com.google.inject.Inject;
import com.typesafe.config.Config;
import org.checkerframework.checker.units.qual.A;
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
        this.list = new ArrayList<>();
    }

    public CompletionStage<SearchResult> searchRepositories(String query, boolean isTopic) {

        WSRequest request = client.url(baseURL + "/search/repositories");
        return request.addHeader("Accept", "application/vnd.github.v3+json")
                .addQueryParameter("q", (isTopic ? "topic:" : "") + query)
                .addQueryParameter("per_page", "10")
                .get()
                .thenApply(r -> {
                    SearchResult searchResult = Json.fromJson(r.asJson(), SearchResult.class);
                    searchResult.input = query;
                    return searchResult;
                });
    }

//    public CompletionStage<ArrayList<CommitStats>> commitList(String authorName, String repoName) {
//        WSRequest commitRequest = client.url(baseURL+"/repos/"+authorName+"/"+repoName+"/commits");
//        ArrayList<CommitStats> commitsList = new ArrayList<>();
//        return commitRequest.addHeader("Accept", "application/vnd.github.v3+json")
//                .addQueryParameter("per_page", "2" )
//                .get()
//                .thenApplyAsync( r -> {
//                    //System.out.println(r.asJson());
//                    int f = 0;
//                    CompletionStage<CommitStats> commits ;
//                    System.out.println("API call start");
//                    while(r.asJson().get(f)!=null) {
//                        WSRequest statRequest;
//                        statRequest = statClient.url(baseURL+"/repos/"+authorName+"/"+repoName+"/commits/"
//                                        +r.asJson().get(f).get("sha").asText());
//                        statRequest.addHeader("Accept", "application/vnd.github.v3+json")
//                                .get()
//                                .thenApplyAsync( sr -> {
//                                    CommitStats commitStats;
//                                   // commitStats = Json.fromJson(r.asJson().get(f).get("commit").get("author"), CommitStats.class);
//                                    commitStats = Json.fromJson(sr.asJson().get("commit").get("author"), CommitStats.class);
//                                    commitStats.setAddition(sr.asJson().get("stats").get("additions").asInt());
//                                    commitStats.setAddition(sr.asJson().get("stats").get("additions").asInt());
//                                    commitsList.add(commitStats);
//                                    System.out.println(commitStats);
//                                    return commitStats;
//                                });
//                        //commits = Json.fromJson(r.asJson().get(f).get("commit").get("author"), CommitStats.class);
//                        //commitsList.add(commits);
//                        f++;
//                    }
//                    return commitsList;
//                });
//    }

    public CompletionStage<ArrayList<CommitStats>> commitList(String authorName, String repoName) {
        WSRequest commitRequest = client.url(baseURL+"/repos/"+authorName+"/"+repoName+"/commits");
        ArrayList<CommitStats> commitsList = new ArrayList<>();
        return commitRequest.addHeader("Accept", "application/vnd.github.v3+json")
                .addQueryParameter("per_page", "5" )
                .addHeader("Authorization", "token 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")
                .get()
                .thenApplyAsync( r -> {
                    int f = 0;
                    CommitStats commits ;
                    while(r.asJson().get(f)!=null) {
                        commits = Json.fromJson(r.asJson().get(f).get("commit").get("author"), CommitStats.class);
                        commits.setName(r.asJson().get(f).get("author").get("login").asText());
                        commits.setSha(r.asJson().get(f).get("sha").asText());
                        commitsList.add(commits);
                        f++;
                    }
                    return commitsList;
                });
    }

    public ArrayList<CommitStats> getCommitStat(String userName, String repoName, ArrayList<CommitStats> list ){
        ArrayList<CommitStats> output = new ArrayList<>();
        for(int i = 0; i<list.size(); i++){
            WSRequest statRequest = statClient.url(baseURL+"/repos/"+userName+"/"
                    +repoName+"/commits/"+list.get(i).getSha());
            statRequest.addHeader("Accept", "application/vnd.github.v3+json")
                    .addHeader("Authorization", "token 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")
                    .get()
                    .thenApplyAsync( r -> {
                        CommitStats commits ;
                        System.out.println(r.asJson().get("stats"));
                        commits = Json.fromJson(r.asJson().get("commit").get("author"), CommitStats.class);
                        commits.setName(r.asJson().get("author").get("login").asText());
                        commits.setSha(r.asJson().get("sha").asText());
                        commits.setAddition(r.asJson().get("stats").get("additions").asInt());
                        commits.setDeletion(r.asJson().get("stats").get("deletions").asInt());
                        output.add(commits);
                        System.out.println("commits"+ commits);
                        return commits;
                    });
        }
        return output;

    }
}

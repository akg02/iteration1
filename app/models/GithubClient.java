package models;

import com.google.inject.Inject;
import com.typesafe.config.Config;
import play.libs.Json;
import play.libs.ws.WSClient;
import play.libs.ws.WSRequest;
import scala.collection.immutable.List;

import java.util.ArrayList;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.Future;

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
        WSRequest commitRequest = client.url(baseURL+"/repos/smituparmar/MedicoGraph/commits");
        ArrayList<CommitStats> commitsList = new ArrayList<>();
        commitRequest.addHeader("Accept", "application/vnd.github.v3+json")
                .addQueryParameter("per_page", "5" )
                .get()
                .thenApply( r -> {
                    //System.out.println(r.asJson());
                    int f = 0;
                    CommitStats commits ;
                    while(r.asJson().get(f)!=null) {
                        System.out.println(r.asJson().get(f).get("commit").get("author"));
                        commits = Json.fromJson(r.asJson().get(f).get("commit").get("author"), CommitStats.class);
                        commitsList.add(commits);
                        System.out.println(commits);
                        f++;
                    }
                    System.out.println(commitsList.get(0));
                    return null;
                });
        System.out.println(commitsList);
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

    public CompletionStage<ArrayList<CommitStats>> commitList(String authorName, String repoName) {
        WSRequest commitRequest = client.url(baseURL+"/repos/"+authorName+"/"+repoName+"/commits");
        WSRequest statRequest = null;
        ArrayList<CommitStats> commitsList = new ArrayList<>();
        return commitRequest.addHeader("Accept", "application/vnd.github.v3+json")
                .addQueryParameter("per_page", "100" )
                .get()
                .thenApplyAsync( r -> {
                    //System.out.println(r.asJson());
                    int f = 0;
                    CommitStats commits ;
                    System.out.println("API call start");
                    while(r.asJson().get(f)!=null) {



                        System.out.println();
                        commits = Json.fromJson(r.asJson().get(f).get("commit").get("author"), CommitStats.class);
                        commitsList.add(commits);
                        System.out.println(commitsList);
                        f++;
                    }
                    return commitsList;
                });
    }
}

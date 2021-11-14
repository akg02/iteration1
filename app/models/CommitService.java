package models;

import com.google.inject.Inject;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.stream.Collectors;

public class CommitService {
    private final GithubClient github;

    @Inject
    public CommitService(GithubClient github) {
        this.github = github;
    }

    public CompletionStage<List<CommitStats>> getCommitStats(String user, String repo) {
        return github.commitList(user,repo).thenApply((output) -> {
            System.out.println(output);
            return output;
        });
//        return CompletableFuture.supplyAsync(() -> {
//            List<CommitStats> result = null;
//            try{
//                System.out.println(github==null);
//                result = github.commitList(user, repo);
//                System.out.println("result is here"+result);
//            }
//            catch (Exception e){
//                e.printStackTrace();
//            }
//            return result;
//        });
    }

//    public Future<List<Commits>> getStats() {
//        List<CompletableFuture<Commits>> commitStats = (List<CompletableFuture<Commits>>) github.commitList("smituparmar", "MedicoGraph");
//
//        return (CompletionStage<Commits>) commitStats;
//    }
}

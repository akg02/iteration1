package models;

import com.google.inject.Inject;

import java.util.*;
import java.util.concurrent.CompletionStage;
import java.util.stream.Collectors;

public class CommitService {
    private final GithubClient github;

    @Inject
    public CommitService(GithubClient github) {
        this.github = github;
    }

    public CompletionStage<Map<String, Integer>> getCommitStats(String user, String repo) {
        //ArrayList<CommitStats> commitStatsList = new ArrayList<>();
        CompletionStage<ArrayList<CommitStats>> commitList = github.commitList(user, repo);
        return commitList.thenApplyAsync(list -> {
                github.getCommitStat(user, repo, list);
                return list;
            }
        ).thenApplyAsync((output) -> {
            System.out.println("output1111"+output);
            Map<String, Integer> result = output.stream()
                    .map(op -> op.getName())
                    .collect(Collectors.toMap(w -> w, w -> 1, Integer::sum));

            result = result.entrySet()
                    .stream()
                    .sorted(Collections.reverseOrder(Map.Entry.comparingByValue()))
                    .limit(10)
                    .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (e1, e2) -> e2, LinkedHashMap::new));
            System.out.println(result);
            return result;
        });
//        System.out.println("My list is here" + commitStatsList);
//        return github.commitList(user, repo).thenApplyAsync((output) -> {
//            Map<String, Integer> result = output.stream()
//                    .map(op -> op.getName())
//                    .collect(Collectors.toMap(w -> w, w -> 1, Integer::sum));
//
//            result = result.entrySet()
//                    .stream()
//                    .sorted(Collections.reverseOrder(Map.Entry.comparingByValue()))
//                    .limit(10)
//                    .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (e1, e2) -> e2, LinkedHashMap::new));
//            System.out.println(result);
//            return result;
//
//        });
    }

//    public Future<List<Commits>> getStats() {
//        List<CompletableFuture<Commits>> commitStats = (List<CompletableFuture<Commits>>) github.commitList("smituparmar", "MedicoGraph");
//
//        return (CompletionStage<Commits>) commitStats;
//    }
}

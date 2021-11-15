package models;

import com.google.inject.Inject;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.stream.Collectors;

public class CommitService {
    private final GithubClient github;


    @Inject
    public CommitService(GithubClient github) {
        this.github = github;
    }

    public CompletionStage<Map<Commits, Integer>> getCommitStats(String user, String repo) throws Exception{
        ArrayList<String> commitIdList = github.getAllCommitList(user, repo).get();
        ArrayList<CommitStats> commitStatsList = github.getCommitStatFromList(user, repo, commitIdList);

        return CompletableFuture.supplyAsync( () -> {
            List<Commits> commitList = commitStatsList.stream()
                    .map(op -> new Commits(op.getName(), op.getAddition(), op.getDeletion()))
                    .collect(Collectors.toList());
            System.out.println(commitList);

            System.out.println("list "+ commitList.stream()
                    .collect(Collectors.groupingBy(Commits::getUserName))+"\n\n\n");

            Map<Commits, Integer> result = commitList.stream()
                    .collect(Collectors.toMap(w -> w, w -> 1, Integer::sum));

            result = result.entrySet()
                    .stream()
                    .sorted(Collections.reverseOrder(Map.Entry.comparingByValue()))
                    .limit(10)
                    .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (e1, e2) -> e2, LinkedHashMap::new));
            System.out.println(result);
            return result;
        });
    }
}

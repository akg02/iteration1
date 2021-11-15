package services;

import com.google.inject.Inject;
import models.CommitStats;
import models.Commits;
import models.GithubClient;

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

    public CompletionStage<List<Map<String, Integer>>> getCommitStats(String user, String repo) throws Exception{
        ArrayList<String> commitIdList = github.getAllCommitList(user, repo).get();
        ArrayList<CommitStats> commitStatsList = github.getCommitStatFromList(user, repo, commitIdList);

        return CompletableFuture.supplyAsync( () -> {
            List<Commits> commitList = commitStatsList.parallelStream()
                    .map(op -> new Commits(op.getName(), op.getAddition(), op.getDeletion()))
                    .collect(Collectors.toList());

            Map<String, Integer> result = commitList.parallelStream()
                    .collect(Collectors.toMap(w -> w.getUserName(), w -> 1, Integer::sum));

            Map<String, Integer> maxAddition = commitList.parallelStream()
                    .collect(Collectors.toMap(w -> w.getUserName(), w -> w.getAddition(), Integer::max));

            Map<String, Integer> minAddition = commitList.parallelStream()
                    .collect(Collectors.toMap(w -> w.getUserName(), w -> w.getAddition(), Integer::min));

            Map<String, Integer> sumAddition = commitList.parallelStream()
                    .collect(Collectors.toMap(w -> w.getUserName(), w -> w.getAddition(), Integer::sum));

            Map<String, Integer> maxDeletion = commitList.parallelStream()
                    .collect(Collectors.toMap(w -> w.getUserName(), w -> w.getDeletion(), Integer::max));

            Map<String, Integer> minDeletion = commitList.parallelStream()
                    .collect(Collectors.toMap(w -> w.getUserName(), w -> w.getDeletion(), Integer::min));

            Map<String, Integer> sumDeletion = commitList.parallelStream()
                    .collect(Collectors.toMap(w -> w.getUserName(), w -> w.getDeletion(), Integer::sum));

            result = result.entrySet()
                    .parallelStream()
                    .sorted(Collections.reverseOrder(Map.Entry.comparingByValue()))
                    .limit(10)
                    .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (e1, e2) -> e2, LinkedHashMap::new));

            List<Map<String, Integer>> resultList = new ArrayList<>();
            resultList.add(result);
            resultList.add(maxAddition);
            resultList.add(minAddition);
            resultList.add(sumAddition);
            resultList.add(maxDeletion);
            resultList.add(minDeletion);
            resultList.add(sumDeletion);

            return resultList;
        });
    }
}

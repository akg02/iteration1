package services;

import com.google.inject.Inject;
import models.CommitStats;
import models.Commits;
import models.GithubClient;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.stream.Collectors;

/**
 * This is the service file for counting statistics of commit page.<br/>
 * Counting of all aggregation function after fetching data handled by this class.
 *
 * @author Smit Parmar
 */
public class CommitService {
    private final GithubClient github;

    /**
     * This is a Parameterised Constructor
     * @param github object of GithubClient by using which we are making API calls
     */
    @Inject
    public CommitService(GithubClient github) {
        this.github = github;
    }

    /**
     * Firstly, this function will fetch data and then by using streams it will compute requried data.
     *
     * @param user gihub username of owner of repository
     * @param repo repository name
     * @return List of maps which contains of all statistical data shown in the page
     */
    public CompletionStage<List<Map<String, Integer>>> getCommitStats(String user, String repo) {
        try{
            ArrayList<String> commitIdList = github.getAllCommitList(user, repo, 100);
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
        catch (Exception e){
            e.printStackTrace();
        }
        return null;

    }
}

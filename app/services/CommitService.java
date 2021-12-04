package services;

import com.google.inject.Inject;
import com.google.inject.Singleton;
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
@Singleton
public class CommitService {
    private static GithubClient github = null;

    /**
     * This is a Parameterised Constructor
     * @param github object of GithubClient by using which we are making API calls
     */
    private static final CommitService instance = new CommitService(github);

    public static CommitService getInstance() {
        return instance;
    }

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
    public CompletionStage<ArrayList<Map<String, Integer>>> getCommitStats(String user, String repo) {
        try{

            ArrayList<String> commitIdList = github.getAllCommitList(user, repo, 5);
            ArrayList<CommitStats> commitStatsList = github.getCommitStatFromList(user, repo, commitIdList);

            return CompletableFuture.supplyAsync( () -> {
                List<Commits> commitList = commitStatsList.parallelStream()
                        .map(op -> new Commits(op.getName(), op.getAddition(), op.getDeletion()))
                        .collect(Collectors.toList());

                Integer allMaxAdditionCommit = commitStatsList.stream()
                        .max((Comparator.comparing(CommitStats::getAddition)))
                        .get().getAddition();

                Integer allMinAdditionCommit = commitStatsList.stream()
                        .min((Comparator.comparing(CommitStats::getAddition)))
                        .get().getAddition();

                Integer allAvgAdditionCommit = commitStatsList.stream()
                        .mapToInt(CommitStats::getAddition)
                        .sum();

                Integer allMaxDeletionCommit = commitStatsList.stream()
                        .max((Comparator.comparing(CommitStats::getDeletion)))
                        .get().getDeletion();

                Integer allMinDeletionCommit = commitStatsList.stream()
                        .min((Comparator.comparing(CommitStats::getDeletion)))
                        .get().getDeletion();

                Integer allAvgDeletionCommit = commitStatsList.stream()
                        .mapToInt(CommitStats::getDeletion)
                        .sum();

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

                ArrayList<Map<String, Integer>> resultList = new ArrayList<>();
                HashMap<String, Integer> maxAllCommitAddition = new HashMap<String, Integer>() {{
                    put("maxAddition", allMaxAdditionCommit);
                }};

                HashMap<String, Integer> minAllCommitAddition = new HashMap<String, Integer>() {{
                    put("minAddition", allMinAdditionCommit);
                }};

                HashMap<String, Integer> avgAllCommitAddition = new HashMap<String, Integer>() {{
                    put("avgAddition", allAvgAdditionCommit/commitStatsList.size());
                }};

                HashMap<String, Integer> maxAllCommitDeletion = new HashMap<String, Integer>() {{
                    put("maxDeletion", allMaxDeletionCommit);
                }};

                HashMap<String, Integer> minAllCommitDeletion = new HashMap<String, Integer>() {{
                    put("minDeletion", allMinDeletionCommit);
                }};

                HashMap<String, Integer> avAllCommitDeletion = new HashMap<String, Integer>() {{
                    put("avgDeletion", allAvgDeletionCommit/commitStatsList.size());
                }};

                resultList.add(maxAllCommitAddition);
                resultList.add(minAllCommitAddition);
                resultList.add(avgAllCommitAddition);
                resultList.add(maxAllCommitDeletion);
                resultList.add(minAllCommitDeletion);
                resultList.add(avAllCommitDeletion);
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

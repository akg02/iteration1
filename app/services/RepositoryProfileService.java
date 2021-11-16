package services;


import models.GithubClient;
import models.RepoIssue;
import models.RepositoryProfile;

import javax.inject.Inject;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.ExecutionException;

/**
 * @author Sagar Sanghani
 * @version 1
 */
public class RepositoryProfileService {
    private final GithubClient github;
    private final Map<String, RepositoryProfile> sessions;

    @Inject
    public RepositoryProfileService(GithubClient github){
        this.sessions = new HashMap<>();
        this.github = github;
    }

    public CompletionStage<RepositoryProfile> getRepoDetails(String user, String repo) throws Exception {
        List<RepoIssue> issueList = github.getLatestIssues(user, repo).toCompletableFuture().get();
        CompletionStage<RepositoryProfile> repoProfile = github.getRepositoryDetails(user, repo, issueList);
        return repoProfile;
    }

}

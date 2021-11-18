package services;


import models.GithubClient;
import models.Issue;
import models.RepositoryProfile;

import javax.inject.Inject;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.stream.Collectors;

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

    public CompletionStage<RepositoryProfile> getRepoDetails(String user, String repo){
        CompletionStage<RepositoryProfile> repoProfile = github.getIssues(user, repo)
                .thenApply(il -> il.stream().limit(20).collect(Collectors.toList()))
                .thenCompose(il2 -> github.getRepositoryDetails(user, repo, il2));

        return repoProfile;
    }

}
